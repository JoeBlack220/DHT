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
