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

class ProcessingThread(inNode: UserNode) extends Runnable {
	def run(){
		actor {
			alive(inNode.getPort)
			register(Symbol(inNode.getName), self)

			loop {
				receive {	    

				case message : UserMessage =>{
					var msgSender : UserNode = message.getSender
							var remoteSender = select(Node(msgSender.getIP, msgSender.getPort), Symbol(msgSender.getName))
							message.getKind match 
							{
							case IMG_UPLOAD =>

							println("Message Kind received by "+inNode.getName+" is IMG_UPLOAD from "+ msgSender.getName)

							val image:Image = message.getData.asInstanceOf[ImageIcon].getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)
							var thumbIcon = new ImageIcon(image)
							var thumbBufImg = picture.convertToBI(thumbIcon)
							var thumbHash = calculate_hash.md5_img(message.getFormat, thumbBufImg)
							var bufImg = picture.convertToBI(message.getData.asInstanceOf[ImageIcon])

							ImageIO.write(bufImg, message.getFormat, new File("images/" +thumbHash+"."+message.getFormat));

							println("Thumbnail created")

							val reply_message:UserMessage = new UserMessage(UPLOAD_ACK, thumbIcon,
							    Shutterbug.curnode, message.getGroup, message.getStorer, message.getFormat)
							
							//var reply_message:UserMessage = new UserMessage(UPLOAD_ACK,
							//"Image Received", Shutterbug.curnode, message.getGroup, message.getStorer)
							// Convert to thumbnail and add to refresh buffer here
							
							Shutterbug.mcs.addToRefreshBuffer(message.getGroup.getName, thumbIcon,
							    message.getStorer, message.getFormat)
							reply(reply_message)

							case THUMB_PUT =>
								println("Message Kind received by "+inNode.getName+" is THUMB_PUT from "+msgSender.getName)
								var reply_msg:UserMessage = new UserMessage(THUMB_ACK, message.getData.asInstanceOf[ImageIcon],
								    Shutterbug.curnode, message.getGroup, message.getStorer, message.getFormat)
								Shutterbug.mcs.addToRefreshBuffer(message.getGroup.getName, message.getData.asInstanceOf[ImageIcon],
								    message.getStorer, message.getFormat)
							
								remoteSender ! reply_msg

							case THUMB_ACK =>
							println("Received THUMB_ACK at "+inNode.getName+" sent by "+msgSender.getName)

							case UPLOAD_ACK =>
							println("Message Kind received by "+inNode.getName+" is UPLOAD_ACK from "+msgSender.getName)

							case DEL_IMG =>
							println("Message Kind received by "+inNode.getName+" is IMG_DELETE from "+msgSender.getName)
							//TODO : Send group name also
							Shutterbug.mcs.processDelete(message.getData.asInstanceOf[ImageIcon], message.getGroup.getName)
							
							case INVITATION =>
							var result =  Dialog.showConfirmation(null, "Invitation received from " + message.getSender.getName+
							      "\nDo you want to accept?", "Invitation")
							      
							if (result == Dialog.Result.Yes)
							{
							  var re_msg:UserMessage = new UserMessage(INV_YES_ACK, "Accepted", Shutterbug.curnode, message.getGroup,
							      null, null)
							  reply(re_msg)
							  
							}
							else
							{
							  var re_msg:UserMessage = new UserMessage(INV_NO_ACK, "Rejected", Shutterbug.curnode, message.getGroup,
							      null, null)
							  reply(re_msg)
							}
							
							case INV_DATA =>
							  println("Inv Data received")
							  var new_grp:Group = message.getGroup
							  Shutterbug.curnode.addToGroup(new_grp)
							  Shutterbug.mcs.setBuffers(message.getData.asInstanceOf[InvitationData], new_grp.getName)
							  
							case NEW_MEMBER =>
							  // TODO: Check for same group name and creator
							  var req_grp:Group = Shutterbug.curnode.returnGroupFromName(message.getGroup.getName, 
							      message.getGroup.getCreator)
							  req_grp.addMembers(message.getData.asInstanceOf[UserNode].getName, message.getData.asInstanceOf[UserNode])

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