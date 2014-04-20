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
			var bufImg = picture.convertToBI(message.getData.asInstanceOf[ImageIcon])
			var hashVal = calculate_hash.md5_img(message.getFormat, bufImg)
			
			if (dstNode.getName.equals(Shutterbug.curnode.getName))
			{				
				val image:Image = message.getData.asInstanceOf[ImageIcon].getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)
				var thumbIcon = new ImageIcon(image)
				var thumbBufImg = picture.convertToBI(thumbIcon)
				var thumbHash = calculate_hash.md5_img(message.getFormat, thumbBufImg)
				
				ImageIO.write(bufImg, message.getFormat, new File("images/" +thumbHash+"."+message.getFormat));
				
				println("Thumbnail created")
				
				val mesg:UserMessage = new UserMessage(THUMB_PUT, thumbIcon, Shutterbug.curnode,grp, dstNode, message.getFormat)

				multicast_msg(dstNode, mesg, grp)
				//Shutterbug.curnode.addToRefreshBuffer(new ImageIcon(image))

				Shutterbug.mcs.addToRefreshBuffer(grp.getName, thumbIcon, dstNode, message.getFormat)

			}
			else
			{
				var remoteActor = select(Node(dstNode.getIP, dstNode.getPort), Symbol(dstNode.getName))
				var waitTime:Long = 5000
				var retryCount: Int = 0
				var sending: Boolean = false

				println("Send blocking called")

				while (sending == false)
				{
					remoteActor !? message
							match {
							case None => 
							retryCount = retryCount + 1

							// Retry 3 times, otherwise pronounce dead
							if (retryCount > 3)
							{
								// Destination Node dead, multicast this to everyone else
								println("Destination "+dstNode.getName+ " dead")
								multicast_dead(dstNode.getName, grp)
								var selectionNode:UserNode = grp.getNodeFromHash(hashVal)
								var selectionNode2:UserNode = grp.getSuccessor(selectionNode)
								println("New node selected: "+selectionNode.getName+" and "+selectionNode2.getName)
//								if (selNode.getName.equals(Shutterbug.curnode.getName))
//								{				
//				
//									ImageIO.write(bufImg, message.getFormat, new File("images/" +hashVal+"."+message.getFormat));
//
//									val image:Image = message.getData.asInstanceOf[ImageIcon].getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)
//									println("Thumbnail created")
//									val mesg:UserMessage = new UserMessage(THUMB_PUT, new ImageIcon(image), Shutterbug.curnode,grp, dstNode, message.getFormat)
//
//									multicast_msg(dstNode, mesg, grp)
//									//Shutterbug.curnode.addToRefreshBuffer(new ImageIcon(image))
//
//									Shutterbug.mcs.addToRefreshBuffer(grp.getName, mesg.getData.asInstanceOf[ImageIcon], dstNode, message.getFormat)
//								  
//								}
								//remoteActor = select(Node(selectionNode.getIP, selNode.getPort), Symbol(selNode.getName))
								//remoteActor = select(Node(selNode.getIP, selNode.getPort), Symbol(selNode.getName))
								//To be finished
								var img_upload:UserMessage = new UserMessage(IMG_UPLOAD, message.getData.asInstanceOf[ImageIcon], Shutterbug.curnode, 
								    grp, selectionNode, message.getFormat)
								println("Selected node is "+selectionNode2.getName())
        		  	
								var img_upload2:UserMessage = new UserMessage(IMG_UPLOAD, message.getData.asInstanceOf[ImageIcon], Shutterbug.curnode, 
								    grp, selectionNode2, message.getFormat)
        		  	
								// Send the Blocking message to the storers
								MessagePasser.send_blocking(selectionNode, img_upload, grp)
								MessagePasser.send_blocking(selectionNode2, img_upload2, grp)
							}

							case mesg: UserMessage =>
							mesg.getKind match {
							case UPLOAD_ACK =>
							println("Got UPLOAD_ACK in " + Shutterbug.curnode.getName + " from "+ mesg.getSender.getName)

							Shutterbug.mcs.addToRefreshBuffer(grp.getName, mesg.getData.asInstanceOf[ImageIcon], dstNode, message.getFormat)
							multicast_msg(dstNode, mesg, grp)
							sending = true

							}//case
					}//match
				}//while
				println("Returning backk")
			}//if-else
		}//actor
	}//function

	def multicast_msg(dstNode : UserNode, message : UserMessage, grp: Group) {
		// Fetch the group members for the group
		var members:ArrayList[UserNode] = grp.returnMembers

				var grpCount:Int = members.size()

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
						var remoteActor = select(Node(members.get(iter).getIP, members.get(iter).getPort), Symbol(members.get(iter).getName))
						var send_msg : UserMessage = new UserMessage(THUMB_PUT, message.getData.asInstanceOf[ImageIcon], Shutterbug.curnode, message.getGroup, dstNode, 
						    message.getFormat)

						// Send here 
						remoteActor ! send_msg
					}
					iter = iter + 1
				}
	}

	def multicast_dead(dstName : String, grp: Group) {
		// Fetch the group members for the group
		var members:ArrayList[UserNode] = grp.returnMembers

				var grpCount:Int = members.size()

				var iter:Int = 0

				while (iter < grpCount)
				{
					if (members.get(iter).getName.equals(dstName) || members.get(iter).getName.equals(Shutterbug.curnode.getName))
					{
						println("Not sending to node " + members.get(iter).getName)
					}
					else
					{
						println("Sending dead msg to node " + members.get(iter).getName)
						var remoteActor = select(Node(members.get(iter).getIP, members.get(iter).getPort), Symbol(members.get(iter).getName))

						var send_msg : UserMessage = new UserMessage(NODE_DEAD, dstName, Shutterbug.curnode, grp, null, null)

						// Send here 
						remoteActor ! send_msg
					}
					iter = iter + 1
				}
	}

}