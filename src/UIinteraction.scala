import java.util.logging.SimpleFormatter
import java.util.logging.FileHandler
import java.io.IOException
import bootMsgKind._
import msgKind._
import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.swing.Dialog
import javax.swing.ImageIcon


object UIinteraction {
	def onLogin( ip: String, port: Int, name: String ){
	  var unode:UserNode = new UserNode(ip,port,name)
	  Shutterbug.curnode = unode
	  try {  

        // This block configure the logger with handler and formatter  
        Shutterbug.fh = new FileHandler(Shutterbug.curnode.getName+".log");  
        Shutterbug.logger.addHandler(Shutterbug.fh);
        Shutterbug.logger.setUseParentHandlers(false)
        
        var formatter:SimpleFormatter = new SimpleFormatter();  
        Shutterbug.fh.setFormatter(formatter);  

        // the following statement is used to log any messages  
        Shutterbug.logger.info("My first log");  

      }
      
      catch  {  
      case e:SecurityException => e.printStackTrace();  
    
      case ioe: IOException =>  ioe.printStackTrace();  
      } 
      var pthread: ProcessingThread = new ProcessingThread(Shutterbug.curnode)
      println("Started for node "+Shutterbug.curnode.getName+"...")
      pthread.run
      
	}
	
	def onCreateGroup(name:String){
	  var ugroup:Group = new Group(Shutterbug.curnode.getName+"/"+name)
	  ugroup.addMembers(Shutterbug.curnode.getName, Shutterbug.curnode)
	  Shutterbug.curnode.addToGroup(ugroup)
	  Shutterbug.mcs.addGroup(ugroup)
	  ugroup.setCreator(Shutterbug.curnode.getName)
	  CreateGroup.setGroupNameList(Shutterbug.curnode.returnGroupName)
	}
	
	
	RemoteActor.classLoader = getClass().getClassLoader()
	def onInviteUser(name:String){
		actor{
			val remoteActor = select(Node(Shutterbug.boot_ip, 10111), 'ragtime)
			val mes:BootMessage = new BootMessage(QUERY, name, "", "",0, null)
			var ip:String = ""
			var port:Int = 0
			var inv_group:Group = Shutterbug.curGroup
			println("Ready to query")
					remoteActor !? mes 
							match {
							case mesg: BootMessage =>
							mesg.getKind match {
							case BOOT_ACK =>
							ip = mesg.getIP
							port = mesg.getPort
							case BOOTSTRAP_ERROR =>
							println("Error: " + mesg.getData)
							return

							}
					}
					if(ip.length() == 0){
						println("IP Address empty")
						return
//						return -1
					}

					val userActor = select(Node(ip, port), Symbol(name))
					var inviteMes:UserMessage = new UserMessage(INVITATION, inv_group.getName, 
					    Shutterbug.curnode, inv_group, null, null)
					userActor !? inviteMes
					match{
					  case mesg:UserMessage=>
					    mesg.getKind match{
					      case INV_YES_ACK =>
					        var newUser:UserNode = new UserNode(ip, port,name)
					        inv_group.addMembers(name, newUser)
					        
					        var inv_data:InvitationData = new InvitationData
					        // Get refresh and display buf
					        Shutterbug.mcs.getBuffers(inv_group.getName, inv_data)
					        // Get members
					        inv_group.returnMembersMap(inv_data)
					        
					        var inv_msg:UserMessage = new UserMessage(INV_DATA, inv_data, Shutterbug.curnode, inv_group, 
					            null, null)
					        
					        //TODO: Wait for ack here
					        userActor ! inv_msg
					         println("sent inv data from ui")
					        Shutterbug.mcs.multicastNewMember(newUser, inv_group)
					        println("Multicast over back in ui")
					        Dialog.showMessage(null, "User successfully added")
					        
					      case INV_NO_ACK =>
					        println("User declined request")
					        Dialog.showMessage(null, name+" User declined request", "Declined")
					    }
					}

		}
//		return 0
				//Query bootstrap IP port
				//if not, display error
				//invite message to user(blocking)
	  //yes: 
	  //Create new node
	  //Add to the list send refresh and display buffer
	  //TODO  Redistribution
	}
	
	
	def onUpload(filepath:String){
	  var in_msg : String = filepath
        		  	
        		  	var img = new ImageIcon(in_msg)
        		  	var format = picture.getPicFormat(in_msg)
        		  	var hash_val = calculate_hash.md5_img(format, picture.convertToBI(img))
        		  	var group = Shutterbug.curGroup
        		  	var curnode = Shutterbug.curnode
        		  	println("Hash value for message "+hash_val)
        		  	
        		  	var selectionNode:UserNode = Shutterbug.curGroup.getNodeFromHash(hash_val)
        		  	// Detect the group which this node is in while sending the message here
        		  	
        		  	println("Selected node is "+selectionNode.getName())
        		  	
        		  	var img_upload:UserMessage = new UserMessage(IMG_UPLOAD, img, curnode, group, selectionNode, format)
        		  	          
        		  	var selectionNode2:UserNode = group.getSuccessor(selectionNode)
        		  	println("Selected node is "+selectionNode2.getName())
        		  	
        		  	var img_upload2:UserMessage = new UserMessage(IMG_UPLOAD, img, curnode, group, selectionNode2, format)
        		  	
        		  	// Send the Blocking message to the storers
        		  	MessagePasser.send_blocking(selectionNode, img_upload, group)
        		  	MessagePasser.send_blocking(selectionNode2, img_upload2, group)
	}
}