import scala.collection.mutable.ArraySeq
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock


class UserNode(ip: String, port: Int, name: String) extends Serializable {
	private var groupList:ArrayList[Group] = new ArrayList[Group]
	private var id:String = ""
	var userLock:ReentrantLock = new ReentrantLock 
	def addToGroup(dstGrp:Group)
	{
	  userLock.lock()
	  groupList.add(dstGrp)
	  userLock.unlock()
	  CreateGroup.setGroupNameList(Shutterbug.curnode.returnGroupName)
	  File.updateFile
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
	
	def checkGroupExist(groupName:String): Boolean = {
	 userLock.lock()
	  var size:Int = groupList.size()
	  var itr:Int = 0
	  while(itr<size){
	    if(groupList.get(itr).getName.equalsIgnoreCase(groupName)){
	      userLock.unlock()
	      return true
	    }
	    itr = itr + 1
	  }
	  userLock.unlock()
	  return false;
	}
	
	def returnGroupName: List[String] = {
	  userLock.lock()
	  var nameList:ArrayList[String] = new ArrayList[String]
	  var size:Int = groupList.size()
	  var itr:Int = 0
	  while(itr<size){
	    //nameList.add(groupList.get(itr).getCreator + "/" + groupList.get(itr).getName)
	    nameList.add(groupList.get(itr).getName)
	    itr = itr + 1
	  }
	  
	  var array = nameList.toArray()
	  var groupname = List.fromArray(array).asInstanceOf[List[String]]
	  userLock.unlock()
	  return groupname
	}

//	def returnGroupFromName(groupName:String): Group = {
//	 userLock.lock()
//	  var size:Int = groupList.size()
//	  var itr:Int = 0
//	  while(itr<size){
//	    if(groupList.get(itr).getName.equalsIgnoreCase(groupName)){
//	      
//	      var ret_grp:Group = groupList.get(itr)
//	      userLock.unlock()
//	     
//	      return ret_grp
//	    }
//	    itr = itr + 1
//	  }
//	  userLock.unlock()
//	  return null;
//	}
	
	def returnGroupFromName(groupName:String, creator:String): Group = {
	 userLock.lock()
	  var size:Int = groupList.size()
	  var itr:Int = 0
	  while(itr<size){
	    if(groupList.get(itr).getName.equalsIgnoreCase(groupName) && 
	        groupList.get(itr).getCreator.equals(creator)){
	      var ret_grp:Group = groupList.get(itr)
	      userLock.unlock()	     
	      return ret_grp
	    }
	    itr = itr + 1
	  }
	  userLock.unlock()
	  return null;
	}
	
	def getGroupFromName(groupName:String): Group = {
	 userLock.lock()
	  var size:Int = groupList.size()
	  var itr:Int = 0
	  while(itr<size){
	    if(groupList.get(itr).getName.equalsIgnoreCase(groupName)){
	      var ret_grp:Group = groupList.get(itr)
	      userLock.unlock()	     
	      return ret_grp
	    }
	    itr = itr + 1
	  }
	  userLock.unlock()
	  return null;
	}
	
	def getLock(){
	  userLock.lock()
	}
	def releaseLock(){
	  userLock.unlock()
	}
}