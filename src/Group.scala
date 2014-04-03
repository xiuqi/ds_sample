import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashMap
import java.util.TreeMap
import java.util.ArrayList
import scala.util.control.Breaks._


class Group(name:String) extends Serializable{
  val id:String = "";
  val members:TreeMap[String,UserNode] = new TreeMap[String, UserNode]
  val lookup:HashMap[String,UserNode] = new HashMap[String, UserNode]
  
  def upload(path:String){}
  
  def view(){}
  
  def returnMembers() : ArrayList[UserNode] =
  {
    var memlist:ArrayList[UserNode] = new ArrayList[UserNode]
    memlist.addAll(members.values())
    return memlist
  }
  
  def addMembers(uname: String, insUserNode:UserNode)
  {
    val hash_val:String = calculate_hash.md5(uname).toString()
    
    println("In addMembers hash for node "+insUserNode.getName+" is "+hash_val)
    members.put(hash_val, insUserNode)   
  }
  
   def getNodeFromHash(in_hash : String) : UserNode =
   {
	    // Comparison function
	    var arrtemp:ArrayList[String] = new ArrayList[String];
    	arrtemp.addAll(members.keySet())
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