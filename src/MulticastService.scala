import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import java.util.HashMap
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock
import javax.swing.ImageIcon
import msgKind._
import java.io.File
import java.util.Map

class MulticastService {

	//var holdbackMap:HashMap[String, ArrayList[UserMessage]]  = new HashMap[String, ArrayList[UserMessage]]

	// First String, groupname. Second string: Hash+format
	var refreshBuffer:HashMap[String, HashMap[String, LookupMsg]] = new HashMap[String, HashMap[String,LookupMsg]]
	var displayBuffer:HashMap[String, ArrayList[ImageIcon]] = new HashMap[String, ArrayList[ImageIcon]]
	var redistBuffer:HashMap[String, HashMap[String,ArrayList[String]]] = new HashMap[String, HashMap[String,ArrayList[String]]]
	var dispMap:HashMap[String, DisplayImage] = new HashMap[String, DisplayImage]
	var dispGrpMap:HashMap[String, Int] = new HashMap[String, Int]
	private var refreshLock:ReentrantLock = new ReentrantLock
	private var displayLock:ReentrantLock = new ReentrantLock
	
	def getBuffers(grpName:String, inv_data:InvitationData)
	{
	  refreshLock.lock()
	  
	  inv_data.set_display(displayBuffer.get(grpName))
	  inv_data.set_refresh(refreshBuffer.get(grpName))
	  inv_data.set_redist(redistBuffer.get(grpName))
	  refreshLock.unlock()
	  
	}
	
	
	def setDispMap(grpName:String, disp:DisplayImage){
	  displayLock.lock()
	  	println("display lock")
	  dispMap.put(grpName,disp)
	  
	  displayLock.unlock()
	  	println("display unlock")
	  
	}
	
	def setDispGrpMap(grpName:String){
	
	  dispGrpMap.put(grpName,1)
	  	  
	}
	
	def unsetDispGrpMap(grpName:String){
	
	  dispGrpMap.put(grpName,0)
	  	  
	}
	
	def checkInsideDispGrpMap(grpName:String) :Boolean ={
	
	  if (dispGrpMap.containsKey(grpName) && dispGrpMap.get(grpName) ==1)
	    return true;
	  	  
	  return false;
	}
	
	
	
	
	def removeDispMap(grpName:String){
	  displayLock.lock()
	  	println("display lock")
	  dispMap.remove(grpName)
	//dispMap.put(grpName,null)
	   displayLock.unlock()
	   	println("display unlock")
	  
	}
	
	

			def addGroup(grp : Group) 
			{

				refreshLock.lock()
				println("addgroup lock")
				//						var grpHoldbackQueue:ArrayList[UserMessage]  = new ArrayList[UserMessage]
				//								holdbackMap.put(grp.getName, grpHoldbackQueue)
				var grpRefreshBuffer:HashMap[String,LookupMsg]  = new HashMap[String, LookupMsg]
				refreshBuffer.put(grp.getName, grpRefreshBuffer)

				var grpdisplayQueue:ArrayList[ImageIcon]  = new ArrayList[ImageIcon]
				displayBuffer.put(grp.getName, grpdisplayQueue)
				
				var grpredistQueue:HashMap[String,ArrayList[String]] = new HashMap[String,ArrayList[String]]
				var tmplist:ArrayList[String] = new ArrayList[String]
				grpredistQueue.put(Shutterbug.curnode.getName, tmplist)
				redistBuffer.put(grp.getName, grpredistQueue)
				refreshLock.unlock()
				println("addgroup unlock")
			}


			def displayRefreshBuf(grpName:String)
			{
				refreshLock.lock()
				println("displ lock")
				var grpDisplayBuffer:ArrayList[ImageIcon]  = displayBuffer.get(grpName)
				var numMsgs: Int = grpDisplayBuffer.size()
				refreshLock.unlock()
				println("displ unlock")
				/*if (grpDisplayBuffer.size() != 0)
						{
							var array = grpDisplayBuffer.toArray()
							var icons = List.fromArray(array).asInstanceOf[List[ImageIcon]]
							DisplayImage.setPicList(icons)
						}*/
				var array = grpDisplayBuffer.toArray()
				var icons = List.fromArray(array).asInstanceOf[List[ImageIcon]]
				displayLock.lock()
				println("display lock")
						
						dispMap.get(grpName).setPicList(icons)
			   displayLock.unlock()
			   println("display unlock")
						var iter : Int = 0

						while (iter < numMsgs)
						{
							Shutterbug.logger.info("Msg "+iter +" :"+grpDisplayBuffer.get(iter).toString())
							iter = iter + 1
						}

						
						
			}

			def addToRefreshBuffer(groupName:String, messg : ImageIcon, dstNode: UserNode, format:String)
			{
				refreshLock.lock()
				println("addref lock")
				val hash_val:String = calculate_hash.md5_img(format, picture.convertToBI(messg))

				// Add the second holder if already present
				if (refreshBuffer.get(groupName).containsKey(hash_val))
				{
					refreshBuffer.get(groupName).get(hash_val).addHolder(dstNode)
				}
				else
				{	  
					var lkmsg:LookupMsg = new LookupMsg()
				lkmsg.addHolder(dstNode)
				lkmsg.setFormat(format)
				refreshBuffer.get(groupName).put(hash_val, lkmsg)
				displayBuffer.get(groupName).add(messg)
				
				}
				if(!redistBuffer.get(groupName).get(dstNode.getName).contains(hash_val))
					redistBuffer.get(groupName).get(dstNode.getName).add(hash_val)
				refreshLock.unlock()
				println("addref unlock")
				
				if(checkInsideDispGrpMap(groupName))
				displayRefreshBuf(groupName)
			}


			def processDeadNode(node:UserNode, grp:Group)
			{
				// Put the images from the dead thread in the successor
				refreshLock.lock()
				
				println("procDead lock")
					var grpName:String = grp.getName
					println("In redistribution, current group is "+grpName)
					var redList:HashMap[String,ArrayList[String]] = redistBuffer.get(grpName)
					if(redList.containsKey(node.getName)){
					  var imageList:ArrayList[String] = redList.get(node.getName)
					  var i:Int = 0
					  while(i<imageList.size()){
					    var holderList:ArrayList[String] = refreshBuffer.get(grpName).get(imageList.get(i)).holder
					    var deleteIndex:Int = -1
					    if(holderList.get(0).equals(node.getName))
					      deleteIndex = 0
					    else
					      deleteIndex = 1
					    var otherIndex:Int = 1 ^ deleteIndex
					    var suc1:UserNode = Shutterbug.curnode.getGroupFromName(grpName).getSuccessor(node)
					    println("The successor of the dead node is "+suc1.getName)
					    if(suc1.getName.equals(holderList.get(otherIndex))){
					      println("Suc1 is the holder")
					      var suc2:UserNode = Shutterbug.curnode.getGroupFromName(grpName).getSuccessor(suc1)
					      println("The second successor "+suc2.getName)
					      if(node.getName.equals(suc2.getName)){}
					      else{
					        if(suc2.getName.equals(Shutterbug.curnode.getName)){
					          //query the suc1 and save the actual image
					          
					            var remoteActor = select(Node(suc1.getIP, suc1.getPort), Symbol(suc1.getName))
					            var mes:UserMessage = new UserMessage(IMG_REQ,imageList.get(i) , Shutterbug.curnode, 
					                Shutterbug.curnode.getGroupFromName(grpName), suc1, 
					                refreshBuffer.get(grpName).get(imageList.get(i)).getFormat)
					            println("sending IMG_REQ to "+suc1.getName)
					            remoteActor !? mes
					            match{
					              case mesg:UserMessage =>
					                println("Got the actual image data for redistribution")
					                //Save the actual image data to disk
					            }
					          
					        }
					        refreshBuffer.get(grpName).get(imageList.get(i)).changeHolder(node, suc2)
					      }
					    }
					    else{
					      println("Suc1 is not the holder")
					      if(suc1.getName.equals(Shutterbug.curnode.getName)){
					          //query the suc1 and save the actual image
					          var newnode:UserNode =  Shutterbug.curnode.getGroupFromName(grpName).getNodeFromName(holderList.get(otherIndex))
					    	  
					            var remoteActor = select(Node(newnode.getIP, newnode.getPort), Symbol(newnode.getName))
					            var mes:UserMessage = new UserMessage(IMG_REQ,imageList.get(i) , Shutterbug.curnode, 
					            Shutterbug.curnode.getGroupFromName(grpName), newnode, 
					            refreshBuffer.get(grpName).get(imageList.get(i)).getFormat)
					            println("sending IMG_REQ to "+newnode.getName)
					            remoteActor !? mes
					            match{
					              case mesg:UserMessage =>
					                println("Got the actual image data for redistribution")
					                //Save the actual image data to disk
					            }
					          
					       }
					       refreshBuffer.get(grpName).get(imageList.get(i)).changeHolder(node, suc1)
					    }
					    i = i + 1
					  }
					  //remove node
					  redistBuffer.get(grpName).remove(node.getName)
					  Shutterbug.curnode.getGroupFromName(grpName).removeMember(node.getName)
					  File.updateFile
					}
					else{
					  println("Duplicated delete action")
					}
				println("procDead unlock")
				refreshLock.unlock()	  
			}

			def getIndexThumb(hashVal:String, grp:String) : Int =
				{
					var ar_size : Int = displayBuffer.get(grp).size()
							var it:Int = 0

							while (it < ar_size)
							{
								var bufImg = picture.convertToBI(displayBuffer.get(grp).get(it))
										var thumbHash = calculate_hash.md5_img("png", bufImg)
										if (hashVal.equals(thumbHash))
										{
											return it
										}
								it = it+1
							}
					return -2
				}

			def processDelete(thumb: ImageIcon, grp:String)
			{
				println("Called process delete")
				var bufImg = picture.convertToBI(thumb)
				var thumbHash = calculate_hash.md5_img("png", bufImg)
				refreshLock.lock()
				println("process delete lock")
				var lkmsg:LookupMsg = refreshBuffer.get(grp).get(thumbHash)
				if (lkmsg.checkIfHolder(Shutterbug.curnode))
				{
					var fmt:String = lkmsg.getFormat
							// TODO: Delete image from disk 
							//var file:File = new File("images/"+thumbHash+"."+fmt)
							//file.delete()

				}						

				var index = getIndexThumb(thumbHash,grp)
						println("Index to be removed"+index)
						println(displayBuffer.get(grp).toString())


						if (index >= 0)
						{
							displayBuffer.get(grp).remove(index)
							// Remove in any case
							refreshBuffer.get(grp).remove(thumbHash)
							var holder:ArrayList[String] = lkmsg.holder
							redistBuffer.get(grp).get(holder.get(0)).remove(thumbHash)
							if(holder.size()>1)
							  redistBuffer.get(grp).get(holder.get(1)).remove(thumbHash)
							refreshLock.unlock()
							println("process delete unlock")

							if(checkInsideDispGrpMap(grp))
									displayRefreshBuf(grp)
							
						}
						else
						{
							refreshLock.unlock()
							println("process delete unlock")
							println("Index negative")
						}
			}

			def multicastDelete(thumb:ImageIcon, groupName:String)
			{	 
				var bufImg = picture.convertToBI(thumb)
						var hashVal = calculate_hash.md5_img("png", bufImg)

						var members:ArrayList[UserNode] = Shutterbug.curGroup.returnMembers

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
								println("Sending to node " + members.get(iter).getName)
								var remoteActor = select(Node(members.get(iter).getIP, members.get(iter).getPort), Symbol(members.get(iter).getName))
								var send_msg : UserMessage =  new UserMessage(DEL_IMG, thumb, Shutterbug.curnode, Shutterbug.curGroup, null, null)

								// Send here 
								remoteActor ! send_msg
							}
							iter = iter + 1
						}
						processDelete(thumb, Shutterbug.curGroup.getName)
			}
			
			def multicastNewMember(unode:UserNode, group:Group)
			{	 
						var members:ArrayList[UserNode] = group.returnMembers

						var grpCount:Int = members.size()

						var iter:Int = 0

						while (iter < grpCount)
						{
							if (members.get(iter).getName.equals(Shutterbug.curnode.getName) || 
							    members.get(iter).getName.equals(unode.getName))
							{
								println("Not sending to node " + members.get(iter).getName)
							}
							else
							{
								println("Sending to node " + members.get(iter).getName)
								var remoteActor = select(Node(members.get(iter).getIP, members.get(iter).getPort), Symbol(members.get(iter).getName))
								var send_msg : UserMessage =  new UserMessage(NEW_MEMBER, unode, Shutterbug.curnode,
								    group, null, null)

								// Send here 
								remoteActor ! send_msg
							}
							iter = iter + 1
						}
				}
			
			def setBuffers(inv_data:InvitationData, grpName:String) {
			  refreshLock.lock()
			  refreshBuffer.put(grpName,inv_data.get_refresh)
			  displayBuffer.put(grpName,inv_data.get_display)
			  redistBuffer.put(grpName,inv_data.get_redist)
			  refreshLock.unlock()
			 // displayRefreshBuf(grpName)
			}
			
			def addInvitedUser(name:String, grp:String){
			  refreshLock.lock()
			  var tmpList:ArrayList[String] = new ArrayList[String]
			  redistBuffer.get(grp).put(name,tmpList)
			  refreshLock.unlock()
			}
			
}