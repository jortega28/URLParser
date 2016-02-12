package persistent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class BalTree{
	
	final int M = 4; 
	
	private Node root;             
    private int HT;                
    private int N;
    private String url;
    private String modified;
    private long startPos;
    private long endPos;
    private int size = 0;
    
    RandomAccessFile raf = Main.getRAF();
    FileChannel fc = raf.getChannel();
    
    public BalTree() throws IOException {
    	recordPosition(raf.getFilePointer(),false);
    	root = new Node(0);
    }
    public int getM(){
    	return M;
    }
    
    public int size() { 
    	return N; 
    }

    public int height() { 
    	return HT; 
    }
    
    public int nodes(){
    	return N;
    }
    
    //inc = increment if found
    public int get(String key, boolean inc) { 
    	return search(root, key, HT, inc); 
    }
    
    private int search(Node x, String key, int ht, boolean inc) {
        Entry[] children = x.children;

        if (ht == 0) {
            for (int j = 0; j < x.m; j++) {
                if (eq(key.toLowerCase(), children[j].key.toLowerCase()) && !inc){
                	return children[j].value;
                }else if(eq(key.toLowerCase(), children[j].key.toLowerCase()) && inc){
                	children[j].incCount();
                	return children[j].value;
                }
            }
        }else {
            for (int j = 0; j < x.m; j++) {
                if (j+1 == x.m || less(key.toLowerCase(), children[j+1].key.toLowerCase())){
                    return search(children[j].next, key, ht-1, inc);
                }
            }
        }
        return -1;
    }
    
    public void writeTree() throws IOException{
    	String urlCapture = url;
    	int difference = 300-urlCapture.length();
		for(int i = 0; i < difference; i++){
			urlCapture = urlCapture + " ";
		}
		String modifiedCapture = modified;
		int difference2 = 20-modifiedCapture.length();
		for(int i = 0; i < difference2; i++){
			modifiedCapture = modifiedCapture + " ";
		}
		String writeMe = urlCapture+modifiedCapture;
		raf.write(writeMe.getBytes());
    	captureTree(root);
    	recordPosition(raf.getFilePointer()+1, true);
    }
   
	private int captureTree(Node x) throws IOException {
    	if(x != null){
        	writeBytes(x.getBytes());
    	    for(int i = 0; i < x.m; i++){
    	    	if(x.children[i] != null){
	    	    	captureTree(x.children[i].next);
    	    	}
    	    }
	    }
    	return 0;
    }
    
    public void put(String key) throws IOException {
    	size++;
    	if(get(key,true) != -1) return;
    	
        Node u = insert(root, key, 1, HT); 
        N++;
        if (u == null) return;

        // need to split root
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, root);
        t.children[1] = new Entry(u.children[0].key, u);
        root = t;
        HT++;
    }
    
    private Node insert(Node h, String key, int value, int ht) throws IOException {
        int j;
        Entry t = new Entry(key, null);

        // external node
        if (ht == 0) {
            for (j = 0; j < h.m; j++) {
                if (less(key, h.children[j].key)) break;
            }
        }

        // internal node
        else {
            for (j = 0; j < h.m; j++) {
                if ((j+1 == h.m) || less(key, h.children[j+1].key)) {
                    Node u = insert(h.children[j++].next, key, value, ht-1);
                    if (u == null) return null;
                    t.key = u.children[0].key;
                    t.next = u;
                    break;
                }
            }
        }

        for (int i = h.m; i > j; i--) h.children[i] = h.children[i-1];
        h.children[j] = t;
        h.m++;
        if (h.m < M) return null;
        else         return split(h);
    }
    
    private void recordPosition(long l,Boolean end) throws IOException{
    	FileWriter fw = new FileWriter("positions.txt",true);
    	String pos = "";
    	if(end){
    		this.setEndPos(l);
        	pos = pos+l;
        	fw.write(pos);
        	fw.write(" ");
        	raf.seek(l+1);
    	}else{
    		this.setStartPos(l);
    		pos = pos + l;
    		fw.write(pos);
        	fw.write(" ");
    	}
    	fw.close();
    }
    
    private void writeBytes(String bytes) throws IOException{
    	if(!bytes.isEmpty() || bytes.trim().length() > 0){
    		raf.write(bytes.getBytes());
    	}
    }

    public void closeRAF() throws IOException{
    	raf.close();
    }
    
    // split node in half
    private Node split(Node h) {
        Node t = new Node(M/2);
        h.m = M/2;
        for (int j = 0; j < M/2; j++)
            t.children[j] = h.children[M/2+j]; 
        return t;    
    }
    
    private boolean less(String k1, String k2) {
    	if(k1.compareTo(k2) < 0){
    		return true;
    	}
        return false;
    }

    private boolean eq(String k1, String k2) {
    	if(k1.compareTo(k2) == 0){
    		return true;
    	}
        return false;
    }
    
    public void setURL(String url){
    	this.url = url;
    }
    
    public String getURL(){
    	return url;
    }
	public long getStartPos() {
		return startPos;
	}
	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}
	public long getEndPos() {
		return endPos;
	}
	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}
	public String getLastModified() {
		return modified;
	}
	public void lastModified(String modified) {
		this.modified = modified;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
    
}
