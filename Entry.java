package persistent;

public class Entry {
	String key;
    int value = 1;
    Node next;     // helper field to iterate over array entries
    public Entry(String key, Node next) {
        this.key   = key;
        this.next  = next;
    }
    
    public void setKey(String key){
    	this.key = key; 
    }
	
    public void incCount(){
    	value++;
    }
    
}
