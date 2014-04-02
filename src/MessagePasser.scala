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

object MessagePasser {

  def send_blocking(dstNode: UserNode, message:UserMessage) {
    actor {
            val remoteActor = select(Node(dstNode.getIP, dstNode.getPort), Symbol(dstNode.getName))
            
            println("Send blocking called")
            
            remoteActor !? message
            match {
                case mesg: UserMessage =>
                  mesg.getKind match {
                    case UPLOAD_ACK =>
                      println("Got UPLOAD_ACK")
                  }
            }
       }
  }
  
}