import swing._
import scala.swing.BorderPanel.Position._
import java.awt.Point
import scala.swing.event.ButtonClicked
import bootMsgKind._
import Swing._
import javax.swing.JLabel
import javax.swing.{Icon, ImageIcon}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._


object LoginRegister extends SimpleSwingApplication{
  
  RemoteActor.classLoader = getClass().getClassLoader()
  val remoteActor = select(Node(Shutterbug.boot_ip, 10111), 'ragtime)
  val login = new Button("Login")
  val register = new Button("Register")
  val userName = new TextField
  userName.location.setLocation(250, 250) 
  userName.maximumSize_= (new Dimension(120,25))
  
  val userLabel = new Label("UserName: ")
  userLabel.foreground = java.awt.Color.WHITE
  userLabel.minimumSize_=(new Dimension(120,25))
  userLabel.location.setLocation(50, 50) 
  
  val password = new PasswordField
  password.maximumSize_= (new Dimension(120,25))
  val pasLabel = new Label("Password: ")
  pasLabel.foreground = java.awt.Color.WHITE
  pasLabel.minimumSize_=(new Dimension(120,25))
  
   var label = new Label {
  //icon = new ImageIcon("shutterbug.jpg")
    text="ASSIST SHUTTERBUG"
    
}
//   label.foreground = java.awt.Color.WHITE
   label.horizontalAlignment = scala.swing.Alignment.Left
   label.verticalAlignment = scala.swing.Alignment.Top
   label.foreground=java.awt.Color.white
  // label.preferredSize= (new Dimension(50,50))
   label.font=new Font("Comic Sans MS",0,50)
   
   var shareLabel = new Label("Share your photos!!")
   shareLabel.font=new Font("Comic Sans MS",0,20)
   shareLabel.foreground=java.awt.Color.white

  lazy val ui = new ImagePanel(){
    
    imagePath = ("cam.jpeg")

    val box0 = new BoxPanel(Orientation.Vertical) {
      contents += label
      contents+=shareLabel
     }
   //box0.border=Swing.EmptyBorder(0, 5, 5, 0)
   box0.background_=(new Color (0,0,0,0))
   contents+= box0;
   
   contents += VStrut(10)
  val box1 = new BoxPanel(Orientation.Horizontal){

    		contents+=userLabel
    		contents+=userName
    		
  }
  contents += VStrut(10)
  //box1.location.setLocation(250, 250) 
  box1.border=Swing.EmptyBorder(0, 175, 0, 0)
  box1.background_=(new Color (0,0,0,0))
  
  contents += box1
  contents += VStrut(10)
  val box2 = new BoxPanel(Orientation.Horizontal){
    		
    		contents += pasLabel
    		contents+=password
    		
  }
    box2.border=Swing.EmptyBorder(0, 175, 0, 0)
    box2.background_=(new Color (0,0,0,0))
    contents+=box2
contents += VStrut(10)
 
 
    val box3 = new BoxPanel(Orientation.Horizontal){
      
      contents+=login
      contents+=register
      
    }
    
      box3.border=Swing.EmptyBorder(0, 175, 0, 0)
   box3.background_=(new Color (0,0,0,0))
    contents+=box3
   
    border = Swing.EmptyBorder(70, 100, 100, 100)   
  }
   
    RemoteActor.classLoader = getClass().getClassLoader()
    def top = new MainFrame {

    title = "Welcome to Assist ShutterBug"
    minimumSize= new Dimension(600,600)
    maximumSize= new Dimension(600,600)
    resizable=false
    iconImage_=(new ImageIcon("frameIcon.png").getImage())
    ui.location.setLocation(250, 250)
    resizable = (false)
    contents = ui
    listenTo(login,register)
        reactions+={
        case ButtonClicked(`login`) =>
          
    	  val ip:String = boot.getBoot
          val mes:BootMessage = new BootMessage(LOGIN, userName.text, new String(password.password), ip, Shutterbug.app_port, null)
    	  println("Ready to send login info")
    	  remoteActor !? mes 
    	  match {
          		case mesg: BootMessage =>
                    mesg.getKind match {
                    	case BOOT_ACK =>
                    		//println("Login successful")
                    		var result = Dialog.showMessage(contents.head, mesg.getData)
                    		UIinteraction.onLogin(ip, Shutterbug.app_port, userName.text)
                    		CreateGroup.startup(null)
                    		self.setVisible(false)
                    	case BOOTSTRAP_ERROR =>
                    		//println("Failed. Error: "+mesg.getData.toString())
                    		var result = Dialog.showMessage(contents.head, "The login process failed: " + mesg.getData.toString())
                    	}
          }    
        case ButtonClicked(`register`) => 
          println(userName.text)
          println(new String(password.password))
          val mes:BootMessage = new BootMessage(REGISTER, userName.text, new String(password.password), "",0, null)
           println("Ready to send registration info")
    	  remoteActor !? mes 
    	  match {
          		case mesg: BootMessage =>
                    mesg.getKind match {
                    	case BOOT_ACK =>
                    		//println("Login successful")
                    		var result = Dialog.showMessage(contents.head, mesg.getData)
                    	case BOOTSTRAP_ERROR =>
                    		//println("Failed. Error: "+mesg.getData.toString())
                    		var result = Dialog.showMessage(contents.head, "The registration failed: " + mesg.getData.toString())
                    	}
          }
      }
  }
}
