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




public class Listener extends Thread {

	private int nport ;
	private NodeService.Processor processor;
	private NodeServiceHandler handler;

	public Listener (int port, NodeServiceHandler handler){
		this.handler = handler;
		nport = port;
		System.out.println("Start to serve as a node in port: " + nport);
	}
	public void run(){
		System.out.println("Start running listener.");
		try {
			//Create Thrift server socket
			TServerTransport serverTransport = new TServerSocket(nport);
			TTransportFactory factory = new TFramedTransport.Factory();

			//Create service request handler
			processor = new NodeService.Processor(handler);

			//Set server arguments
			TServer.Args args = new TServer.Args(serverTransport);
			args.processor(processor);  //Set handler
			args.transportFactory(factory);  //Set FramedTransport (for performance)

			//Run server as a multithread server
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
			server.serve();

			} catch (Exception e) {
			e.printStackTrace();
			}

		}
}
