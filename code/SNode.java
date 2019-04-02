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
	public static void main(String [] args) {
		try {	Scanner sc = new Scanner(System.in);
			int nodeNum = 0;
			System.out.println("Please enter the node number of the DHT: ");
			nodeNum = Integer.parseInt(sc.nextLine());
			//Create service request handler
			handler = new SNodeServiceHandler(nodeNum);
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
			System.out.println("Now the super node has ip: " + sip + ".");
			System.out.println("Please enter the port of the super node: ");
			String serverPort = sc.nextLine();
			//Create Thrift server socket
			TServerTransport serverTransport = new TServerSocket(Integer.parseInt(serverPort));
			TTransportFactory factory = new TFramedTransport.Factory();
			//Set server arguments
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

