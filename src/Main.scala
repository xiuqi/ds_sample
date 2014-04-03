
import java.util.Scanner
import java.security.MessageDigest
import msgKind._
import scala.actors.remote.RemoteActor
import scala.concurrent.ops._
import java.util.logging.Logger
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter
import java.io.IOException
import java.util.logging.Handler


object Shutterbug {
    RemoteActor.classLoader = getClass().getClassLoader()
    var curnode:UserNode = null
    var logger:Logger = Logger.getLogger("MyLog");  
    var fh: FileHandler = null;
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
      
      curnode = three
      
      
      try {  

        // This block configure the logger with handler and formatter  
        fh = new FileHandler(curnode.getName+".log");  
        logger.addHandler(fh);
        logger.setUseParentHandlers(false)
        
        var formatter:SimpleFormatter = new SimpleFormatter();  
        fh.setFormatter(formatter);  

        // the following statement is used to log any messages  
        logger.info("My first log");  

      }
      
      catch  {  
      case e:SecurityException => e.printStackTrace();  
    
      case ioe: IOException =>  ioe.printStackTrace();  
      } 
      
      
      //Start the listening thread
      val pthread: ProcessingThread = new ProcessingThread(curnode)
      
      // Start the refresh thread
      //val refThread: RefreshThread = new RefreshThread
      
      
      println("Started for node "+curnode.getName+"...")
      pthread.run
      
      // Run the background refresh thread here 
      spawn {
    	  while (true) { 
    		  Shutterbug.curnode.displayRefreshBuf
    		  Thread.sleep(20000);
    		  // clear the cache's old entries
    	  }
      }
      
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
        		  	// Detect the group which this node is in while sending the message here
        		  	
        		  	println("Selected node is "+selectionNode.getName())
        		  	
        		  	val img_upload:UserMessage = new UserMessage(IMG_UPLOAD, in_msg, curnode )
        		  	
        		  	// Send the Blocking message to the storer
        		  	MessagePasser.send_blocking(selectionNode, img_upload, group)       		  	
        		  	
          case 2 => println("Bye")
          			System.exit(0)
        }

      }
      
    }
   
}