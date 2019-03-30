import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Scanner;
public class Client {
    public static void main(String [] args) {
	 try {
		int serverPort = 9998;
		// To run server and client in different machine, please modify the locahost to IP address of the server machine
		TTransport  transport = new TSocket("localhost", serverPort);
		TProtocol protocol = new TBinaryProtocol(transport);
		SNodeService.Client client = new SNodeService.Client(protocol);
		// Try to connect
		transport.open();
		int flag = 1;
		System.out.println("Start testing!");
		Scanner sc = new Scanner(System.in);
		String ip;
		String port;
		// Send input directory address and mode to server
		while(flag != 0){
			System.out.println("Please enter your ip:");
			ip = sc.next();
			System.out.println("Please enter your port:");
			port = sc.next();
			NodeInfo testResult = client.join(ip, port);
			// Log final file score ranking and elapsed time
			System.out.println(testResult.nodeIp);
			System.out.println("Please choose the next mode:(0 for exit)");
			flag = sc.nextInt();
		// Notice
		}
		System.out.println("finished job!");
	} catch(TException e) {
	
        }

    }
}

