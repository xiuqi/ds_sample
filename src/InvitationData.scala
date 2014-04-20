import java.util.HashMap
import java.util.ArrayList
import javax.swing.ImageIcon
import java.util.TreeMap

class InvitationData extends Serializable{
	var inv_refresh:HashMap[String, LookupMsg] = null
	var inv_display:ArrayList[ImageIcon] = null
	var inv_members:TreeMap[String,UserNode] = null
	
	def set_refresh(in_refresh:HashMap[String, LookupMsg]) {
	  inv_refresh = in_refresh
	}
	
	def set_display(in_display:ArrayList[ImageIcon]) {
	  inv_display = in_display
	}
	
	def set_members(in_members:TreeMap[String,UserNode]) {
	  inv_members = in_members
	}
	
	def get_refresh(): HashMap[String, LookupMsg] = {
	  return inv_refresh
	}
	
	def get_display(): ArrayList[ImageIcon] = {
	  return inv_display
	}
	
	def get_members():TreeMap[String,UserNode] = {
	  return inv_members
	}	
}