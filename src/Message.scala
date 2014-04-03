
object msgKind extends Enumeration{
    type msgKind = Value
    val IMG_UPLOAD, IMG_DELETE, UPLOAD_ACK, NORMAL_ACK, THUMB_PUT, THUMB_ACK = Value
  }
import msgKind._

class UserMessage(kind:msgKind, data: Object, sendNode:UserNode) extends Serializable {
  
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
  
}