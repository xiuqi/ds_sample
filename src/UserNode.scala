import scala.collection.mutable.ArraySeq
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock

class UserNode(ip: String, port: Int, name: String) extends Serializable {
	private var groupList:ArrayList[Group] = new ArrayList[Group]
	private var id:String = ""

	
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
	
	
	
	
	def createGroup(name:String){
	  val newgrp = new Group(name)
	  //send info to supernode
	}
	
	
	
	
}