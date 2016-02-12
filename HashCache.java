package persistent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class HashCache {
    private static int TABLE_SIZE = 128;
    private int size = 0;
    private int items = 0;
    private float loadFactor = 0.75f;
    private int threshold = (int)(TABLE_SIZE * loadFactor);
    
    private long startPos = -1;
    private long endPos = -1;
    
    RandomAccessFile raf = Main.getRAF();
    
    HashEntry[] table;

    HashCache() {
        table = new HashEntry[TABLE_SIZE];
        for (int i = 0; i < TABLE_SIZE; i++)
              table[i] = null;
    }
    
    protected void rehash() throws IOException{
    	int oldCapacity = table.length;
    	HashEntry oldTable[] = table;
    	
    	TABLE_SIZE = oldCapacity * 2 + 1;
    	HashEntry newTable[] = new HashEntry[TABLE_SIZE];
    	
    	threshold = (int)(TABLE_SIZE * loadFactor);
    	table = newTable;
    	
    	for(HashEntry hashEntry : oldTable) {
    		if(hashEntry != null){
            	put(hashEntry.getURL());
    		}
        }
    	
    }

    public String getModified(String URL) {
        int hash = (URL.length() % TABLE_SIZE);
        while (table[hash] != null && !table[hash].getURL().equalsIgnoreCase(URL))
              hash = (hash + 1) % TABLE_SIZE;
        if (table[hash] == null)
              return "Not Here!";
        else
              return table[hash].getModified();
    }
    
    public void setModified(String URL, String modified) {
        int hash = (URL.length() % TABLE_SIZE);
        while (table[hash] != null && !table[hash].getURL().equalsIgnoreCase(URL))
              hash = (hash + 1) % TABLE_SIZE;
        if (table[hash] == null){}
        else{
              table[hash].setModified(modified);
        }
    }
    
    public void put(String URL) throws IOException {
    	if(items >= threshold){
    		System.out.println("Resizing Hashtable to... " + TABLE_SIZE);
    		rehash();
    	}
        int hash = (URL.length() % TABLE_SIZE);
        while (table[hash] != null && !table[hash].getURL().equalsIgnoreCase(URL))
              hash = (hash + 1) % TABLE_SIZE;
        table[hash] = new HashEntry(URL, getURLModified(URL));
        //recordTop(new HashEntry(word, count));
        size++;
        items++;
    }
    
    public void updateAllModified() throws IOException{
    	for(HashEntry entries:table){
    		if(entries != null){
	    		String URL = entries.getURL();
	    		try{
	    	    	URL url = new URL(URL);
	    	    	URLConnection connection = url.openConnection();
	    	    	connection.connect();
	    	    	long time = connection.getLastModified();
	    	    	if(time == 0)
	    	    		entries.setModified("N/A");
	    	    	else{
	    	    		String temp = "" + new Date(time);
	    	    		entries.setModified(temp);
	    	    	}
	        	}catch(IOException e){}
    		}
    	}
    	updateCacheToRAF();
    }
    
    public String getURLModified(String URL){
    	String modified = "N/A";
    	try{
	    	URL url = new URL(URL);
	    	URLConnection connection = url.openConnection();
	    	connection.connect();
	    	long time = connection.getLastModified();
	    	if(time == 0)
	    		return modified = "N/A";
	    	else
	    		modified = "" + new Date(time);
	    	return modified;
    	}catch(IOException e){
    		return modified;
    	}
    }
    
    public void updateCacheToRAF() throws IOException{
    	LinkedList<String> file = new LinkedList<String>();
    	File f = new File("positions.txt");
    	Scanner sc = new Scanner(f);
    	while(sc.hasNext()){
    		String next = sc.next();
    		if(next.trim().equalsIgnoreCase("c")){
    			sc.next();
    			sc.next();
    		}else{
    			file.add(next.trim());
    		}
    	}
    	FileOutputStream newFile = new FileOutputStream("positions.txt", false);
    	FileWriter fw = new FileWriter("positions.txt",true);
    	for(int i = 0; i < file.size();i++){
    		fw.write(file.get(i));
    		fw.write(" ");
    	}
    	sc.close();
    	Main.openFW();
    	String entry = getBytes();
    	if(!entry.isEmpty()){
    		updateRAF();
    		startPos = raf.getFilePointer();
    		String start = "" + startPos;
    		fw.write("c ");
    		fw.write(start);
    		fw.write(" ");
    		raf.write(entry.getBytes());
    		endPos = raf.getFilePointer();
    		String end = "" + endPos;
    		fw.write(end);
    		fw.write(" ");
    		raf.seek(raf.getFilePointer()+1);
    	}
    	fw.close();
    }
    
    public void writeCacheToRAF() throws IOException{
    	String entry = getBytes();
    	if(!entry.isEmpty()){
    		updateRAF();
    		FileWriter fw = Main.getFW();
    		startPos = raf.getFilePointer();
    		String start = "" + startPos;
    		fw.write("c ");
    		fw.write(start);
    		fw.write(" ");
    		raf.write(entry.getBytes());
    		endPos = raf.getFilePointer();
    		String end = "" + endPos;
    		fw.write(end);
    		fw.write(" ");
    		raf.seek(raf.getFilePointer()+1);
    	}
    }
    
    private String getBytes(){
    	String entry = "";
    	LinkedList<String> allEntries = new LinkedList<String>();
    	for(HashEntry entries:table){
    		entry = "";
    		if(entries != null){
    			entry = entry + entries.getURL();
    			int sLength = entry.length();
    			int difference = 300-sLength;
    			if(difference >= 0){
    				for(int j = 0; j < difference; j++){
    					entry = entry + " ";
    				}
    				entry = entry + entries.getModified();
    				int sLength2 = entries.getModified().length();
    				int difference2 = 20 - sLength2;
    				for(int j = 0; j < difference2; j++){
    					entry = entry + " ";
    				}
    			}
    		}
    		allEntries.add(entry);
    	}
    	if(!allEntries.isEmpty()){
    		for(String thing: allEntries){
    			entry = entry + thing;
    		}
    	}else{
    		entry = "";
    	}
    	return entry;
    }
    
    private void updateRAF(){
    	raf = Main.getRAF();
    }
    
    public int size(){
    	return size;
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

}
