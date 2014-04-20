
import java.util.Observable
import java.io.File


class GraphicsModel extends Observable {
  val image = new UserImage // image

  /**
   * Open a file
   * @param file
   * @return opened or not
   */
  def open(file: File): Boolean = {
    val opened = image.open(file)
    if (opened) {
      setChanged
      notifyObservers
    }
    opened
  }
}
