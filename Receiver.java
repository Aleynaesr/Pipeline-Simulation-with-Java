
import java.net.*;
import java.util.ArrayList;

public class Receiver {

    private final static int FIX_PORT = Sender.PORT;
    private static Object recvObj;

    public static void main(String... args) throws Exception {
        // check argument number
        if (args.length < 3) {
            System.out.println("Usage: java Receiver <protocol selector> <window size> <ack percent>"); //make documentation
            return;
        }

        // get input arguments
        int protocolType = Integer.parseInt(args[0]);
        int windowSize = Integer.parseInt(args[1]);
        int percent = Integer.parseInt(args[2]);
  
        DatagramSocket socket;
        while(true) {
            try {
                socket = new DatagramSocket(FIX_PORT);
                break;
            } catch (SocketException e) {
            	e.printStackTrace(System.out);
            	return;
            }
        }

        // use appropriate protocol type to receive
        if (protocolType == 0) {
            System.out.println("GBN protocol");
            GBNReceiver receiver = new GBNReceiver(socket,percent);
            receiver.start();
            recvObj = receiver;
        } else if (protocolType == 1) {
            System.out.println("SR protocol");
            SRReceiver receiver = new SRReceiver(socket,windowSize,percent);
            receiver.start();
            recvObj = receiver;
        } else {
            throw new Exception("invalid protocol type");
        }
    }
    
    public static final ArrayList<byte[]> getData(){
    	if(recvObj == null) {
    		return null;
    	}
    	
    	if(recvObj instanceof GBNReceiver) {
    		return ((GBNReceiver) recvObj).data;
    	} else if(recvObj instanceof SRReceiver) {
    		return ((SRReceiver) recvObj).data;
    	}
    	
    	return null;
    }

}
