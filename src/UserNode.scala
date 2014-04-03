import scala.collection.mutable.ArraySeq
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock

class UserNode(ip: String, port: Int, name: String) extends Serializable {
	private var groupList:ArrayList[Group] = new ArrayList[Group]
	private var id:String = ""
	private var refreshBuffer:ArrayList[String] = new ArrayList[String]
	private var refreshLock:ReentrantLock = new ReentrantLock
	
	def addToGroup(dstGrp:Group)
	{
	  groupList.add(dstGrp)
	}
	
	def getName() : String=
	{
	  return name
	}
	
	def getPort() : Int=
	{
	  return port
	}
	
	def getIP() : String=
	{
	  return ip
	}
	
	def getGroupList() :ArrayList[Group]=
	{
	  return groupList
	}
	
	private def setID( idNo: String ){
	  id = idNo
	}
	
	def lockRefreshBuf()
	{
	  refreshLock.lock()
	}
	
	def displayRefreshBuf()
	{
	  refreshLock.lock()
	  val numMsgs: Int = refreshBuffer.size()
	  var iter : Int = 0
	  
	  //if (numMsgs == 0)
	    //println("No Messages in the refresh buffer")
	  
	  while (iter < numMsgs)
	  {
	    Shutterbug.logger.info("Msg "+iter +" :"+refreshBuffer.get(iter) )
	    iter = iter + 1
	  }
	  
	  refreshLock.unlock()
	}
	
	def unlockRefreshBuf()
	{
	  refreshLock.unlock()
	}
	
	
	def createGroup(name:String){
	  val newgrp = new Group(name)
	  //send info to supernode
	}
	
	def addToRefreshBuffer(messg : String)
	{
	  refreshLock.lock()
	  refreshBuffer.add(messg)
	  refreshLock.unlock()
	  
	}
	
	
}