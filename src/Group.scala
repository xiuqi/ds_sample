import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashMap
import java.util.TreeMap
import java.util.ArrayList
import scala.util.control.Breaks._
import java.util.concurrent.locks.ReentrantLock


class Group(name:String) extends Serializable{
  var id:String = "";
  var members:TreeMap[String,UserNode] = new TreeMap[String, UserNode]
  var lookup:HashMap[String,UserNode] = new HashMap[String, UserNode]
  var memLock:ReentrantLock = new ReentrantLock 
  
  def upload(path:String){}
  
  def view(){}
  def getName() : String =
  {
    return this.name
  }
  
  def getSuccessor(unode:UserNode) : UserNode =
  {
    memLock.lock()
    var arrtemp:ArrayList[String] = new ArrayList[String];
    var hash_val:String = calculate_hash.md5(unode.getName).toString()
    arrtemp.addAll(members.keySet())
    var grpSize:Int = members.size()
    var iter:Int = 0
    while (iter < grpSize)
    {
      if (arrtemp.get(iter).equals(hash_val))
      {
        memLock.unlock()
        return members.get(arrtemp.get((iter+1)%grpSize))
      }
      iter = iter + 1
    }
    memLock.unlock()
    return null    
  }
  
  def returnMembers() : ArrayList[UserNode] =
  {
    memLock.lock()
    var memlist:ArrayList[UserNode] = new ArrayList[UserNode]
    memlist.addAll(members.values())
    memLock.unlock()
    return memlist
  }
  
  def addMembers(uname: String, insUserNode:UserNode)
  {
    memLock.lock()
    var hash_val:String = calculate_hash.md5(uname).toString()
    
    println("In addMembers hash for node "+insUserNode.getName+" is "+hash_val)
    members.put(hash_val, insUserNode)
    memLock.unlock()
  }
  
  
  def removeMember(uname: String)
  {
    memLock.lock()
    val hash_val:String = calculate_hash.md5(uname).toString()
    
    println("Removing node "+ uname)
    members.remove(hash_val)
    memLock.unlock()
  }
  
   def getNodeFromHash(in_hash : String) : UserNode =
   {
	    // Comparison function
		memLock.lock()
	    var arrtemp:ArrayList[String] = new ArrayList[String];
    	arrtemp.addAll(members.keySet())
    	memLock.unlock()
    	var i:Int= arrtemp.size() - 1
    	var j:Int = 0
    	while( j <= i)
    	{    	  
    	  if (arrtemp.get(j).compareTo(in_hash) > 0)
    	  {
    	    
    	     j = j - 1
        
    	     if(j<0) 
    	       j = i
    	     return members.get(arrtemp.get(j))
    	  }
    	  j+=1
    	}
    	
    	return members.get(arrtemp.get(i))       
     
   }
  
}