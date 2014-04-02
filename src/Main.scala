
import java.util.Scanner
import java.security.MessageDigest
import msgKind._
import scala.actors.remote.RemoteActor


object HelloWorld {
    RemoteActor.classLoader = getClass().getClassLoader()
    def main(args: Array[String]) {
      val one = new UserNode("localhost",10111,"a")
      val two = new UserNode("localhost",10112,"b")
      val three = new UserNode("localhost",10113,"c")
      val group = new Group("test")
      group.addMembers("a", one)
      group.addMembers("b", two)
      group.addMembers("c", three)
      
      one.addToGroup(group)
      two.addToGroup(group)
      three.addToGroup(group)
      
      //Create Current Node
      
      val curnode:UserNode = one;
      
      //Start the listening thread
      val pthread: ProcessingThread = new ProcessingThread(curnode)
      
      println("Started for node "+curnode.getName+"...")
      pthread.run
      
      //While loop
      while(true)
      {
        println("Enter 1 to send a message:")
        println("Enter 2 to exit:")
        
        val scan:Scanner = new Scanner(System.in)
        val selection: Int = scan.nextInt();
        
        selection match {
      
          
          case 1 => println("Enter the message:")
        		  	scan.nextLine()
        		  	val in_msg : String = scan.nextLine()
        		  	val hash_val = calculate_hash.md5(in_msg).toString()
        		  	
        		  	println("Hash value for message "+hash_val)
        		  	
        		  	val selectionNode:UserNode = group.getNodeFromHash(hash_val)
        		  	
        		  	println("Selected node is "+selectionNode.getName())
        		  	val img_upload:UserMessage = new UserMessage(IMG_UPLOAD, in_msg, null )
        		  	MessagePasser.send_blocking(selectionNode, img_upload)       		  	
        		  	
          case 2 => println("Bye")
          			System.exit(0)
        }

      }
      
    }
    
   
}