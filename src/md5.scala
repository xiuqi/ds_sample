
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object calculate_hash {
  def md5(s: String) :String= {
    	  	//String s = "password";
        var generatedPassword:String = null;
        try {
            // Create MessageDigest instance for MD5
            val md:MessageDigest = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(s.getBytes());
            //Get the hash's bytes 
            val bytes:Array[Byte] = md.digest();
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            val sb:StringBuilder = new StringBuilder();
            var i:Int = 0
            while(i<bytes.length){
              sb.append(Integer.toString((bytes(i) & 0xff) + 0x100, 16).substring(1));
              i = i+1
            }

            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } 
        catch 
        {
          case e:NoSuchAlgorithmException =>
            e.printStackTrace();
        }
        return generatedPassword
      }
}