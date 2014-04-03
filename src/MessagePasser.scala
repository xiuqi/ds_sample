import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import javax.swing.ImageIcon
import java.awt.Image
import javax.imageio.ImageIO
import java.io.File
import java.awt.image.RenderedImage
import java.awt.image.BufferedImage
import msgKind._
import java.util.ArrayList

object MessagePasser {

  def send_blocking(dstNode: UserNode, message:UserMessage, grp: Group) {
    actor {
    		if (dstNode.getName.equals(Shutterbug.curnode.getName))
    		{
    		  multicast_msg(dstNode, message, grp)
    		  Shutterbug.curnode.addToRefreshBuffer(message.getData.toString())
    		  
    		}
    		else
    		{
    		
    		val remoteActor = select(Node(dstNode.getIP, dstNode.getPort), Symbol(dstNode.getName))
            
            println("Send blocking called")
            
            remoteActor !? message
            match {
                case mesg: UserMessage =>
                  mesg.getKind match {
                    case UPLOAD_ACK =>
                      println("Got UPLOAD_ACK in " + Shutterbug.curnode.getName + " from "+ mesg.getSender.getName)
                      
                      Shutterbug.curnode.addToRefreshBuffer(message.getData.toString())
                      multicast_msg(dstNode, message, grp)
                      
                  }
            }
            println("Returning backk")
    		}
       }
  }
  
  def multicast_msg(dstNode : UserNode, message : UserMessage, grp: Group) {
    // Fetch the group members for the group
    val members:ArrayList[UserNode] = grp.returnMembers
    
    val grpCount:Int = members.size()
    
    var iter:Int = 0
    
    while (iter < grpCount)
    {
      if (members.get(iter).getName.equals(dstNode.getName) || members.get(iter).getName.equals(Shutterbug.curnode.getName))
      {
        println("Not sending to node " + members.get(iter).getName)
      }
      else
      {
        println("Sending to node " + members.get(iter).getName)
        val remoteActor = select(Node(members.get(iter).getIP, members.get(iter).getPort), Symbol(members.get(iter).getName))
        val send_msg : UserMessage = new UserMessage(THUMB_PUT, message.getData, Shutterbug.curnode)
        
        // Send here 
        remoteActor ! send_msg
      }
      iter = iter + 1
    }
  }
  
}