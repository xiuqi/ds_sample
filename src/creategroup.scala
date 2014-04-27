

import swing._
import scala.swing.BorderPanel.Position._
import java.awt.Point
import scala.swing.event.ButtonClicked
import scala.swing.event.SelectionChanged
import javax.swing.{Icon, ImageIcon}
//import javax.swing.text.html.BackgroundImage

object  CreateGroup extends SimpleSwingApplication{
  
  var groupNameList:List[String] = List()
  def setGroupNameList(newNameList:List[String]){
    groupNameList = newNameList
   
//    iconBox.background=new Color(0,0,0,0)
//    iconBox.foreground=java.awt.Color.white
//    box3.background=new Color(0,0,0,0)
    iconBox.listData = (groupNameList)
   }
  val create = new Button("Create Group")
  val view = new Button("View Group")
  val iconBox=	new ListView(groupNameList)
  val box3 = new BoxPanel(Orientation.Horizontal)
  val box11 = new BoxPanel(Orientation.Horizontal)
  val buttonBox = new BoxPanel(Orientation.Horizontal)
  
  lazy val ui = new ImagePanel{
	  imagePath = ("cam.jpeg")

	  buttonBox.contents+=create
      buttonBox.contents+=view
  	  buttonBox.background_=(new Color (0,0,0,0))
	  buttonBox.border=Swing.EmptyBorder(2, 2, 100, 100)
  
	val box1 = new BoxPanel(Orientation.Vertical){
	    val label = new Label("Welcome " + Shutterbug.curnode.getName +"!")
	    label.foreground=java.awt.Color.white
	    label.border=Swing.EmptyBorder(0, 0, 8, 0)
	    label.font= new Font("Comic Sans MS",0,36)
	    contents+=label
	    val label1 = new Label("Your groups:")
	    label1.foreground=java.awt.Color.white
	    label1.font= new Font("Comic Sans MS",0,30)
	    contents+=label1
	  }
	 
	 box1.background_=(new Color (0,0,0,0))
	 box1.border=Swing.EmptyBorder(2, 2, 30, 100)
	 buttonBox.background_=(new Color (0,0,0,0))
	 buttonBox.border=Swing.EmptyBorder(20, 0, 2, 2)
	 box11.contents+=box1
	 box11.contents+=buttonBox
	 box11.background_=(new Color (0,0,0,0))
	 box11.border=Swing.EmptyBorder(5, 10, 4, 10)
	 contents+=box11
	
	iconBox.font=new Font("Comic Sans MS",0,15)
    //iconBox.background=new Color(0,0,0,0)
	//iconBox.foreground=java.awt.Color.white
	//box3.contents+=iconBox
	
	//box3.border=Swing.EmptyBorder(2, 2, 2, 2)
    //box3.background=new Color(0,0,0,0)
   
    contents+=iconBox
    
    
  }
    def top = new MainFrame {
    title = Shutterbug.curnode.getName+"'s Groups"
    minimumSize= new Dimension(600,600)
    maximumSize= new Dimension(600,600)
    iconImage_=(new ImageIcon("frameIcon.png").getImage())
    resizable=false
    ui.location.setLocation(250, 250)
    val outputTextScrollPane = new ScrollPane(ui)
	contents = outputTextScrollPane
    //contents = ui
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
