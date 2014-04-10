import swing._
import event._
import java.util.Date
import scala.swing.GridPanel
import java.awt.Color
import java.text.SimpleDateFormat
import javax.swing.{Icon, ImageIcon}
import ComboBox._

/**
 * Demonstrates how to use combo boxes and custom item renderers.
 * 
 * TODO: clean up layout
 */
object DisplayImage extends SimpleSwingApplication {

	var list:List[ImageIcon]=null
			
	def setPicList(pics:List[ImageIcon]){
	
	
	list=pics
	ui.iconBox.listData_=(list)
	println(list.toString)
	println("list updated");
}


//def update(){
//  ui.iconBox.listData_=(list)
//	//ui.revalidate
//	//ui.repaint
//	//.revalidate
//}

lazy val ui = new FlowPanel {
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

	
	contents += iconBox
			val button = new Button("Delete") 
	contents += button

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
		
		}

}

def top = new MainFrame {
	title = "Album"
	val outputTextScrollPane = new ScrollPane(ui)
	contents = outputTextScrollPane

}
}
