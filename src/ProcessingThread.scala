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

class ProcessingThread(inNode: UserNode) extends Runnable {
  def run(){
    actor {
		alive(inNode.getPort)
		register(Symbol(inNode.getName), self)
		
		loop {
		  receive {	    
		    
		  		  case message : UserMessage =>{
		  		  println("Message #######")
		  		  message.getKind match 
		  		  {
		  		    case IMG_UPLOAD =>
		  		      println("Message Kind received by "+inNode.getName+"is Image Upload")
		  		      val reply_message:UserMessage = new UserMessage(UPLOAD_ACK, "Image Received", null )
		  		      reply(reply_message)
		  		     
		  		    
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