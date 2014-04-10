import javax.swing.ImageIcon
import java.awt.Image
import scala.swing.Dialog
import java.awt.image._;
import javax.imageio.ImageIO;
import java.awt.Graphics2D

object picture {
  
	def convertToBI(ic : ImageIcon) : BufferedImage ={
			var image:Image = null                              
			image = ic.getImage();
			var bufferedImage:BufferedImage = null
					
			// Create a buffered image with transparency
			bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

			// Draw the image on to the buffered image
			var bGr:Graphics2D = bufferedImage.createGraphics();
			bGr.drawImage(image, 0, 0, null);
			bGr.dispose();
			
			return bufferedImage;
	  
	}
	
	def getPicFormat(fp:String) : String = {
			var format:String=null;
			format=fp.substring(fp.lastIndexOf('.')+1).trim();
			return format;
	}
	
	def createThumb(ic : ImageIcon) : BufferedImage ={
			var image:Image = null                              
			image = ic.getImage();
			var bufferedImage:BufferedImage = null
					
			// Create a buffered image with transparency
			bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			// Draw the image on to the buffered image
			var bGr:Graphics2D = bufferedImage.createGraphics();
			bGr.drawImage(image, 0, 0, null);
			bGr.dispose();
			
			return bufferedImage;
	  
	}

}

