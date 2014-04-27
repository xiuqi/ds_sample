import java.net._
import java.io._
import scala.io._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor._
import javax.swing.ImageIcon
import scala.swing.Dialog
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Graphics2D
import msgKind._
import scala.actors.remote.Node
import java.awt.Image

class ProcessingChatThread(inNode: UserNode) extends Runnable {
	def run(){
	  println("am here in pchattherad")
		actor {
			alive(Shutterbug.chat_port)
			register(Symbol(inNode.getName), self)
			loop {
				receive {	    
				case message : UserMessage =>{
					var msgSender : UserNode = message.getSender
					//var remoteSender = select(Node(msgSender.getIP, msgSender.getPort), Symbol(msgSender.getName))
							message.getKind match 
							{
							case CHAT =>
							Shutterbug.mcs.addToChatBuffer(message.getSender.getName, message.getGroup.getName,
							    message.getData.asInstanceOf[String])
							println("CHAT Message Kind received by "+inNode.getName+" is CHAT from "+ msgSender.getName)
					}

				}

				}
			}
		}
	}
}
