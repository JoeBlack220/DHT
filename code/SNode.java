import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import java.io.*;
import java.util.*;
import java.net.InetAddress; 
public class SNode {
	public static SNodeServiceHandler handler;
	public static SNodeService.Processor processor;
	public static int nodeNum;
	public static int tableSize;
	public static void main(String [] args) {
		try {	Scanner sc = new Scanner(System.in);
			int nodeNum = 0;
			// Set how many nodes are needed for the DHT
			System.out.println("Please enter the node number of the DHT: ");
			nodeNum = Integer.parseInt(sc.nextLine());
			System.out.println("Please enter the size of the DHT: (e.g.: enter 5 for a DHT with 2^5 = 32 entries.)");
			tableSize = Integer.parseInt(sc.nextLine());
			System.out.println("The DHT's size is: " + tableSize + " and the number of entries is: " + ((int) Math.pow(2,tableSize)));
			// Create service request handler
			handler = new SNodeServiceHandler(nodeNum, tableSize);
			processor = new SNodeService.Processor(handler);

			Runnable simple = new Runnable() {
				public void run() {
				simple(processor);
				}
			};

			new Thread(simple).start();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void simple(SNodeService.Processor processor) {
		try {
			// Get the ip address of the current computer
			InetAddress localhost;
			String sip = "";
			try{
				localhost = InetAddress.getLocalHost();
				sip = (localhost.getHostAddress()).trim();
			}
			catch (Exception e){
				e.printStackTrace();
			}
			Scanner sc = new Scanner(System.in);
			// Show the current computer's ip
			System.out.println("Now the super node has ip: " + sip + ".");
			System.out.println("Please enter the port of the super node: ");
			// Set the port of the server
			String serverPort = sc.nextLine();
			// Create Thrift server socket
			TServerTransport serverTransport = new TServerSocket(Integer.parseInt(serverPort));
			TTransportFactory factory = new TFramedTransport.Factory();
			// Set server arguments
			TServer.Args args = new TServer.Args(serverTransport);
			args.processor(processor);  //Set handler
			args.transportFactory(factory);  //Set FramedTransport (for performance)

			//Run server as a multithread server
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
			System.out.println("Start to serve as a super node.");
			server.serve();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

