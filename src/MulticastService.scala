import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import java.util.HashMap
import java.util.ArrayList
import java.util.concurrent.locks.ReentrantLock
import javax.swing.ImageIcon
import msgKind._
import java.nio.file.Files
import java.io.File

class MulticastService {

	var holdbackMap:HashMap[String, ArrayList[UserMessage]]  = new HashMap[String, ArrayList[UserMessage]]

			// First String, groupname. Second string: Hash+format
			var refreshBuffer:HashMap[String, HashMap[String, LookupMsg]] = new HashMap[String, HashMap[String,LookupMsg]]
					var displayBuffer:HashMap[String, ArrayList[ImageIcon]] = new HashMap[String, ArrayList[ImageIcon]]

							private var refreshLock:ReentrantLock = new ReentrantLock

							def addGroup(grp : Group) 
					{
						
						refreshLock.lock()
						println("addgroup lock")
						var grpHoldbackQueue:ArrayList[UserMessage]  = new ArrayList[UserMessage]
								holdbackMap.put(grp.getName, grpHoldbackQueue)
								var grpRefreshBuffer:HashMap[String,LookupMsg]  = new HashMap[String, LookupMsg]
										refreshBuffer.put(grp.getName, grpRefreshBuffer)

										var grpdisplayQueue:ArrayList[ImageIcon]  = new ArrayList[ImageIcon]
												displayBuffer.put(grp.getName, grpdisplayQueue)
												refreshLock.unlock()
												println("addgroup unlock")
					}


					def displayRefreshBuf(grpName:String)
					{
						refreshLock.lock()
						println("displ lock")
						var grpDisplayBuffer:ArrayList[ImageIcon]  = displayBuffer.get(grpName)
						var numMsgs: Int = grpDisplayBuffer.size()

						/*if (grpDisplayBuffer.size() != 0)
						{
							var array = grpDisplayBuffer.toArray()
							var icons = List.fromArray(array).asInstanceOf[List[ImageIcon]]
							DisplayImage.setPicList(icons)
						}*/
						var array = grpDisplayBuffer.toArray()
						var icons = List.fromArray(array).asInstanceOf[List[ImageIcon]]
						DisplayImage.setPicList(icons)

						var iter : Int = 0

								while (iter < numMsgs)
								{
									Shutterbug.logger.info("Msg "+iter +" :"+grpDisplayBuffer.get(iter).toString())
									iter = iter + 1
								}

							refreshLock.unlock()
							println("displ unlock")
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
						refreshLock.unlock()
						println("addref unlock")
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
					
					def getIndexThumb(hashVal:String) : Int =
					{
					  var ar_size : Int = displayBuffer.get(Shutterbug.curGroup.getName).size()
						var it:Int = 0
						
						while (it < ar_size)
						{
						  var bufImg = picture.convertToBI(displayBuffer.get(Shutterbug.curGroup.getName).get(it))
						  var thumbHash = calculate_hash.md5_img("png", bufImg)
						  if (hashVal.equals(thumbHash))
						  {
						    return it
						  }
						  it = it+1
						}
					  return -2
					}

					def processDelete(thumb: ImageIcon)
					{
					  println("Called process delete")
						var bufImg = picture.convertToBI(thumb)
						var thumbHash = calculate_hash.md5_img("png", bufImg)
						refreshLock.lock()
						println("process delete lock")
						var lkmsg:LookupMsg = refreshBuffer.get(Shutterbug.curGroup.getName).get(thumbHash)
						if (lkmsg.checkIfHolder(Shutterbug.curnode))
						{
						  var fmt:String = lkmsg.getFormat
						  // TODO: Delete image from disk 
						  //var file:File = new File("images/"+thumbHash+"."+fmt)
						  //file.delete()
						  
						}						
						
						var index = getIndexThumb(thumbHash)
						println("Index to be removed"+index)
						println(displayBuffer.get(Shutterbug.curGroup.getName).toString())

						
						if (index >= 0)
						{
							displayBuffer.get(Shutterbug.curGroup.getName).remove(index)
							// Remove in any case
							refreshBuffer.get(Shutterbug.curGroup.getName).remove(thumbHash)
							refreshLock.unlock()
							println("process delete unlock")

							displayRefreshBuf(Shutterbug.curGroup.getName)
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
								processDelete(thumb)
					}
}