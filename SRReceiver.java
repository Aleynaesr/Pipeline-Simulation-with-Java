
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SRReceiver {

    private static final int BUFFER_SIZE = 512;
    private static final int SEQNUM_MODULO = 256;
    private static int WINDOW_SIZE;

    private int base;

    private Map<Integer, Packet> map;
    ArrayList<byte[]> data;

    private DatagramSocket socket;

    private InetAddress channelAddress;
    private int channelPort;
    private boolean getChannelInfo;
    private int mPercent;

    SRReceiver(DatagramSocket socket, int winSize, int percent) throws Exception {
        this.socket = socket;
        base = 0;
        getChannelInfo = false;
        map = new HashMap<>();
        data = new ArrayList<>();
        WINDOW_SIZE = winSize;
        mPercent = percent;
    }

    // check if ackNum falls in the receiver's window
    private boolean withinWindow(int ackNum) {
        int distance = ackNum - base;
        if (ackNum < base) {
            distance += SEQNUM_MODULO;
        }
        return distance < WINDOW_SIZE;
    }

    // check if ackNum falls in receiver's previous window
    private boolean withinPrevWindow(int ackNum) {
        int distance = base - ackNum;
        if (base < ackNum) {
            distance += SEQNUM_MODULO;
        }
        return distance <= WINDOW_SIZE && distance > 0;
    }

    public void start() throws Exception {

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket receiveDatagram = new DatagramPacket(buffer, buffer.length);

        System.out.println("Start to receive data");
        while(true) {
            // receive packet
            socket.receive(receiveDatagram);
            Packet packet = Packet.getPacket(receiveDatagram.getData());

            // get channel info
            if (!getChannelInfo) {
                channelAddress = receiveDatagram.getAddress();
                channelPort = receiveDatagram.getPort();
                getChannelInfo = true;
            }

            if (packet.getType() == 2) {
                // end receiver session when receiving EOT
                Util.endReceiverSession(packet, channelAddress, channelPort, socket);
                break;

            } else if (packet.getType() == 0){
                // process data packets
                int ackNum = packet.getSeqNum();
                System.out.println(String.format("PKT RECV DAT %s %s", packet.getLength(), ackNum));
                if (withinWindow(ackNum)) {
                    // send ACK back to sender
                    Util.sendACK(ackNum, channelAddress, channelPort, socket);
                    if (!map.containsKey(ackNum)) {
                        map.put(ackNum, packet);
                    }

                    // if ackNum == base, move forward the window
                    if (ackNum == base && map.containsKey(ackNum)) {
                        while (map.containsKey(ackNum)) {
							Packet dataPacket = map.get(ackNum);
                            data.add(dataPacket.getData());
                            System.out.println(data.get(data.size()-1));
                            map.remove(ackNum);
                            ackNum = (ackNum + 1) % SEQNUM_MODULO;
                        }
                        base = ackNum % SEQNUM_MODULO;
                    }

                } else if (withinPrevWindow(ackNum)) {
                    
                    Util.sendACK(ackNum, channelAddress, channelPort, socket);
                }
            }

        }

        // close socket
        System.out.println("Finish receiving data");
        socket.close();
    }
}
