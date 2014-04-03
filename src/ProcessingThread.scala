// Simple server
import java.net._
import java.io._
import scala.io._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor._
import javax.swing.ImageIcon
import scala.swing.Dialog
import java.awt.image.BufferedImage
import java.awt.Image
import javax.imageio.ImageIO
import java.awt.Graphics2D
import msgKind._
import scala.actors.remote.Node

class ProcessingThread(inNode: UserNode) extends Runnable {
  def run(){
    actor {
		alive(inNode.getPort)
		register(Symbol(inNode.getName), self)
		
		loop {
		  receive {	    
		    
		  		  case message : UserMessage =>{
		  		  val msgSender : UserNode = message.getSender
		  		  val remoteSender = select(Node(msgSender.getIP, msgSender.getPort), Symbol(msgSender.getName))
		  		  message.getKind match 
		  		  {
		  		    case IMG_UPLOAD =>
		  		      println("Message Kind received by "+inNode.getName+" is IMG_UPLOAD from "+ msgSender.getName)
		  		      val reply_message:UserMessage = new UserMessage(UPLOAD_ACK, "Image Received", Shutterbug.curnode)
		  		      Shutterbug.curnode.addToRefreshBuffer(message.getData.toString())
		  		      reply(reply_message)
		  		      
		  		    case THUMB_PUT =>
		  		      println("Message Kind received by "+inNode.getName+" is THUMB_PUT from "+msgSender.getName)
		  		      val reply_msg:UserMessage = new UserMessage(THUMB_ACK, "Thumbnail Received", Shutterbug.curnode)
		  		      Shutterbug.curnode.addToRefreshBuffer(message.getData.toString())
		  		      remoteSender ! reply_msg
		  		      
		  		    case THUMB_ACK =>
		  		      println("Received THUMB_ACK at "+inNode.getName+" sent by "+msgSender.getName)
		  		      
		  		    case UPLOAD_ACK =>
		  		      println("Message Kind received by "+inNode.getName+" is UPLOAD_ACK from "+msgSender.getName)
		  		      
		  		    case IMG_DELETE =>
		  		      println("Message Kind received by "+inNode.getName+" is IMG_DELETE from "+msgSender.getName)
		  		    
		  		  }
		  		  
		  		}
		  		case msg =>
		  		{
		  			println("Message found "+msg)
		  		}
				
			}
		}
	}
  }
  
}