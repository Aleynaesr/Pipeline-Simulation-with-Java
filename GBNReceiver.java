
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class GBNReceiver {

    private static final int BUFFER_SIZE = 512;
    private static final int SEQNUM_MODULO = 256;

    private int expectedSeqNum;

    private DatagramSocket socket;
    ArrayList<byte[]> data;

    private InetAddress channelAddress;
    private int channelPort;
    private boolean getChannelInfo;
    private int mPercent;

    GBNReceiver(DatagramSocket socket, int percent) throws Exception {
        this.socket = socket;
        expectedSeqNum = 0;
        getChannelInfo = false;
        data = new ArrayList<>();
        mPercent = percent;
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
                
                Util.endReceiverSession(packet, channelAddress, channelPort, socket);
                break;

            } else if (packet.getType() == 0){
                // process data packet
                System.out.println(String.format("PKT RECV DAT %s %s", packet.getLength(), packet.getSeqNum()));
                if (packet.getSeqNum() == expectedSeqNum) {
                	 data.add(packet.getData());
                     System.out.println(data.get(data.size()-1));
                    Util.sendACK(expectedSeqNum, channelAddress, channelPort, socket);
                    expectedSeqNum = (expectedSeqNum + 1) % SEQNUM_MODULO;
                } else {
                    Util.sendACK(((expectedSeqNum + SEQNUM_MODULO - 1) % SEQNUM_MODULO),
                            channelAddress, channelPort, socket);
                }
            }
        }

        // close socket
        System.out.println("Finish receiving data");
        socket.close();
    }
}
