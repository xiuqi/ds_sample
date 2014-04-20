import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import java.util.HashMap
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock
import javax.swing.ImageIcon
import msgKind._
import java.io.File

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
				var nodelist:ArrayList[UserNode] = grp.returnMembers
				var i:Int = 0
				while(i<nodelist.size){
				  var tmplist:ArrayList[String] = new ArrayList[String]
				  grpredistQueue.put(nodelist.get(i).getName, tmplist)
				  i = i + 1
				}
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
//				redistBuffer.get(groupName).get(dstNode.getName).add(hash_val)
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
				var grpRefreshBuffer:HashMap[String, LookupMsg]  = refreshBuffer.get(grp.getName)
				var numMsgs:Int = grpRefreshBuffer.size()
				var iter:Int = 0
				var arrtemp:ArrayList[String] = new ArrayList[String];
				arrtemp.addAll(grpRefreshBuffer.keySet())

				// Remove node from your group list
				grp.removeMember(node.getName) 

				while (iter < numMsgs)
				{
					// See which messages the dead node held and move 
					// move it to the successor or it's successor
					if (grpRefreshBuffer.get(arrtemp.get(iter)).checkIfHolder(node))
					{
						var suc_node:UserNode = grp.getSuccessor(node)
								// Already holder
								if (grpRefreshBuffer.get(arrtemp.get(iter)).checkIfHolder(suc_node))
								{
									suc_node = grp.getSuccessor(suc_node)
											println("Successor already holder");
									if (grpRefreshBuffer.get(arrtemp.get(iter)).checkIfHolder(node))
									{
										// Already holder
										println("Successor's successor already holder")

									}
									else
									{
										grpRefreshBuffer.get(arrtemp.get(iter)).changeHolder(node, suc_node)
										if (suc_node.getName.equals(Shutterbug.curnode.getName))
										{
											// Request for the  image and store it here if you are the successor node
										}
									}

								}
								else
								{
									grpRefreshBuffer.get(arrtemp.get(iter)).changeHolder(node, suc_node)
									if (suc_node.getName.equals(Shutterbug.curnode.getName))
									{
										// Request for the  image and store it here if you are the successor node
									}
								}

					}
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
			  refreshLock.unlock()
			 // displayRefreshBuf(grpName)
			}
			
			
			
}