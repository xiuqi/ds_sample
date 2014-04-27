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
import javax.imageio.ImageIO

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

			def addToRefreshBuffer(sender:String,groupName:String, messg : ImageIcon, dstNode: UserNode, format:String)
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
				lkmsg.setUploader(sender)
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
				if(lkmsg == null) return;
				if (lkmsg.checkIfHolder(Shutterbug.curnode))
				{
					var fmt:String = lkmsg.getFormat
							// TODO: Delete image from disk 
							var file:File = new File("images/"+thumbHash+"."+fmt)
							file.delete()

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
							if (members.get(iter).getName.equals(unode.getName))
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
			
			def addNodeRedistribution(grp:Group, newnode:UserNode){
			  println("Starting to redistribute for new user")
			  var members:ArrayList[UserNode] = grp.returnMembers
			  if(members.size()<2){
			    println("We should never reach here")
			    return
			  }
			  else if(members.size()==2){
			    var newIndex:Int = -1
			    if(members.get(0).getName.equals(newnode.getName)){
			      newIndex = 0
			    }
			    else{
			      newIndex = 1
			    }
			    var oldIndex:Int = newIndex ^ 1
			    var oldNode:UserNode = members.get(oldIndex)
			    //The new node takes all picture from old node
			    if(Shutterbug.curnode.getName.equals(newnode.getName)){
			      var imageList:ArrayList[String] = redistBuffer.get(grp.getName).get(oldNode.getName).clone().asInstanceOf[ArrayList[String]]
			      var imagelen:Int = imageList.size()
			      var itr:Int = 0
			      while(itr < imagelen){
			        var remoteActor = select(Node(oldNode.getIP, oldNode.getPort), Symbol(oldNode.getName))
					var mes:UserMessage = new UserMessage(IMG_GET,imageList.get(itr) , Shutterbug.curnode, 
							grp, oldNode, refreshBuffer.get(grp.getName).get(imageList.get(itr)).getFormat)
					println("sending IMG_REQ to "+oldNode.getName)
					remoteActor !? mes
					match{
						case mesg:UserMessage =>
						  mesg.getKind match{
						    case IMG_GET_ACK =>
							println("Got the actual image data for add redistribution from " + oldNode.getName)
					        //TODO: Save the actual image data to disk
							
							 ImageIO.write(picture.convertToBI(mesg.getData.asInstanceOf[ImageIcon]), mesg.getFormat, new File("images"+"/" + imageList.get(itr) +"."+mesg.getFormat));
						    case IMG_GET_NOACK =>
						      println("Error downloading images")
						  }
					}
			        itr = itr + 1
			      }
			    }
			    //Update lookup and redisBuffer
			    refreshLock.lock()
			    var imageList:ArrayList[String] = redistBuffer.get(grp.getName).get(oldNode.getName).clone().asInstanceOf[ArrayList[String]]
			    var imagelen:Int = imageList.size()
			    var itr:Int = 0
			    while(itr < imagelen){
			      redistBuffer.get(grp.getName).get(newnode.getName).add(imageList.get(itr))
			      refreshBuffer.get(grp.getName).get(imageList.get(itr)).addHolder(newnode)
			      itr = itr + 1
			    }
			    refreshLock.unlock()
			    return
			  }
			  
			  //If there are multiple nodes within a the group
			  var suc:UserNode = grp.getSuccessor(newnode)
			  var pre:UserNode = grp.getPredecessor(newnode)
			  println("For add redistribution, suc: "+ suc.getName + " pre: "+pre.getName)
			  //Find all the pictures that shared between suc and pre
			  var imageList:ArrayList[String] = redistBuffer.get(grp.getName).get(pre.getName).clone().asInstanceOf[ArrayList[String]]
			  var sucList:ArrayList[String] = redistBuffer.get(grp.getName).get(suc.getName).clone().asInstanceOf[ArrayList[String]]
			  println("The size of pre list is "+imageList.size())
			  var imagelen:Int = imageList.size()
			  var itr:Int = 0
			  while(itr < imagelen){
			    //Find if the suc is also the holder
			    if(sucList.contains(imageList.get(itr))){
			      //recalculate the hash
			      var selectedNode:UserNode = grp.getNodeFromHash(imageList.get(itr))
			      println("The selectedNode is " + selectedNode.getName )
			      if(selectedNode.getName.equals(pre.getName)){
			        if(newnode.getName.equals(Shutterbug.curnode.getName)){
			          //Ask for picture from pre
			          
			          var remoteActor = select(Node(pre.getIP, pre.getPort), Symbol(pre.getName))
					  var mes:UserMessage = new UserMessage(IMG_GET,imageList.get(itr) , Shutterbug.curnode, 
							grp, pre, refreshBuffer.get(grp.getName).get(imageList.get(itr)).getFormat)
					  println("sending IMG_REQ to "+pre.getName)
					  remoteActor !? mes
					  match{
						  case mesg:UserMessage =>
							  mesg.getKind match{
						    case IMG_GET_ACK =>
							println("Got the actual image data for add redistribution from " + pre.getName)
					        //TODO: Save the actual image data to disk
							
							 ImageIO.write(picture.convertToBI(mesg.getData.asInstanceOf[ImageIcon]), mesg.getFormat, new File("images"+"/" + imageList.get(itr) +"."+mesg.getFormat));
						    case IMG_GET_NOACK =>
						      println("Error downloading images")
						  }
			          }
			        }
			        //Deleted image
			        if(suc.getName.equals(Shutterbug.curnode.getName)){
			          // TODO:Delete actual image from disk
			          var file:File = new File("images/"+imageList.get(itr)+"."+refreshBuffer.get(grp.getName).get(imageList.get(itr)).getFormat)
					  file.delete()
			          println("Actual image deleted from "+Shutterbug.curnode.getName)
			        }
			        //Update two buffers
			        refreshLock.lock()
			        refreshBuffer.get(grp.getName).get(imageList.get(itr)).changeHolder(suc, newnode)
			        redistBuffer.get(grp.getName).get(suc.getName).remove(imageList.get(itr))
			        redistBuffer.get(grp.getName).get(newnode.getName).add(imageList.get(itr))
			        refreshLock.unlock()
			      }
			      else if(selectedNode.getName.equals(newnode.getName)){
			        if(newnode.getName.equals(Shutterbug.curnode.getName)){
			          //Ask for picture from suc
			          
			          var remoteActor = select(Node(suc.getIP, suc.getPort), Symbol(suc.getName))
					  var mes:UserMessage = new UserMessage(IMG_GET,imageList.get(itr) , Shutterbug.curnode, 
							grp, suc, refreshBuffer.get(grp.getName).get(imageList.get(itr)).getFormat)
					  println("sending IMG_REQ to "+suc.getName)
					  remoteActor !? mes
					  match{
						  case mesg:UserMessage =>
							  mesg.getKind match{
						    case IMG_GET_ACK =>
							println("Got the actual image data for add redistribution from " + suc.getName)
					        //TODO: Save the actual image data to disk
							
							 ImageIO.write(picture.convertToBI(mesg.getData.asInstanceOf[ImageIcon]), mesg.getFormat, new File("images"+"/" + imageList.get(itr) +"."+mesg.getFormat));
						    case IMG_GET_NOACK =>
						      println("Error downloading images")
						  }
			          }
			        }
			        //Delete image
			         if(pre.getName.equals(Shutterbug.curnode.getName)){
			          // TODO:Delete actual image from disk
			           var file:File = new File("images/"+imageList.get(itr)+"."+refreshBuffer.get(grp.getName).get(imageList.get(itr)).getFormat)
			           file.delete()
			           println("Actual image deleted from "+Shutterbug.curnode.getName)
			        }
			        //update two buffers
			         refreshLock.lock()
			         refreshBuffer.get(grp.getName).get(imageList.get(itr)).changeHolder(pre, newnode)
			         redistBuffer.get(grp.getName).get(pre.getName).remove(imageList.get(itr))
			         redistBuffer.get(grp.getName).get(newnode.getName).add(imageList.get(itr))
			         refreshLock.unlock()
			      }
			      else if(selectedNode.getName.equals(suc.getName)){
			        if(members.size()>3)
			        	println("We should never reach here!!!")
			      }
			      else
			        println("We should never reach here!!!")
			    }
			    itr = itr + 1
			  }
			}
			
			def getImageLookup(grpName:String,imgHash:String) : LookupMsg ={
			  var lkpMsg:LookupMsg =new LookupMsg
			  refreshLock.lock()
			  lkpMsg=refreshBuffer.get(grpName).get(imgHash)
			  refreshLock.unlock()
			  return lkpMsg
			  
			}
			
			
			def addImageCaption(grpName:String,imgHash:String,caption:String){
			  refreshLock.lock()
			  refreshBuffer.get(grpName).get(imgHash).setCaption(caption)
			  refreshLock.unlock()
			 			  
			  if(checkInsideDispGrpMap(grpName)){
				  displayLock.lock
			    //if(dispMap.get(grpName).getCurImg!=null && dispMap.get(grpName).getCurImg.length()>0){
				  if(imgHash.equals(dispMap.get(grpName).getCurImg))
					dispMap.get(grpName).updateDisplay(caption)				
			    //}
				 displayLock.unlock   
			  }
			  
			}
			
			def multicastImageCaption(caption:String,imgHash:String, group:Group){
			  println("multicast caption called");
			  var members:ArrayList[UserNode] = group.returnMembers

						var grpCount:Int = members.size()

						var iter:Int = 0

						var msgData:String = caption+"/"+imgHash; 
						println("msg"+msgData);
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
								var send_msg : UserMessage =  new UserMessage(NEW_CAPTION, msgData, Shutterbug.curnode,
								    group, null, null)

								// Send here 
								remoteActor ! send_msg
							}
							iter = iter + 1
						}
			  
			}
			
			
			def displayImage(img:ImageIcon,grpName:String){
			  
			   displayLock.lock()
			   if(checkInsideDispGrpMap(grpName))
				   dispMap.get(grpName).showImage(img)
			  displayLock.unlock()
			  
			}
			
			
			def returnImageMap(grpName:String) : HashMap[String,LookupMsg]={
			  
			  var imgMap: HashMap[String,LookupMsg]=null
			  refreshLock.lock()
			  imgMap=refreshBuffer.get(grpName)
			  refreshLock.unlock()
			  
			  return imgMap
			}
			
			def addToChatBuffer(senderName:String, grpName:String, messg : String)
			{
				if(checkInsideDispGrpMap(grpName)){
				  displayLock.lock()
				  dispMap.get(grpName).updateChatArea(senderName + " : " + messg+"\n")
				  displayLock.unlock()
				}
					
			}

}