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
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter the super node's ip address:");
		String superNodeIp = sc.nextLine();
		System.out.println("Please enter tne super node's port: ");
		String serverPort = sc.nextLine();
		// To run server and client in different machine, please modify the locahost to IP address of the server machine
		TTransport  transport = new TSocket(superNodeIp, Integer.parseInt(serverPort));
		TProtocol protocol = new TBinaryProtocol(transport);
		SNodeService.Client client = new SNodeService.Client(protocol);
		// Try to connect
		transport.open();
		String flag= "start";
		String setMode = "input";
		String operationMode = "set";
		String inputDir;
		String bookName ;
		String bookGenre ;
		String bookWhole;
		String[] bookSplited;
		BufferedReader reader = null;
		NodeInfo lookNode;
		operResult res = new operResult();
		String logFlag = "";
		// Send input directory address and mode to server
		// call getNode() function here from the superNode to get a start node.
	//	lookNode  = client.getNode();
	//	System.out.println("The starting node that the superNode assgined to you has ip: " + lookNode.nodeIp + " and the port is: " + lookNode.nodePort + ".");
	//	TTransport  transportNode = new TSocket(lookNode.nodeIp, Integer.parseInt(lookNode.nodePort));
	//	TProtocol protocolNode = new TBinaryProtocol(transportNode);
	//	NodeService.Client clientNode = new NodeService.Client(protocolNode);
	//	transportNode.open();
		while(!flag.equals("exit")){
			lookNode  = client.getNode();
			System.out.println("The starting node that the superNode assgined to you has ip: " + lookNode.nodeIp + " and the port is: " + lookNode.nodePort + ".");
			TTransport  transportNode = new TSocket(lookNode.nodeIp, Integer.parseInt(lookNode.nodePort));
			TProtocol protocolNode = new TBinaryProtocol(transportNode);
			NodeService.Client clientNode = new NodeService.Client(protocolNode);
			transportNode.open();
			System.out.println("Select your operation (set/get): ");
			operationMode = sc.nextLine();
			boolean equalsSet = operationMode.equalsIgnoreCase("set");
			boolean equalsGet = operationMode.equalsIgnoreCase("get"); 
			System.out.println("Do you want to print the log file of your set/get operation? (enter 'y' if you want the log file.)");
			logFlag = sc.nextLine();
			while(! (equalsSet || equalsGet)){
				System.out.println("Wrong operation mode, please select again (set/get).");
				operationMode = sc.nextLine();
				equalsSet = operationMode.equalsIgnoreCase("set");
				equalsGet = operationMode.equalsIgnoreCase("get"); 
			}
			if(equalsSet){
				System.out.println("In set mode.");
				System.out.println("Select your set mode (file/input): ");
				setMode = sc.nextLine();
				boolean equalsFile = setMode.equalsIgnoreCase("file");
				boolean equalsInput = setMode.equalsIgnoreCase("input");
				while(! (equalsFile || equalsInput)){
					System.out.println("Wrong set mode, please select again (file/input)");
					setMode = sc.nextLine();
					equalsFile = setMode.equalsIgnoreCase("file");
					equalsInput = setMode.equalsIgnoreCase("input");
				}
				if(equalsFile){
					System.out.println("In file mode of setting.");
					System.out.println("Please enter the location of your file: ");
					inputDir = sc.nextLine();
					File f = new File(inputDir);
					while(!f.isFile()){
						System.out.println("What you have just entered is not a file, please enter again: ");
						inputDir = sc.nextLine();
						f = new File(inputDir);
					}
					try{
						reader = new BufferedReader(new FileReader(f));
						while((bookWhole = reader.readLine()) != null ){
							bookSplited = bookWhole.split(":");
							if(bookSplited.length < 2) {
								System.out.println("The grene of book <" + bookSplited[0] + "> is missing.");
								System.out.println("Set this book's genre to' missing'.");
								bookName = bookSplited[0];
								bookGenre = "missing";
							}
							else {
								bookName = bookSplited[0];
								bookGenre = bookSplited[1];
							}
							System.out.println("Setting the book: " + bookName + "of genre: " + bookGenre + ".");
							// call set function of node here:
							res = clientNode.setItem(bookName, bookGenre, "This set operation has visited these nodes:");
							if(logFlag.equals("y")) {
								System.out.println("This is the log information of setting <" + bookName +"> of genre " + bookGenre + " to the DHT is: ");
								System.out.println(res.log);
								System.out.println();
							}
							
						}
						System.out.println("Setting finished");
					}
					catch (Exception e){
						System.err.println("Something wrong with the input file, end setting.");
					}
					
					
				}
				else if(equalsInput){
					System.out.println("In input mode of setting");
					System.out.println("Please enter the book's name you are setting: ");
					bookName = sc.nextLine();
					System.out.println("Please enter the book's genre you are setting: ");
					bookGenre = sc.nextLine();
					System.out.println("Setting the book: " + bookName + "of genre: " + bookGenre + ".");
					// call set() function of node here:
					res = clientNode.setItem(bookName, bookGenre, "This set operation has visited these nodes:");
					if(logFlag.equals("y")) {
						System.out.println("This is the log information of setting <" + bookName +"> of genre " + bookGenre + " to the DHT is:");
						System.out.println(res.log);
						System.out.println();
					}
					System.out.println("Setting finished");
				}
			}
			if(equalsGet){
				System.out.println("In get mode.");
				System.out.println("Please enter the book's name: ");
				bookName = sc.nextLine();
				// call get() fucntion of node here
				res = clientNode.getItem(bookName, "This get operation has visited these nodes:");
				System.out.println("The genre of the book <"+ bookName + "> is: " + res.value + "." );
				if(logFlag.equals("y")) {
					System.out.println("This is the log information of getting <" + bookName +">'s genre out of the DHT is:");
					System.out.println(res.log);
					System.out.println();
				}

			}
			System.out.println("Please enter exit to quit the program, enter other things to continue operating.");
			flag = sc.nextLine();
			// Notice
			transportNode.close();
		}
		System.out.println("finished job!");
	} catch(TException e) {
	
        }

    }
}

