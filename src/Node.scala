import scala.collection.mutable.ArraySeq;
import java.util.ArrayList;

class Node(ip: String, port: Int, name: String){
	val groupList:ArrayList[Group] = null
	val id:String = ""
	  
	def addToGroup(dstGrp:Group){}
	
	def createGroup(name:String){
	  val newgrp = new Group(name)
	  //send info to supernode
	}
}