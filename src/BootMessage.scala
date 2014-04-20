object bootMsgKind extends Enumeration{
    type bootMsgKind = Value
    val REGISTER, QUERY, LOGIN, BOOTSTRAP_ERROR, LOGOFF, BOOT_ACK = Value
  }
import bootMsgKind._

@SerialVersionUID(14l)
class BootMessage(kind:bootMsgKind, username:String, password:String, ip: String, port:Int, data:Object) extends Serializable {
  
  def getKind() :bootMsgKind = 
  {
    return kind;
  }
  
  def getData() : Object = 
  {
    return data;
  }
  
  def getIP() : String = 
  {
    return ip;
  }
  
   def getPort() : Int = 
  {
    return port;
  }
   
    def getUserName() : String = 
  {
    return username;
  }
  def getPassword() : String = 
  {
    return password;
  }
  
}