

import java.util.HashMap
import java.util.ArrayList
import javax.swing.ImageIcon

class LookupMsg() {
  // HashMap for acks
  var holder:ArrayList[String] = new ArrayList[String] 
  var format:String = null;
  
  def setFormat(fmt:String)
  {
    format = fmt
  }
  
  def getFormat() : String =
  {
    return format
  }
  
  def addHolder(hldNode : UserNode)
  {
    if (!holder.contains(hldNode.getName))
    	holder.add(hldNode.getName)
  }
  
  def changeHolder(oldNode:UserNode, newnode: UserNode)
  {
    var i: Int = 0
    
    while (i < 2)
    {
    	if (holder.get(i).equals(oldNode.getName))
    	{
    	  holder.remove(i)
    	  holder.add(i, newnode.getName)
    	}
    	i = i+1
    }
  }
  
  def checkIfHolder(hldNode:UserNode) : Boolean =
  {
    var i: Int = 0

	if (holder.contains(hldNode.getName))
	{
		return true
	}
 
    return false
  }

}