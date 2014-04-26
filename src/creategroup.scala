

import swing._
import scala.swing.BorderPanel.Position._
import java.awt.Point
import scala.swing.event.ButtonClicked
import scala.swing.event.SelectionChanged
//import javax.swing.text.html.BackgroundImage

object  CreateGroup extends SimpleSwingApplication{
  
  var groupNameList:List[String] = List()
  def setGroupNameList(newNameList:List[String]){
    groupNameList = newNameList
    iconBox.listData = (groupNameList)
  }
  val create = new Button("Create group")
  val view = new Button("View")
  val iconBox=	new ListView(groupNameList)
  lazy val ui = new ImagePanel{
imagePath = ("sample.jpeg")
    val box3 = new BoxPanel(Orientation.Horizontal){

      contents+=create
      contents+=view
      
    }
    contents+=box3
       
     contents+=iconBox
//     listenTo(iconBox.selection) 
//       reactions += {
//        case SelectionChanged(`iconBox`) => 
//          val selected = iconBox.selection.items(0)
//          DisplayImage.startup(null)
//          self.setVisible(false)
//  }
    
  }
    def top = new MainFrame {
    title = "Login"
    minimumSize= new Dimension(600,600)
    ui.location.setLocation(250, 250)
    contents = ui
    listenTo(create, view)
        reactions+={
        case ButtonClicked(`create`) =>
        	//contents.head
        	var s = Dialog.showInput(contents.head, "Enter the group name :",title ="Create a new group", initial="Enter group")
        	s match {
        	case Some(str) => 
        	  println(str)
        	  if(str.equals("") || str.equals("Enter group")){
        	    Dialog.showMessage(contents.head, "Please enter group name")
        	  }
        	  else{
        	    if(Shutterbug.curnode.checkGroupExist(str)){
        	      Dialog.showMessage(contents.head, "Group name already exists")
        	    }
        	    else{
        	      UIinteraction.onCreateGroup(str)
        	      Dialog.showMessage(contents.head, "Group successfully created")
        	    }
        	  }
        	case None => 
        	  //println("case None")
        	}
        case ButtonClicked(`view`) =>
          try{
           val selected = iconBox.selection.items(0)
           val stringList:Array[String] = selected.split("/")
           val grp = Shutterbug.curnode.getGroupFromName(selected)
           if(grp == null){
             println("Error: missing group")
           }
           Shutterbug.curGroup = grp
           println("current group is "+Shutterbug.curGroup)
           var disp:DisplayImage = new DisplayImage()
           Shutterbug.mcs.setDispMap(grp.getName, disp)
           Shutterbug.mcs.setDispGrpMap(grp.getName)
           disp.setMain(self)
           disp.startup(null)
           self.setVisible(false)
          }
          catch{
            case e:Exception => Dialog.showMessage(contents.head, "Please select a group")
          }
      }
  }
    
    
    
}
