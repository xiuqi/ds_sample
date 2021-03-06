
object msgKind extends Enumeration{
    type msgKind = Value
    val IMG_UPLOAD, UPLOAD_ACK, NORMAL_ACK, 
    THUMB_PUT, THUMB_ACK, NODE_DEAD, DEL_IMG, INVITATION, 
    INV_YES_ACK, INV_NO_ACK, INV_DATA, NEW_MEMBER, IMG_REQ, 
    IMG_REP, RELOGIN, RELOGIN_DATA,NEW_CAPTION,IMG_GET,IMG_GET_ACK, 
    IMG_GET_NOACK,CHAT = Value
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