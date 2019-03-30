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
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
public class SNode {
    public static SNodeServiceHandler handler;
    public static SNodeService.Processor processor;

    public static void main(String [] args) {
        try {
            handler = new SNodeServiceHandler();
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
            int serverPort = 9998;
            
            //Create Thrift server socket
            TServerTransport serverTransport = new TServerSocket(serverPort);
            TTransportFactory factory = new TFramedTransport.Factory();

            //Create service request handler
            SNodeServiceHandler handler = new SNodeServiceHandler();
            processor = new SNodeService.Processor(handler);

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

