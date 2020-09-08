import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class StartServer {
	
	private static String protocolType, timeOut, windowSize, percent;
	private static long lastTime;

	public static void main(String[] args) throws Exception {
		System.out.println("0 - GBN");
		System.out.println("1 - SR");
		System.out.print("protocol> ");
		Scanner scanner = new Scanner(System.in);
		protocolType = scanner.nextLine();
		System.out.print("timeout (ms)> ");
		timeOut = scanner.nextLine();
		System.out.print("windowSize> ");
		windowSize = scanner.nextLine();
		System.out.println("Enter the expected losses in packages and acknowledge is ");  //user enter the expected losses 
		percent = scanner.nextLine();
		scanner.close();
		recvThread.start();
		Thread.currentThread().sleep(1000);
		sendThread.start();
	}

	// with anonymous class and time calculating
	private static Thread recvThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try { 
				Receiver recv = new Receiver();
				recv.main(protocolType,windowSize,percent); 
				lastTime = System.currentTimeMillis() - lastTime;
				System.out.println("Data time: " + (lastTime) +"ms");
				/** List<byte[]> data = recv.getData();
				String[] arr = new String[data.size()+2];
				
				arr[0] = protocolType;
				arr[1] = timeOut;
				
				for(int i = 2;i < data.size();i++) {
					byte[] item = data.get(i-2);
					String str = new String(item);
					arr[i] = str;
				}
				
				run();
				Thread.currentThread().sleep(1000);
				sendThread.start();
				*/
			} catch(Throwable t) {
				throw new RuntimeException(t);
			}
			
			System.exit(0);
		}
	});
	
	// without anonymous class
	private static Thread sendThread = new Thread(new SendRun());
	
	private static class SendRun implements Runnable {

		@Override
		public void run() {
			Sender send = new Sender();
			try {
				lastTime = System.currentTimeMillis();
				send.main(getArray(protocolType,timeOut));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private static Random rand = new Random();
	
	private static String[] getArray(String protocolType, String timeOut) {
		String[] out = new String[rand.nextInt(10)+3];
		out[0] = protocolType;
		out[1] = timeOut;
		for(int i = 2;i < out.length;i++) {
			out[i] = Integer.toString(rand.nextInt());
		}
		return out;
	}

}
