struct NodeInfo {
  1:string nodeIp,
  2:string nodePort,
  3:i32 nodeKey
}

service SNodeService {
  NodeInfo join(1: string ip,2: string port),
  bool postJoin(1: string ip, 2: string port),
  NodeInfo getNode() 
}

service NodeService {
  bool setItem(1: string bookTitle, 2: string gerne),
  string getItem(1: string bookTitle),
  bool UpdateDHT()
}
