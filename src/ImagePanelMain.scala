
import swing._
import event._
import java.util.Date
import Swing._
import scala.swing.GridPanel
import java.awt.Color
import java.text.SimpleDateFormat
import java.awt.Dimension
import swing.event.Key
import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.{JOptionPane, KeyStroke}
import javax.swing.{Icon, ImageIcon}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.awt.AlphaComposite

class ImagePanelMain extends BoxPanel(Orientation.Horizontal)                                               
{                                                                             
  private var _imagePath = ""                                                 
//  private var bufferedImage:BufferedImage = ImageIO.read(new File(_imagePath))                               
private var bufferedImage:BufferedImage = null    
  def imagePath = _imagePath                                                  

  def imagePath_=(value:String)                                               
  {                                                                           
    _imagePath = value                                                        
    bufferedImage = ImageIO.read(new File(_imagePath)) 
  }                                                                           


  override def paintComponent(g:Graphics2D) =                                 
  {                                                                           
    if (null != bufferedImage) g.drawImage(bufferedImage, 0, 0, null)         
  }                                                                           
}                                                                             

object ImagePanelMain                                                             
{                                                                             
  def apply() = new ImagePanelMain()                                              
} 