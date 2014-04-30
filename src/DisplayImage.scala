
import swing._
import event._
import java.util.Date
import scala.swing.GridPanel
import java.awt.Color
import java.awt.Image
import javax.imageio.ImageIO
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

			var curImg:String=""
			var list:List[ImageIcon]= List(new ImageIcon())
			var createGrp:java.awt.Component = null
			val displayField = new TextArea {
			  peer.setAlignmentX(0)
			}
				
				val chatArea = new TextArea(10,2) {
					        editable = false
							background = Color.WHITE
							//bound= Swing.bounds.
							//size=new Dime
					        lineWrap=true
					        wordWrap=true
					        //maximumSize = new Dimension(00,40)
					        //minimumSize = new Dimension(100,40)
					        //peer.setAlignmentX(0)
				}
				
				
			val imgLabel = new Label
			val uploaderLabel = new Label
			val saveBtn = new Button("Save Caption")
			val box1 = new BoxPanel(Orientation.Vertical)

			def getCurImg() : String={
					return curImg;
			}
			def updateImgLabel(img:ImageIcon){
				imgLabel.icon=img
			}

			def updateUploaderLabel(s:String){
			
		      uploaderLabel.text="Uploader : " +s
			}

			def updateDisplay(s:String){
				displayField.text=s+"\n"
			}
			
			def updateChatArea(chatText:String) {
				chatArea.append(chatText)
			}


			def setPicList(pics:List[ImageIcon]){
				list=pics
						ui.iconBox.listData_=(list)
						println(list.toString)
						println("list updated");
			}

			def setMain(mainframe:java.awt.Component){
				createGrp= mainframe

			}
			
			def showImage(img:ImageIcon){
			  val image:Image = img.getImage().getScaledInstance(600, 600, Image.SCALE_DEFAULT)
			var imgIcon = new ImageIcon(image)
			  Dialog.showMessage(null, "",title="Image",icon=imgIcon);
			}
			
			def showError(s:String){
			  Dialog.showMessage(null, s, "Error")
			}

			//	val back = new Button("Back")


			lazy val ui = new ImagePanelMain(){
			   imagePath = ("cam.jpeg")
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
					listenTo(mouse.clicks)
					reactions+={
					case e : MouseClicked =>{
				    var clickNum=e.clicks
				    //println("No of clicks" + clickNum)
				    if(clickNum==2)
				      UIinteraction.onDoubleClick(selection.items(0))
				      //Dialog.showMessage(contents.head, "",title="Image",icon=selection.items(0));
				      
				  }
				}
				}

				//	val button = new Button("Delete")

				//val box1 = new BoxPanel(Orientation.Vertical){
				  val labImage = new Label("Image Details")
				  labImage.foreground=java.awt.Color.white
				  labImage.font=new Font("Comic Sans MS",0,20)
					box1.contents+=labImage
					uploaderLabel.foreground=java.awt.Color.white
		            uploaderLabel.font=new Font("Comic Sans MS",0,20)
					box1.contents+=uploaderLabel
					val img =  new ImageIcon();
					imgLabel.icon=img
					box1.contents+=imgLabel
					displayField.maximumSize = new Dimension(100,40)
					displayField.minimumSize = new Dimension(100,40)
					
					displayField.lineWrap_=(true)
					displayField.wordWrap_=(true)
					displayField.rows=2
					box1.contents+=displayField
					//contents += new ScrollPane(displayField)
					box1.contents+=saveBtn
			//	}
				box1.border=Swing.EmptyBorder(0, 10, 0, 10)
				box1.background=new Color(0,0,0,0)
				box1.visible=false
					//val box00 = new BoxPanel(Orientation.Vertical){

					//val invite = new Button("Invite")
					//val username = new TextField()
					//					val box01 = new BoxPanel(Orientation.Horizontal){
					//						contents += button
					//						contents+=back
					//
					//					}
					//					box01.background=new Color(0,0,0,0)
					//					
					//					contents += box01
					contents += iconBox

//				}
//				box00.background=new Color(0,0,0,0)
//				box00.border = Swing.EmptyBorder(0, 0, 0, 10)
//				contents+=box00
				contents+=box1

				//ChatBox
				val label0 = new Label("Chat!")
				label0.horizontalAlignment=Alignment.Left
				//label0.peer.setAlignmentX(0)
				label0.foreground = java.awt.Color.white
				label0.font=new Font("Comic Sans MS",0,20)
				
				val inputField = new TextField()
				inputField.maximumSize_=(new Dimension(240,25))
				inputField.minimumSize_=(new Dimension(240,25))
				//inputField.horizontalAlignment=Alignment.Left
				//inputField.peer.setAlignmentX(0)
				
				val scroll = new ScrollPane(chatArea)
				scroll.verticalScrollBar
				//scroll.ho
				//scroll.peer.setAlignmentX(0)
				
				//val sendButton = new Button("Send")
				val box0 = new BoxPanel(Orientation.Vertical){
				contents += label0
				contents += scroll
				contents += inputField
				//contents += sendButton

				}
				
				
				box0.background=new Color(0,0,0,0)
				box0.border = Swing.EmptyBorder(400, 10, 0, 0)
			
				//box0.
				//box0.peer.setAlignmentY(0)
				
						//box0.preferredSize = (new Dimension(100,100))
						contents += VStrut(10)
						contents += box0
						contents += VStrut(10)
						//			contents += new BorderPanel {
						//				add(iconBox, BorderPanel.Position.West)
						//			}


						//Selection Change listener
						listenTo(iconBox.selection)
						reactions +={
						case SelectionChanged(`iconBox`) =>{

							if(iconBox.selection.items.size>0){
								box1.visible=false
										var selectedIcon:ImageIcon =iconBox.selection.items(0)
										curImg=calculate_hash.md5_img("png",picture.convertToBI(selectedIcon))
										println(selectedIcon);
								var imgLkp:LookupMsg= new LookupMsg
										imgLkp=UIinteraction.onImageSelect(selectedIcon)
										updateUploaderLabel(imgLkp.getUploader)
										updateImgLabel(selectedIcon)
										println("current caption" + imgLkp.getCaption);
								updateDisplay(imgLkp.getCaption)
								box1.visible=true

							}
						}

				}

				listenTo(saveBtn,inputField)
				reactions+={
				case ButtonClicked(`saveBtn`) => {
					try{
						var caption : String = " "+displayField.text
								caption=caption.trim()
								UIinteraction.onSaveCaption(caption, iconBox.selection.items(0))
								println(caption)
					}
					catch{
					case e:Exception => Dialog.showMessage(contents.head, "Please select image for the caption")
					}
				}
				
//				case ButtonClicked(`sendButton`)=>{
//				   var chatText:String = inputField.text
//			       updateChatArea(Shutterbug.curnode.getName + " : "+chatText+"\n")
//				   inputField.text=""
//			       UIinteraction.onChat(chatText)			       
//			       		  
//				  
//				}
				
				case EditDone(`inputField`) => {
				   var chatText:String = inputField.text
				   
				   if(chatText.length()!=0){
			       updateChatArea(Shutterbug.curnode.getName + " : "+chatText+"\n")
				   inputField.text=""
			       UIinteraction.onChat(chatText)	
				   }
				}			
				
				
				}
				
				
				


				//				listenTo(button)
				//				reactions += {
				//				case ButtonClicked(`button`) =>
				//				try{
				//					var result = Dialog.showConfirmation(contents.head, "Are you sure you want to delete these " + iconBox.selection.items.size + " image?" ,
				//							icon=iconBox.selection.items(0));
				//
				//					if (result == Dialog.Result.Yes)
				//					{
				//						Shutterbug.mcs.multicastDelete(iconBox.selection.items(0), Shutterbug.curGroup.getName)		  
				//					}
				//
				//
				//				}
				//				catch{
				//				case e:Exception => Dialog.showMessage(contents.head, "Please select image for deletion")
				//				}
				//			
				//				}

			}

			def top = new MainFrame {
				Shutterbug.mcs.displayRefreshBuf(Shutterbug.curGroup.getName)
				//self.open()
				title = Shutterbug.curGroup.getName//Shutterbug.curnode.getName
				minimumSize= new Dimension(600,600)
				iconImage_=(new ImageIcon("frameIcon.png").getImage())
				resizable=false

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
										
					contents += new MenuItem(Action("Delete"){
						try{
						    
							var result = Dialog.showConfirmation(contents.head, "Are you sure you want to delete this image?" ,
									icon=ui.iconBox.selection.items(0));

							if (result == Dialog.Result.Yes)
							{
								Shutterbug.mcs.multicastDelete(ui.iconBox.selection.items(0), Shutterbug.curGroup.getName)		  
							}

							box1.visible=false
						}
						catch{
						case e:Exception => Dialog.showMessage(contents.head, "Please select image for deletion")
						}

					})
					
				contents += new MenuItem(Action("Back"){
						Shutterbug.mcs.unsetDispGrpMap(Shutterbug.curGroup.getName)
						createGrp.setVisible(true)
						close 

					})
												
					contents += new MenuItem(Action("Download"){
						UIinteraction.onDownload

					})
					
//					contents += new MenuItem(Action("Upload to Facebook"){
//						
//
//					})
				
					//      contents += new Menu("Delete") 
				}

				//				listenTo(back)
				//				reactions+={
				//				case ButtonClicked(`back`) =>
				//				//CreateGroup.setVisible()
				//				// Shutterbug.check_inside=0
				//				Shutterbug.mcs.unsetDispGrpMap(Shutterbug.curGroup.getName)
				//				createGrp.setVisible(true)
				//				close
				//				// CreateGroup.top.setVisible(true)
				//				//CreateGroup.startup(args)
				//				}
				val outputTextScrollPane = new ScrollPane(ui)
				contents = outputTextScrollPane

			}
}

