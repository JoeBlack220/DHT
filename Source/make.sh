thrift --gen java NodeInterface.thrift
thrift --gen java SNodeInterface.thrift
mv ./gen-java/* ./
javac -cp ".:/usr/local/Thrift/*" SNode.java -d .
javac -cp ".:/usr/local/Thrift/*" Client.java -d .
javac -cp ".:/usr/local/Thrift/*" Node.java -d .
