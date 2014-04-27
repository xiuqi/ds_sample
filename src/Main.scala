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
import javax.swing.ImageIcon


object Shutterbug {
    RemoteActor.classLoader = getClass().getClassLoader()
    var IMG_FOLDER:String = "images/"
    var grp_file_name:String = "groupInfo.txt"
    var app_port:Int = 10234
    var check_inside:Int =0
    var boot_ip:String = "localhost"//"ashutterbug.noip.me"
    var curnode:UserNode = null
    var curGroup:Group = null
    var logger:Logger = Logger.getLogger("MyLog");  
    var fh: FileHandler = null;
    var mcs:MulticastService = new MulticastService
    def main(args: Array[String]) {
      var one = new UserNode("128.237.223.235",10111,"a")
      var two = new UserNode("128.237.218.148",10112,"b")
      var three = new UserNode("128.237.126.28",10113,"c")
      //var four = new UserNode("localhost",10114,"d")
      var group = new Group("test")
      group.addMembers("a", one)
      group.addMembers("b", two)
      group.addMembers("c", three)
      //group.addMembers("d", four)
      
      one.addToGroup(group)
      two.addToGroup(group)
      three.addToGroup(group)
      //four.addToGroup(group)
      
      mcs.addGroup(group)
      
      //Create Current Node
      
      curnode = one
      curGroup = group
      
      
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
      var pthread: ProcessingThread = new ProcessingThread(curnode)
      
      // Start the refresh thread
      //val refThread: RefreshThread = new RefreshThread
      
      
      println("Started for node "+curnode.getName+"...")
      pthread.run
      //View group
      var im = new ImageIcon()
//      DisplayImage.setPicList(List(im))
//      DisplayImage.main(args)
      
      // Run the background refresh thread here 
//      spawn {
//    	  while (true) { 
//    		  mcs.displayRefreshBuf(group.getName)
//    		  Thread.sleep(20000);
//    		  // clear the cache's old entries
//    	  }
//      }
      
      //While loop
      while(true)
      {
        println("Enter 1 to send a message:")
        println("Enter 2 to exit:")
        
        var scan:Scanner = new Scanner(System.in)
        var selection: Int = scan.nextInt();
        
        selection match {
      
          
          case 1 => println("Enter the Image Path:")
        		  	scan.nextLine()
        		  	var in_msg : String = scan.nextLine()
        		  	
        		  	var img = new ImageIcon(in_msg)
        		  	var format = picture.getPicFormat(in_msg)
        		  	var hash_val = calculate_hash.md5_img(format, picture.convertToBI(img))
        		  	
        		  	println("Hash value for message "+hash_val)
        		  	
        		  	var selectionNode:UserNode = group.getNodeFromHash(hash_val)
        		  	// Detect the group which this node is in while sending the message here
        		  	
        		  	println("Selected node is "+selectionNode.getName())
        		  	
        		  	var img_upload:UserMessage = new UserMessage(IMG_UPLOAD, img, curnode, group, selectionNode, format)
        		  	          
        		  	var selectionNode2:UserNode = group.getSuccessor(selectionNode)
        		  	println("Selected node is "+selectionNode2.getName())
        		  	
        		  	var img_upload2:UserMessage = new UserMessage(IMG_UPLOAD, img, curnode, group, selectionNode2, format)
        		  	
        		  	// Send the Blocking message to the storers
        		  	MessagePasser.send_blocking(selectionNode, img_upload, group)
        		  	MessagePasser.send_blocking(selectionNode2, img_upload2, group)
        		  	
          case 2 => println("Bye")
          			System.exit(0)
        }

      }
      
    }
   
}