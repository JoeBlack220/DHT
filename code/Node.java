import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import java.util.*;
import java.lang.Thread;
import java.io.*;
import java.net.InetAddress; 
public class Node {
	public static NodeServiceHandler handler;
	public static NodeService.Processor processor;

	public static void main(String [] args) {
		try {
			handler = new NodeServiceHandler();
			processor = new NodeService.Processor(handler);

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

	public static void simple(NodeService.Processor processor){
		// Get the ip address of the current computer
		InetAddress localhost;
		String nip = "";
		try{
			localhost = InetAddress.getLocalHost();
			nip = (localhost.getHostAddress()).trim();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("The current ip address of this computer is: " + nip + ".");
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter SNode's ip: ");
		String snip = sc.nextLine();
		System.out.println("Please enter SNode's port: ");
		String snport = sc.nextLine();
		System.out.println("Please enter your port: ");
		String nport = sc.next();
		String showInfoFlag = "";
		Listener listener = new Listener(Integer.parseInt(nport), handler);

		try {

			TTransport  transport = new TSocket(snip, Integer.parseInt(snport));
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			SNodeService.Client client = new SNodeService.Client(protocol);

			//Try to connect
			transport.open();
			System.out.println("Listener starting.");
			listener.start();
			//Try to join DHT
			NodeInfo initInfo = client.join(nip, nport);

			System.out.println("The node has ip: " + initInfo.nodeIp);
			//If receive NACK, wait 1s and try again
		while (initInfo.nodeIp.equals("nack")) {
			Thread.sleep(1000*10l);
			initInfo = client.join(nip, nport);
		}
		//Once receive identifier and a node info from super node, start initiate its finger table
		handler.init(nip, nport, initInfo);
		System.out.println("Calling the post join fucntion now to end joining.");
		client.postJoin(nip, nport);
		while(!showInfoFlag.equals("exit")){
			System.out.println("You can type 'show' any time to see the information of the node.");
			System.out.println("You can type 'exit' any time to quit.");
			showInfoFlag = sc.nextLine();
			if(showInfoFlag.equals("show")){
				handler.showInfo();
			}
		}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
