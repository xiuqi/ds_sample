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
import java.io.File
import java.util.HashMap
import java.util.ArrayList
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import scala.actors.TIMEOUT


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

	// Fetch the groups this node was part of here
	var grpMap: HashMap[String, ArrayList[String]] = File.readFromFile
	var grpMap1: HashMap[String, ArrayList[String]] = File.readFromFile

	if (grpMap != null)
	{

		println("Groups this node was part of is");

		var it = grpMap.entrySet().iterator();
		while (it.hasNext()) {
			var pairs = it.next();

			var ugroup:Group = new Group(pairs.getKey())
			Shutterbug.curnode.addToGroup(ugroup)
			Shutterbug.mcs.addGroup(ugroup)
			ugroup.setCreator(Shutterbug.curnode.getName)
			it.remove(); // avoids a ConcurrentModificationException
		}
		CreateGroup.setGroupNameList(Shutterbug.curnode.returnGroupName)
		println("Done here")
		var it1 = grpMap1.entrySet().iterator();
		while (it1.hasNext()) {
			var pairs1 = it1.next();
			var cur_grpname = pairs1.getKey()
					var tmp_nodes:ArrayList[String] = pairs1.getValue()
					var num_nodes:Int = tmp_nodes.size()
					var itr:Int = 0
					var transfer:Boolean = false

					println("Elements of this group are :")
					actor {
						while (itr < num_nodes && transfer == false)
						{
							if (Shutterbug.curnode.getName.equals(tmp_nodes.get(itr)))
							{
								itr = itr + 1
							}
							else
							{
								println("Querying"+tmp_nodes.get(itr))
								println("Group name = "+cur_grpname)
								val remoteActor = select(Node(Shutterbug.boot_ip, 10111), 'ragtime)
								val mes:BootMessage = new BootMessage(QUERY, tmp_nodes.get(itr), "", "",0, null)
								var ip:String = ""
								var port:Int = 0
								var found: Boolean = false

								remoteActor !? mes 
										match {
										case mesg: BootMessage =>
										mesg.getKind match {
										case BOOT_ACK =>
										ip = mesg.getIP
										port = mesg.getPort
										found = true
										case BOOTSTRAP_ERROR =>
										println("Error during query for : " + mesg.getData)
										

										}
								}
								
								if (found == true)
								{
								  var userActor = select(Node(ip, port), Symbol(tmp_nodes.get(itr)))
								  var inviteMes:UserMessage = new UserMessage(RELOGIN, cur_grpname, 
									Shutterbug.curnode, null, null, null)
								  	println("Found true for node "+tmp_nodes.get(itr))
								  	// Ask for group data here, set timeout of 4s
								  	userActor ! inviteMes
								  	receiveWithin(4000){
								    // Timeout, try with the next node
								    case TIMEOUT => 
								      println("Timeout during login and contacting "+tmp_nodes.get(itr))
								      itr = itr + 1
								      
								    // Got the group data, insert into group
								  	case mesg:UserMessage=>
								  	mesg.getKind match{
								  	case RELOGIN_DATA => 
								  	  println("Relogin data received")
								  	  Shutterbug.mcs.setBuffers(mesg.getData.asInstanceOf[InvitationData], cur_grpname)
								  	  transfer = true
								  	  File.updateFile
								  	
								  	}
								  }
								}
								else
								{
								  itr = itr + 1
								}
							}
						}// While
					}

					it1.remove(); // avoids a ConcurrentModificationException
		} 
	}


	// After fetching get the group related info

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
							Shutterbug.mcs.addInvitedUser(mesg.getSender.getName, mesg.getGroup.getName)
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