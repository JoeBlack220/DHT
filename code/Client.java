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
		String flag= "start";
		String setMode = "input";
		String operationMode = "set";
		System.out.println("Start testing!");
		Scanner sc = new Scanner(System.in);
		String inputDir;
		String bookName ;
		String bookGenre ;
		String bookWhole;
		String[] bookSplited;
		BufferedReader reader = null;
		NodeInfo lookNode;
		// call getNode() function here from the superNode to get a start node.

		// Send input directory address and mode to server
		while(!flag.equals("exit")){
			System.out.println("Select your operation (set/get): ");
			operationMode = sc.next();
			boolean equalsSet = operationMode.equalsIgnoreCase("set");
			boolean equalsGet = operationMode.equalsIgnoreCase("get"); 
			while(! (equalsSet || equalsGet)){
				System.out.println("Wrong operation mode, please select again (set/get).");
				operationMode = sc.next();
			}
			if(equalsSet){
				System.out.println("In set mode.");
				System.out.println("Select your set mode (file/input): ");
				setMode = sc.next();
				boolean equalsFile = setMode.equalsIgnoreCase("file");
				boolean equalsInput = setMode.equalsIgnoreCase("input");
				while(! (equalsFile || equalsInput)){
					System.out.println("Wrong set mode, please select again (file/input)");
					setMode = sc.next();
				}
				if(equalsFile){
					System.out.println("In file mode of setting.");
					System.out.println("Please enter the location of your file: ");
					inputDir = sc.next();
					File f = new File(inputDir);
					while(!f.isFile()){
						System.out.println("What you have just entered is not a file, please enter again: ");
						inputDir = sc.next();
						f = new File(inputDir);
					}
					try{
						reader = new BufferedReader(new FileReader(f));
						while((bookWhole = reader.readLine()) != null ){
							bookSplited = bookWhole.split(":");
							if(bookSplited.length < 2) {
								System.out.println("The grene part of book <" + bookSplited[0] + "> is missing.");
								System.out.println("Set this book's genre to missing.");
								bookName = bookSplited[0];
								bookGenre = "missing";
							}
							else {
								bookName = bookSplited[0];
								bookGenre = bookSplited[1];
							}
							System.out.println("Setting the book: " + bookName + "of genre: " + bookGenre + ".");
							// call set function of node here:
							System.out.println("Setting finished");
						}
					}
					catch (Exception e){
						System.err.println("Something wrong with the input file, end setting.");
					}
					
					
				}
				else if(equalsInput){
					System.out.println("In input mode of setting");
					System.out.println("Please enter the book's name you are setting: ");
					bookName = sc.next();
					System.out.println("Please enter the book's genre you are setting: ");
					bookGenre = sc.next();
					System.out.println("Setting the book: " + bookName + "of genre: " + bookGenre + ".");
					// call set() function of node here:
					System.out.println("Setting finished");
				}
			}
			if(equalsGet){
				System.out.println("In get mode.");
				System.out.println("Please enter the book's name: ");
				bookName = sc.next();
				// call get() fucntion of node here
				bookGenre = "";
				System.out.println("The genre of the book <"+ bookName + "> is: " + bookGenre + ".");
			}
			//NodeInfo testResult = client.join(ip, port);
			// Log final file score ranking and elapsed time
			//System.out.println(testResult.nodeIp);
			System.out.println("Please enter exit to quit the program, enter other things to continue operating.");
			flag = sc.next();
			// Notice
		}
		System.out.println("finished job!");
	} catch(TException e) {
	
        }

    }
}

