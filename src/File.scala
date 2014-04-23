import java.util.concurrent.locks.ReentrantLock
import java.util.ArrayList
import java.io._
import scala.io.Source
import java.util.HashMap

object File {
	var filelock:ReentrantLock = new ReentrantLock
   
	def updateFile(){
	  filelock.lock()
	  var writer:PrintWriter = null
	  try{
	  	writer = new PrintWriter(new File("test.txt" ))
	  }
	  catch{
	    case e:IOException =>
	      println("Can not create file properly")
	      filelock.unlock()
	      return
	  }
	 	var i:Int = 0
	 	var j:Int = 0
	 	var gList:ArrayList[Group] = Shutterbug.curnode.getGroupList
	 	var gListSize:Int = gList.size()
	 	var groupSize:Int = 0
	 	while(i<gListSize){
	 	  //Write group name
	 	  writer.write(gList.get(i).getName+":")
	 	  var memberlist:ArrayList[UserNode] = gList.get(i).returnMembers
	 	  groupSize = memberlist.size()
	 	  //Write node information
	 	  j = 0
	 	  while(j<groupSize){
	 	    writer.write(memberlist.get(j).getName)
	 	    if(j!=groupSize-1) writer.write(",")
	 	    j = j+1
	 	  }
	 	  writer.write("\n")
	 	  i = i+1
	 	}
	 	writer.close()
	 	filelock.unlock()
	}
	
	def readFromFile(): HashMap[String, ArrayList[String]]= {
	  filelock.lock()
	  var res:HashMap[String, ArrayList[String]] = new HashMap[String, ArrayList[String]]
	  try{
	  	for(line <- Source.fromFile("test.txt").getLines()){
	  		var nameAndMem:Array[String] = line.split(":")
	  		var memList:Array[String] = nameAndMem(1).split(",")
	  		var i:Int = 0
	  		var len:Int = memList.length
	  		var tempList:ArrayList[String] = new ArrayList[String]
	  		while(i<len){
	  		  tempList.add(memList(i))
	  		  i = i+1
	  		}
	  		res.put(nameAndMem(0), tempList)
	  	}
	  }
	  catch{
	    case ex: IOException => {
            println("File does not exist or format error")
            filelock.unlock()
            return null
         }
	  }
	  filelock.unlock()
	  return res
	}
}