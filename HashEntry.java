package persistent;

public class HashEntry {

	private String URL;
	private String modified;
	HashEntry next;
	
	HashEntry(String URL, String modified){
		this.URL = URL;
		this.modified = modified;
	}
	
	public String getURL(){
		return URL;
	}
	
	public String getModified(){
		return modified;
	}
	
	public void setModified(String modified){
		this.modified = modified;
	}
	
}
