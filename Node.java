package persistent;

public class Node {

	private int M = 4;
	int max = 20;
	
	int m;                             // number of children
    Entry[] children = new Entry[M];   // the array of children
    public Node(int k) { 
    	m = k; 
    }
    
    public String getWords(){
    	String words = "";
    	for(int i = 0; i < children.length; i++){
    		if(children[i] != null){
    			words = words + children[i].key;
    		}
    	}
    	return words;
    }
    
    public String getBytes(){
    	String allBytes = "";
    	for(int i = 0; i < children.length; i++){
    		if(children[i] == null){}
    		else{
    			if(children[i].value > 1){
	    			String word = children[i].key;
	    			int sLength = 0;
	    			sLength = children[i].key.length();
	    			int difference = max-sLength;
	    			if(difference >= 0){
	    				for(int j = 0; j < difference; j++){
	    					word = word + " ";
	    				}
	    				String count = "" + children[i].value;
	    				int cLength = count.length();
	    				int cDifference = 10-cLength;
	    				for(int j = 0; j < cDifference; j++){
	    					count = count + " ";
	    				}
	    				word = word + count;
	    				allBytes = allBytes + word;
	    			}
    			}
    		}
    	}
    	return allBytes;
    }
    
    public boolean isFull(){
    	int count = 0;
    	
    	for(int i = 0; i < children.length; i++){
    		if(children[i] != null)
    			count++;
    	}
    	
    	if(count == M)
    		return true;
    	
    	return false;
    }
    
}
