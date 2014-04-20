
import swing._
import event._
import java.util.Date
import scala.swing.GridPanel
import java.awt.Color
import Swing._
import java.text.SimpleDateFormat
import javax.swing.{Icon, ImageIcon}
import ComboBox._

/**
 * Demonstrates how to use combo boxes and custom item renderers.
 * 
 * TODO: clean up layout
 */
class DisplayImage extends SimpleSwingApplication {

	var list:List[ImageIcon]= List(new ImageIcon())
	var createGrp:java.awt.Component = null
	//var displayGroup:Group
	//var iconBox:List[ImageIcon]=null
	def setPicList(pics:List[ImageIcon]){
	list=pics
	ui.iconBox.listData_=(list)
	println(list.toString)
	println("list updated");
}
	
	def setMain(mainframe:java.awt.Component){
	  createGrp= mainframe
	  
	}

	val back = new Button("Back")


lazy val ui = new BoxPanel(Orientation.Horizontal){
	  val iconBox=	new ListView(list){    
		renderer = new ListView.AbstractRenderer[Icon, Label](new Label) {
			def configure(list: ListView[_], isSelected: Boolean, focused: Boolean, icon: Icon, index: Int) {	component.icon = icon
			list.selectionBackground=java.awt.Color.blue
			component.xAlignment = Alignment.Center
			if(isSelected) {
							component.border = Swing.LineBorder(list.selectionBackground, 3)
						} else {
							component.border = Swing.EmptyBorder(3)
						}
			}
		}
	}

	val label0 = new Label("Chat Window")
	 label0.foreground = java.awt.Color.BLUE
	val chatArea = new TextArea() {
    editable = false
    background = Color.WHITE
  }
  val inputField = new TextField()
  def sendButton = new Button("Send")
  
  	val button = new Button("Delete")
	val box00 = new BoxPanel(Orientation.Vertical){

	//val invite = new Button("Invite")
	//val username = new TextField()
    val box01 = new BoxPanel(Orientation.Horizontal){
	contents += button
	contents+=back
	
    }
    contents += iconBox
    contents += box01
  }
  box00.border = Swing.LineBorder(iconBox.background, 3)
		contents+=box00
		
//		val usersLabel = new Label("Users")
//		usersLabel.preferredSize= (new Dimension(250,250))
//  val box02 = new BoxPanel(Orientation.Vertical){
//    contents += usersLabel
//  }
//
//	contents+=box02	
  val box0 = new BoxPanel(Orientation.Vertical){
   contents += label0
   contents += chatArea
   contents += inputField
   contents += sendButton
   
      }
  box0.border = Swing.LineBorder(chatArea.background, 3)
  box0.preferredSize = (new Dimension(100,100))
  	contents += VStrut(10)
  	contents += box0
	contents += VStrut(10)
//			contents += new BorderPanel {
//				add(iconBox, BorderPanel.Position.West)
//			}
	


		listenTo(button)
		reactions += {
	  case ButtonClicked(`button`) =>
	try{
		var result = Dialog.showConfirmation(contents.head, "Are you sure you want to delete these " + iconBox.selection.items.size + " image?" ,
		icon=iconBox.selection.items(0));
		
		if (result == Dialog.Result.Yes)
		{
		  Shutterbug.mcs.multicastDelete(iconBox.selection.items(0), Shutterbug.curGroup.getName)
		}
	}
	catch{
	case e:Exception => Dialog.showMessage(contents.head, "Please select image for deletion")
	}
//	case ButtonClicked(`invite`) =>
//	  //This is where we add people
//	  println("Invite: "+username.text)
		
		}

}

def top = new MainFrame {
	Shutterbug.mcs.displayRefreshBuf(Shutterbug.curGroup.getName)
	//self.open()
	title = "Album"//Shutterbug.curnode.getName
	minimumSize = new Dimension(1000,1000)
	
	menuBar = new MenuBar {
      contents += new Menu("File") {
       // contents += new MenuItem("Upload")
        contents += new MenuItem(Action("Upload"){
          val fileChooser = new FileChooser
        if (fileChooser.showOpenDialog(null) == FileChooser.Result.Approve) {
        	println(fileChooser.selectedFile.getAbsolutePath())
        	UIinteraction.onUpload(fileChooser.selectedFile.getAbsolutePath())

          
        }
        })
        contents += new MenuItem(Action("Invite user") {
          //println("Action '"+ title +"' invoked")
          var s = Dialog.showInput(null, "Enter username :",title ="Invitation", initial="Enter name")
        	s match {
        	case Some(str) => 
        	  println(str)
        	  if(str.equals("") || str.equals("Enter name")){
        	    Dialog.showMessage(null, "Please enter username")
        	  }
        	  else{
        	    //User already in the group
        	    if(Shutterbug.curGroup.checkMemberExists(str)){
        	          		  Dialog.showMessage(null, "User already exists in the group")
        	    }
        	    else{
        	      //Add user function
        	      UIinteraction.onInviteUser(str)
        	      //TODO : check if user was added
        	     // Dialog.showMessage(null, "User successfully added")
        	    }
        	  }
        	case None => 
        	  //println("case None")
        	}
        })
        //contents += new Separator

      }
//      contents += new Menu("Delete") 
	}
	
	listenTo(back)
	reactions+={
	  case ButtonClicked(`back`) =>
	    //CreateGroup.setVisible()
	   // Shutterbug.check_inside=0
	    Shutterbug.mcs.unsetDispGrpMap(Shutterbug.curGroup.getName)
	    createGrp.setVisible(true)
	    close
	   // CreateGroup.top.setVisible(true)
	    //CreateGroup.startup(args)
	}
	val outputTextScrollPane = new ScrollPane(ui)
	contents = outputTextScrollPane
	
}
}

