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
import java.util.Iterator
import java.util.Map
import java.util.Set
import scala.actors.TIMEOUT
import java.awt.Image
import javax.imageio.ImageIO


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
	
	 var pchatThread: ProcessingChatThread = new ProcessingChatThread(Shutterbug.curnode)
      println("Started chat thread for node "+Shutterbug.curnode.getName+"...")
      pchatThread.run


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
								  	  
								  	  var new_grp:Group = mesg.getGroup
								  	  
								  	  var my_grp = Shutterbug.curnode.getGroupFromName(new_grp.getName)
								  	  
								  	  my_grp.setMembers(new_grp.returnMembers)
								  	  
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
							Dialog.showMessage(null, name+ "User is unavailable", "User not available")
							return 

							}
					}
					if(ip.length() == 0){
						println("IP Address empty")
						Dialog.showMessage(null, name+ "User is unavailable", "User not available")
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
	
	val image:Image = img.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)
	var thumbIcon = new ImageIcon(image)
	var thumbBufImg = picture.convertToBI(thumbIcon)
	var hash_val = calculate_hash.md5_img(format, thumbBufImg)
	
	//var hash_val = calculate_hash.md5_img(format, picture.convertToBI(img))
	
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
		
	def onImageSelect(img:ImageIcon) : LookupMsg ={
			var lkpMsg:LookupMsg = new LookupMsg()
			var imgHash=calculate_hash.md5_img("png", picture.convertToBI(img))
			lkpMsg=Shutterbug.mcs.getImageLookup(Shutterbug.curGroup.getName, imgHash)
			return lkpMsg;
	 	
	}
	
	def onSaveCaption(caption:String,img:ImageIcon) {
		var imgHash=calculate_hash.md5_img("png", picture.convertToBI(img))
		Shutterbug.mcs.addImageCaption(Shutterbug.curGroup.getName, imgHash, caption)
		Shutterbug.mcs.multicastImageCaption(caption, imgHash, Shutterbug.curGroup)
	  
	}
	

	def onDoubleClick(thumb:ImageIcon){
	    var thumbHash=calculate_hash.md5_img("png", picture.convertToBI(thumb))
	    var img_group:Group=Shutterbug.curGroup
	    //Get the holder of the image
	    var lkpMsg:LookupMsg = new LookupMsg()
	    lkpMsg=Shutterbug.mcs.getImageLookup(Shutterbug.curGroup.getName, thumbHash)
	    var holderList:ArrayList[String]=lkpMsg.getHolder
	    var format=lkpMsg.getFormat
	    var img:ImageIcon=new ImageIcon()
	    
	    println("No of holders" + holderList.size)
	   
	    //If current node is holder
	   if(holderList.contains(Shutterbug.curnode.getName)){
	     //get from the images folder
	     println("Retrieving for my images")
	      var imgDirName = img_group.getName + Shutterbug.IMG_FOLDER
	      imgDirName=imgDirName.replaceFirst("/", "_");
	     var imgPath:String =imgDirName+thumbHash+"."+format
	     var image:File= new File(imgPath)
							  
		if(image.exists()){
	     img=new ImageIcon(imgPath);
	     Shutterbug.mcs.displayImage(img, img_group.getName)
		}
	     return
	   }
	    
	   else{
	     //Send to first user in the holder list
	     
	     actor{
			 println("Sending to first node in holder list");
	         var holder:String=holderList.get(0)
	        val remoteActor = select(Node(Shutterbug.boot_ip, 10111), 'ragtime)
	        var sending=true
	        var count=0
	        
	        loopWhile(sending){
	        println("sending to" +holder);
			val mes:BootMessage = new BootMessage(QUERY, holder, "", "",0, null)
			var ip:String = ""
			var port:Int = 0
			println("Ready to query")
					remoteActor !? mes 
							match {
							case mesg: BootMessage =>
							mesg.getKind match {
							case BOOT_ACK =>
							println("Received bootack")
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
					}

					var waitTime:Long = 5000
					val userActor = select(Node(ip, port), Symbol(holder))
					var getImg:UserMessage = new UserMessage(IMG_GET, thumbHash,
					    Shutterbug.curnode, img_group, null, format)
					count=count+1;
					println(count);
					userActor ! getImg
					receiveWithin(waitTime)
					{
					  case TIMEOUT =>
					    if(count==2)
					    {
					      sending=false
					    }
					    else
					    {
					      println("Timeout");
					      holder=holderList.get(1)
					    }
					    
					   case mesg:UserMessage=>
					    mesg.getKind match{
					      				       
					    case IMG_GET_NOACK=>
					    println("Image not available at holder1")
					    if(count==2)
					    {
					      sending=false
					    }
					    else
					    {
					      holder=holderList.get(1)
					    }
					      
					    case IMG_GET_ACK =>
					    img=mesg.getData.asInstanceOf[ImageIcon]        
					    Shutterbug.mcs.displayImage(img, img_group.getName)
					    sending=false					         
					    }//match
					}//receive within
	     		}//loop
	        }//actor
	   }//else
		    
	}//func end
	
	
	def onDownload(){
	  
	  var img_group:Group = Shutterbug.curGroup
	  var userMap:HashMap[String,UserNode]=null
	  var imageMap:HashMap[String,LookupMsg]=Shutterbug.mcs.returnImageMap(img_group.getName)
	  var dirName:String=img_group.getName
	  
	  
	  println(dirName)
	  dirName=dirName.replace("/", "_")
	  println(dirName)
	  //Create a directory for the group
	  println("ImageGroupname " + img_group.getName)
	
	  var imgDir:File = new File(dirName)
	  
	  println("Directory" + imgDir)
	  
	  if(imgDir.exists()){
       deleteDirectory(imgDir)
	   println("Directory deleted")
	  }
	   println("Creating directoory " + imgDir)
	   imgDir.mkdir()
	  //loop over keyset
	  
	  var imgtemp:ArrayList[String] = new ArrayList[String];
      imgtemp.addAll(imageMap.keySet())
      var imgNum:Int = imageMap.size()
      var iter:Int = 0
    
      while (iter < imgNum)
      {
        var thumbHash=imgtemp.get(iter);
        var lkpMsg:LookupMsg=imageMap.get(thumbHash)
        var holderList:ArrayList[String]=lkpMsg.getHolder
	    var format=lkpMsg.getFormat
	    var img:ImageIcon=new ImageIcon()
        
        println("No of holders" + holderList.size)
	   
	    //If current node is holder
	   if(holderList.contains(Shutterbug.curnode.getName)){
	     //get from the images folder
	     println("Retrieving for my images")
	      var imgDirName = img_group.getName + Shutterbug.IMG_FOLDER
	      imgDirName=imgDirName.replaceFirst("/", "_");
	     var imgPath:String =imgDirName+thumbHash+"."+format
	     var image:File= new File(imgPath)
							  
		if(image.exists()){
	    img=new ImageIcon(imgPath);
	    ImageIO.write(picture.convertToBI(img), format, new File(imgDir+"/" + thumbHash +"."+format));
		}
	   }
	    
	   else{
	     //Send to first user in the holder list
	     
	     actor{
			 println("Sending to first node in holder list");
	         var holder:String=holderList.get(0)
	        val remoteActor = select(Node(Shutterbug.boot_ip, 10111), 'ragtime)
	        var sending=true
	        var count=0
	        
	        loopWhile(sending){
	        println("sending to" +holder);
			val mes:BootMessage = new BootMessage(QUERY, holder, "", "",0, null)
			var ip:String = ""
			var port:Int = 0
			println("Ready to query")
					remoteActor !? mes 
							match {
							case mesg: BootMessage =>
							mesg.getKind match {
							case BOOT_ACK =>
							println("Received bootack")
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
					}

					var waitTime:Long = 5000
					val userActor = select(Node(ip, port), Symbol(holder))
					var getImg:UserMessage = new UserMessage(IMG_GET, thumbHash,
					    Shutterbug.curnode, img_group, null, format)
					count=count+1;
					println(count);
					userActor ! getImg
					receiveWithin(waitTime)
					{
					  case TIMEOUT =>
					    if(count==2)
					    {
					      sending=false
					    }
					    else
					    {
					      println("Timeout");
					      holder=holderList.get(1)
					    }
					    
					   case mesg:UserMessage=>
					    mesg.getKind match{
					      				       
					    case IMG_GET_NOACK=>
					    println("Image not available at holder1")
					    if(count==2)
					    {
					      sending=false
					    }
					    else
					    {
					      holder=holderList.get(1)
					    }
					      
					    case IMG_GET_ACK =>
					    println("Image received")
					    img=mesg.getData.asInstanceOf[ImageIcon]        
					    ImageIO.write(picture.convertToBI(img), format, new File(imgDir+"/" + thumbHash +"."+format));
					    sending=false					         
					    }//match
					}//receive within
	     		}//loop
	        }//actor
	   }//else
        
        
        
      
      iter = iter + 1
      }
	  
	  
	}
	
	
	
	 def deleteDirectory(file:File)
	 {
 
    	if(file.isDirectory()){
 
    		//directory is empty, then delete it
    		if(file.list().length==0){
     		   file.delete();
    		   System.out.println("Directory is deleted : " 
                                                 + file.getAbsolutePath());
 
    		}else{
 
    		   //list all the directory contents
        	   var files:Array[String] = file.list();
    			var iter =0
    			var fileNum = files.length
        	   //for (String temp : files)
        	   while(iter<fileNum){
        	      //construct the file structure
        	      var fileDelete:File = new File(file, files(iter));
         	      //recursive delete
        	     deleteDirectory(fileDelete);
        	     iter=iter+1
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	     System.out.println("Directory is deleted : " 
                                                  + file.getAbsolutePath());
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		file.delete();
    		System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }
	
	
	 def onChat(chatText:String){
	   var grp = Shutterbug.curGroup
	      
	  
		// Fetch the group members for the group
		var members:ArrayList[UserNode] = grp.returnMembers

				var grpCount:Int = members.size()

				var iter:Int = 0

				while (iter < grpCount)
				{
					if (members.get(iter).getName.equals(Shutterbug.curnode.getName))
					{
						println("Not sending to node " + members.get(iter).getName)
					}
					else
					{
						println("Sending chat to node " + members.get(iter).getName)
						var remoteActor = select(Node(members.get(iter).getIP, Shutterbug.chat_port), Symbol(members.get(iter).getName))
					//	var send_msg : UserMessage = new UserMessage(CHAT, chatText, Shutterbug.curnode, grp, null,null)
						val msg:UserMessage = new UserMessage(CHAT, chatText, Shutterbug.curnode, grp, null, null)
						// Send here 
						remoteActor ! msg
					}
					iter = iter + 1
				}

	   
	 }
}