import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class UserImage {
  private var img: BufferedImage = null // buffered image

  /**
   * Open a file
   * @param file
   * @return opened or not
   */
  def open(file: File): Boolean = {
    img = ImageIO.read(file)
    return img match {
      case null => false
      case _    => true
    }
  }

  /**
   * @return image width
   */
  def width: Int = img.getWidth

  /**
   * @return image height
   */
  def height: Int = img.getHeight

  /**
   * @return buffered image
   */
  def buffer: BufferedImage = img
}
