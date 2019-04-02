import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import java.io.*;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NodeServiceHandler implements NodeService.Iface {

	int ftSize = 5;
	private ArrayList<NodeInfo> ftNode = new ArrayList<>();
	private ArrayList<Integer> ftStart = new ArrayList<>();
	private NodeInfo pred = new NodeInfo();
	private NodeInfo succ = new NodeInfo();
	private NodeInfo curr = new NodeInfo();
	private HashMap<String, String> database = new HashMap<String, String>();

	public void init(String nip, String nport, NodeInfo initInfo) throws TException{
    	//Set current node info
		curr.nodeKey = initInfo.nodeKey;
		curr.nodeIp = nip;
		curr.nodePort = nport;
		//Calculate the start of finger table
		for (int i = 1; i <= ftSize; i ++) {
    			ftStart.add( (int) ((curr.nodeKey + Math.pow(2, i - 1) ) % Math.pow(2, ftSize)) );
    		}
    	//If there is no node in DHT
    	if (initInfo.nodeIp.equals("")) {
    		//Set predecessor and successor to itself
		pred.nodeKey = initInfo.nodeKey;
		pred.nodeIp = nip;
		pred.nodePort = nport;
		succ.nodeKey = initInfo.nodeKey;
		succ.nodeIp = nip;
		succ.nodePort = nport;
		System.out.println(initInfo.nodeKey);
		//Set the node of finger table
    		for (int i = 1; i <= ftSize; i ++) {
    			ftNode.add(curr);
    		}
    	} else {
		System.out.println(initInfo.nodeIp+ " " + initInfo.nodePort);
    		initFT(initInfo.nodeIp, initInfo.nodePort);
		System.out.println("The finger table has been created, they are: ");
		for(NodeInfo nodeS : ftNode ){
			System.out.println(nodeS.nodeKey);
		}
    		updateOthers(initInfo.nodeIp, initInfo.nodePort);
    	}

    }
	private void initFT(String nip, String nport) throws TException{
	    	TTransport  transport = new TSocket(nip, Integer.parseInt(nport));
		TBinaryProtocol protocol = new TBinaryProtocol(transport);
		NodeService.Client client = new NodeService.Client(protocol);
		System.out.println("Current node key is: " + curr.nodeKey);
		transport.open();
		NodeInfo mySucc = client.findSucc(ftStart.get(0));
		System.out.println("The curr of tmp is " + mySucc.nodeKey + ".");
	    	TTransport  transport2 = new TSocket(mySucc.nodeIp, Integer.parseInt(mySucc.nodePort));
		TBinaryProtocol protocol2 = new TBinaryProtocol(transport2);
		NodeService.Client client2 = new NodeService.Client(protocol2);
		transport2.open();
		pred = client2.getPred();
		System.out.println("MY PRED IS " + pred.nodeKey + ".");
		succ = mySucc;
		client2.setPred(curr);
		ftNode.add(mySucc);	
	    	TTransport  transport1 = new TSocket(pred.nodeIp, Integer.parseInt(pred.nodePort));
		TBinaryProtocol protocol1 = new TBinaryProtocol(transport1);
		NodeService.Client client1 = new NodeService.Client(protocol1);
		transport1.open();
		client1.setSucc(curr);
		int currKey = curr.nodeKey;
		for (int i = 1; i < ftSize; i ++) {
			int ftKey = ftNode.get(i - 1).nodeKey;
			System.out.println("previous key is " + ftKey + " current start is " + ftStart.get(i));
			if (inRange(currKey, ftKey, ftStart.get(i), true, false)) {
				ftNode.add(ftNode.get(i - 1));
			} else {
				System.out.println("finding this start now: " + ftStart.get(i));
				NodeInfo tmp = client.findSucc(ftStart.get(i));
				System.out.println("found this start now: " + tmp);
				ftNode.add(tmp);
			}
		}
		System.out.println("complete");
	}
	private void updateOthers(String nip, String nport) throws TException{
	    	for (int i = 1; i <= ftSize; i ++) {
			int tmp = (int) ((curr.nodeKey - Math.pow(2, i - 1)) % Math.pow(2, ftSize));
			if(tmp < 0) tmp += (int) Math.pow(2, ftSize);
			System.out.println(tmp);
			NodeInfo p;
	    		p = findPred(tmp, true);
			System.out.println("p key is " + p.nodeKey);
			System.out.println("p ip is " + p.nodeIp);
			System.out.println("p port is " + p.nodePort);
			TTransport  transport2 = new TSocket(p.nodeIp, Integer.parseInt(p.nodePort));
			TBinaryProtocol protocol2 = new TBinaryProtocol(transport2);
			NodeService.Client client2 = new NodeService.Client(protocol2);	    
			transport2.open();	
			client2.updateDHT(curr, i);
	    	}
	}
	public NodeInfo findSucc(int nid) throws TException {
		System.out.println("Start finding successor of key: " + nid + ".");    	
		NodeInfo tmp = findPred(nid, false);
		System.out.println("The current of predecessor of the key is: " + tmp.nodeKey + ".");
	    	TTransport  transport1 = new TSocket(tmp.nodeIp, Integer.parseInt(tmp.nodePort));
		TBinaryProtocol protocol1 = new TBinaryProtocol(transport1);
		NodeService.Client client1 = new NodeService.Client(protocol1);
		transport1.open();
		NodeInfo tmpSucc = client1.getSucc();
		System.out.println("The successor node's key of the key: " + nid + " is: "+ tmpSucc.nodeKey);
		return tmpSucc;
	}
	private NodeInfo findPred(int nid, boolean update) throws TException{
	    	NodeInfo tmp = curr;
	    	TTransport  transport1 = new TSocket(tmp.nodeIp, Integer.parseInt(tmp.nodePort));
		TBinaryProtocol protocol1 = new TBinaryProtocol(transport1);
		NodeService.Client client1 = new NodeService.Client(protocol1);
		transport1.open();
		NodeInfo tmpSucc = client1.getSucc();
		int succKey = tmpSucc.nodeKey;
		int currKey = tmp.nodeKey;
	    	while (!inRange(currKey, succKey, nid, false, true)) {
			if (tmp.nodeKey != curr.nodeKey) {
		    		String tmpIp = tmp.nodeIp;
		    		String tmpPort = tmp.nodePort;
			    	TTransport  transport = new TSocket(tmpIp, Integer.parseInt(tmpPort));
				TBinaryProtocol protocol = new TBinaryProtocol(transport);
				NodeService.Client client = new NodeService.Client(protocol);
				transport.open();
				tmp = client.closetPrecedingFinger(nid);
				succKey = client.getSucc().nodeKey;
				currKey = tmp.nodeKey;
			} 
			else {
				tmp = closetPrecedingFinger(nid);
			    	TTransport  transport = new TSocket(tmp.nodeIp, Integer.parseInt(tmp.nodePort));
				TBinaryProtocol protocol = new TBinaryProtocol(transport);
				NodeService.Client client = new NodeService.Client(protocol);
				transport.open();
				succKey = client.getSucc().nodeKey;
				currKey = tmp.nodeKey;
			}
		}

		if (update) {
			String tmpIp = tmp.nodeIp;
			String tmpPort = tmp.nodePort;
			TTransport  transport = new TSocket(tmpIp, Integer.parseInt(tmpPort));
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			NodeService.Client client = new NodeService.Client(protocol);
			transport.open();
			tmpSucc = client.getSucc();
			if (tmpSucc.nodeKey == nid) return tmpSucc;
		} 
		return tmp;
	}
	public NodeInfo closetPrecedingFinger(int nid) throws TException {
		//System.out.println("This is closet preceding finger: "+ nid);
		int currKey = curr.nodeKey;
	    	for (int i = ftSize - 1; i >= 0; i --) {
			int ftKey = ftNode.get(i).nodeKey;
			//System.out.println("this is: " + currKey + ftKey + nid);
	    		if (inRange(currKey, nid, ftKey, false, false)) {
	    			return ftNode.get(i);
	    		}
	    	}
	    	return curr;
	}

	@Override
	public NodeInfo getNodeSet () throws TException { 
		//add this to interface
	    	return curr;
	}

        @Override
	public void setPred (NodeInfo n) throws TException { 
		//add this to interface
    		pred = n;
		System.out.println("my id is "+ curr.nodeKey + "my pred is " + pred.nodeKey);
    	}
	//@Override
	public void setSucc (NodeInfo n) throws TException { 
		//add this to interface
    		succ = n;
		ftNode.set(0, n);
		System.out.println("my id is "+ curr.nodeKey + "my succ is " + succ.nodeKey);
    	}
	public NodeInfo getSucc() throws TException {
		return succ;
	}
	public NodeInfo getPred() throws TException {
		System.out.println("getPred working!!! my pred is " + pred);
		return pred;
	}

	@Override
	public operResult setItem(String bookTitle, String genre, String prevLog) throws TException {
		int itemKey = getKey(bookTitle, ((int)Math.pow(2,ftSize))+"");
		System.out.println("Now this node is using to set the book <" + bookTitle + "> with the key: " + itemKey + ", into the DHT.");		
		operResult ret = new operResult();
		prevLog = prevLog + " node with key " + curr.nodeKey;	
		ret.log = prevLog;
		ret.value = bookTitle;
		NodeInfo loNode;
		NodeInfo hiNode;
		NodeInfo nextNode = pred;
		if(inRange(pred.nodeKey, curr.nodeKey, itemKey, true, false)) {
			setItemHelper(bookTitle, genre);
			return ret;
		}
		else{
			for(int i = 0; i < ftSize; i++){
				if(i == 0) {
					loNode = curr;
					hiNode = ftNode.get(i);
					int tempKey = hiNode.nodeKey;
					if (tempKey < curr.nodeKey) tempKey += (int) Math.pow(2, ftSize);
					if(tempKey >= itemKey ){
						nextNode = hiNode;
						break;
					}
				}
				else {
					loNode = ftNode.get(i - 1);
					hiNode = ftNode.get(i);
				}
				if(inRange(loNode.nodeKey, hiNode.nodeKey, itemKey, false, true)) {
					nextNode = loNode;
					break;
				}
				else if(i == 4) nextNode = hiNode;
			}
			TTransport  transport = new TSocket(nextNode.nodeIp, Integer.parseInt(nextNode.nodePort));
		    	TBinaryProtocol protocol = new TBinaryProtocol(transport);
		    	NodeService.Client client = new NodeService.Client(protocol);
		   	//Try to connect
		   	transport.open();
			return client.setItem(bookTitle, genre, ret.log);
		}

	}

	private void setItemHelper (String bookTitle, String bookGenre){
		database.put(bookTitle, bookGenre);
	}
	@Override
	public operResult getItem(String bookTitle, String prevLog) throws TException {
		int itemKey = getKey(bookTitle, ((int)Math.pow(2,ftSize))+"");
		System.out.println("Now this node is using to set the book <" + bookTitle + "> with the key: " + itemKey + ", into the DHT.");	
		prevLog = prevLog + " node with key " + curr.nodeKey;	
		operResult ret = new operResult();
		NodeInfo loNode;
		NodeInfo hiNode;
		NodeInfo nextNode = pred;
		if(inRange(pred.nodeKey, curr.nodeKey, itemKey, true, false)) {					
			ret.value = getItemHelper(bookTitle);
			ret.log = prevLog;
			return ret;
		}
		else{
			for(int i = 0; i < ftSize; i++){
				if(i == 0) {
					loNode = curr;
					hiNode = ftNode.get(i);
					int tempKey = hiNode.nodeKey;
					if (tempKey < curr.nodeKey) tempKey += (int) Math.pow(2, ftSize);
					if(tempKey >= itemKey ){
						nextNode = hiNode;
						break;
					}
				}
				else {
					loNode = ftNode.get(i - 1);
					hiNode = ftNode.get(i);
				}
				if(inRange(loNode.nodeKey, hiNode.nodeKey, itemKey, false, true)) {
					nextNode = loNode;
					break;
				}
				else if(i == 4) nextNode = hiNode;
			}
			TTransport  transport = new TSocket(nextNode.nodeIp, Integer.parseInt(nextNode.nodePort));
		    	TBinaryProtocol protocol = new TBinaryProtocol(transport);
		    	NodeService.Client client = new NodeService.Client(protocol);
		   	//Try to connect
		   	transport.open();
			return client.getItem(bookTitle, prevLog);
		}
	}
	
	private String getItemHelper(String bookTitle){
		if(database.containsKey(bookTitle)) return database.get(bookTitle);
		else return "Sorry, not found the genre of the book <" + bookTitle + ">.";
		
	}
	private boolean inRange(int a, int b, int c, boolean front, boolean back) {
		if (a == b) {
			return true;
		} else if (front && back) {
			if ((c >= a && c <= b) || (c >= a && a > b) || (a > b && c <= a && c <= b)) return true;
			return false;
		} else if (front) {
			if ((c >= a && c < b) || (c >= a && a > b) || (a > b && c <= a && c < b)) return true;
			return false;
		} else if (back) {
			if ((c > a && c <= b) || (c > a && a >= b) || (a > b && c < a && c <= b)) return true;
			return false;
		} else {
			if ((c > a && c < b) || (c > a && a > b) || (a > b && c < a && c < b)) return true;
			return false;
		}	
	}

	public int getKey(String input, String range){
		try {
			int tempKey = 0;
			String tempKeyString = "";			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			BigInteger key = no.mod(new BigInteger (range));
			tempKeyString = key.toString(10);
			tempKey = Integer.parseInt(tempKeyString);
			return tempKey;
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateDHT(NodeInfo n, int i) throws TException { // change params
		System.out.println("i am " + curr.nodeKey + "i am updating dht");
		int currKey = curr.nodeKey;
		int targetKey = ftNode.get(i - 1).nodeKey;
		if (inRange(currKey, targetKey, n.nodeKey, false, false)) {
			if (i == 1) {
			    	TTransport  transport1 = new TSocket(n.nodeIp, Integer.parseInt(n.nodePort));
				TBinaryProtocol protocol1 = new TBinaryProtocol(transport1);
				NodeService.Client client1 = new NodeService.Client(protocol1);
				transport1.open();
				succ = client1.getSucc();
			}
			System.out.println("Updating the DHT.");
			ftNode.set(i - 1, n);
		    	TTransport  transport = new TSocket(pred.nodeIp, Integer.parseInt(pred.nodePort));
			TBinaryProtocol protocol = new TBinaryProtocol(transport);
			NodeService.Client client = new NodeService.Client(protocol);
			transport.open();
			client.updateDHT(n, i);
		}
		System.out.println("The finger table has been updated, they are: ");
		for(NodeInfo nodeS : ftNode ){
			System.out.println(nodeS.nodeKey);
		}
	}


	public void showInfo(){
		System.out.println("Now show all the information fo the node.");
		System.out.println("The node has ip: " + curr.nodeIp + " has port: " + curr.nodePort + " has key: "+ curr.nodeKey + ".");
		System.out.println("The successor of the node has ip: " + succ.nodeIp + " has port: " + succ.nodePort + " has key: " + succ.nodeKey + ".");
		System.out.println("The predecessor of the node has ip: " + pred.nodeIp + " has port: " + pred.nodePort + " has key: " + pred.nodeKey + ".");
		System.out.println("The database of the node now has " + database.size() + " books and their genres information.");
		System.out.println("They are listed below: ");
		for(Map.Entry<String, String> entry : database.entrySet()){
			System.out.println("The book with name <" + entry.getKey() + "> has genre: " + entry.getValue() + ".");
		}
		System.out.println("Now printing the finger table:");
		for(int i = 0; i < ftNode.size() ; i++){
			System.out.println("Number " + i + " entry of the finger table has ip: " + ftNode.get(i).nodeIp + " has port: " + ftNode.get(i).nodePort + " has key:" + ftNode.get(i).nodeKey);
		}
	}
}
