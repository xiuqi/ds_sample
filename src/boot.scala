
import java.net._;
import java.io._;
import java.net.InetAddress;
import java.net.InetAddress;
import java.util.Enumeration;
import java.net.NetworkInterface;

object boot {
	def getBoot() : String = {
	  var res:String = ""
	  try{
			val myIP:URL = new URL("http://myip.dnsomatic.com/");
			  val in:BufferedReader  = new BufferedReader(
			                       new InputStreamReader(myIP.openStream())
			                      );
			  res = in.readLine().toString()
		}
		catch{
		  case e:  IOException => e.printStackTrace()
		} 
      return res 
	}
}