import java.io.*;

public class Sender {
	
	public static final int PORT = 65530;

    public static void main(String[] args) throws Exception {
        // check argument number
        if (args.length < 3) {
            System.out.println("Usage: java Sender <protocol selector> <timeout> <data>");
            return;
        }

        // geting input arguments
        int protocolType = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);

        String hostName = "localhost";

        // checking input data
        String[] newArr = new String[args.length-2];
        for(int i = 2;i < args.length;i++){
			newArr[i-2] = args[i];
		}

        // using  protocol type to send 
        if (protocolType == 0) {
            System.out.println("GBN protocol");
            GBNSender sender = new GBNSender(newArr, hostName, PORT, timeout);
            sender.start();
        } else if (protocolType == 1) {
            System.out.println("SR protocol");
            SRSender sender = new SRSender(newArr, hostName, PORT, timeout);
            sender.start();
        } else {
            throw new Exception("invalid protocol type");
        }
    }
}
