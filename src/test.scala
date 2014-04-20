import swing._
import scala.swing.BorderPanel.Position._
import java.awt.Point
import scala.swing.event.ButtonClicked
//import javax.swing.text.html.BackgroundImage
import bootMsgKind._
import Swing._
import javax.swing.JLabel;
import javax.swing.{Icon, ImageIcon}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

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
  icon = new ImageIcon("shutterbug.jpg")
}
//   label.foreground = java.awt.Color.WHITE
   label.horizontalAlignment = scala.swing.Alignment.Left
   label.verticalAlignment = scala.swing.Alignment.Top
   label.preferredSize= (new Dimension(50,50))

  lazy val ui = new ImagePanel(){
    
    imagePath = ("sample.jpeg")

  val box0 = new BoxPanel(Orientation.Horizontal) {
      contents += label
    }

   box0.background_=(new Color (0,0,0,0))
   contents+= box0;
   
   contents += VStrut(10)
  val box1 = new BoxPanel(Orientation.Horizontal){
    		
    		contents+=userLabel
    		contents+=userName
    		
  }
  contents += VStrut(10)
  box1.location.setLocation(250, 250) 
  box1.background_=(new Color (0,0,0,0))
  
  contents += box1
    contents += VStrut(10)
  val box2 = new BoxPanel(Orientation.Horizontal){
    		
    		contents += pasLabel
    		contents+=password
    		
  }
    box2.background_=(new Color (0,0,0,0))
    contents+=box2
 contents += VStrut(10)
    val box3 = new BoxPanel(Orientation.Horizontal){
      
      contents+=login
      contents+=register
      
    }
    
    contents+=box3
    border = Swing.EmptyBorder(200,200,200,200)
//      layout(new FlowPanel(FlowPanel.Alignment.Right)(
//      Button("Login") {
//        if (makeLogin()) {
//          Dialog.showMessage(this, "Login Done", "Login Done", Dialog.Message.Error)
//         // auth = Some(Auth(userName.text, password.text))
//         // close()
//        } else {
//          Dialog.showMessage(this, "Wrong username or password!", "Login Error", Dialog.Message.Error)
//        }
//      }
//      
//    )
    
  }
   
    RemoteActor.classLoader = getClass().getClassLoader()
    def top = new MainFrame {

    title = "Login"
    minimumSize = new Dimension(1000,1000)
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
