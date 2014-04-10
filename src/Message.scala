
object msgKind extends Enumeration{
    type msgKind = Value
    val IMG_UPLOAD, UPLOAD_ACK, NORMAL_ACK, THUMB_PUT, THUMB_ACK, NODE_DEAD, DEL_IMG = Value
  }
import msgKind._

class UserMessage(kind:msgKind, data: Object, sendNode:UserNode, groupBelongs : Group, storer:UserNode, format:String) extends Serializable {
  
  def getKind() : msgKind = 
  {
    return kind;
  }
  
  def getData() : Object = 
  {
    return data;
  }
  
  def getSender() : UserNode = 
  {
    return sendNode;
  }
  
   def getStorer() : UserNode = 
  {
    return storer;
  }
   
    def getFormat() : String = 
  {
    return format;
  }
  def getGroup() : Group = 
  {
    return groupBelongs;
  }
  
}