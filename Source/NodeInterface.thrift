struct NodeInfo {
  1:string nodeIp,
  2:string nodePort,
  3:i32 nodeKey
}

struct operResult{
  1: string value,
  2: string log
}

service NodeService {
  operResult setItem(1: string bookTitle, 2: string gerne, 3: string prevLog),
  operResult getItem(1: string bookTitle, 2: string prevLog),
  void updateDHT(1: NodeInfo n, 2: i32 i),
  NodeInfo getNodeSet(),
  void setPred(1: NodeInfo n),
  NodeInfo findSucc(1: i32 nid),
  NodeInfo closetPrecedingFinger(1: i32 n),
  void setSucc(1: NodeInfo n),
  NodeInfo getSucc(),
  NodeInfo getPred()

}
