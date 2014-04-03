// Simple server
import java.net._
import java.io._
import scala.io._
import scala.actors.Actor._
import scala.actors.remote.RemoteActor._
import javax.swing.ImageIcon
import scala.swing.Dialog
import java.awt.image.BufferedImage
import java.awt.Image
import javax.imageio.ImageIO
import java.awt.Graphics2D
import msgKind._

class RefreshThread extends Runnable {
  def run(){
	  while (true)
	  {
	  Shutterbug.curnode.displayRefreshBuf
	  Thread.sleep(20000)
	  }
    
	}
  
  
}