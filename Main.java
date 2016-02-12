package persistent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	
	private static LinkedList<String> common = new LinkedList<String>();
	private static LinkedList<BalTree> trees = new LinkedList<BalTree>();
	private static LinkedList<String> externalLinks = new LinkedList<String>();
	private static LinkedList<String> baseLinks = new LinkedList<String>();
	private static HashCache cache = new HashCache();
	static RandomAccessFile raf;
	static FileWriter fw;
	private static FileChannel fc;
	
	public static void main(String[] args) throws IOException {
		
		File f = new File("RandomAccess.txt");
		RandomAccessFile raftemp = new RandomAccessFile(f,"rw");
		raf = raftemp;
		fc = raf.getChannel();
		
		readCommonWords();
		
		if(firstTime()){}
		
		GUI gui = new GUI();
		gui.runGUI();
		
		closeFW();
		
	}
	
	public static void updatePages() throws IOException{
		try{
		cache.updateAllModified();
		for(int i = 0; i < trees.size(); i++){
			String bURL = trees.get(i).getURL();
			String cMod = cache.getModified(bURL);
			String bMod = trees.get(i).getLastModified();
			if(!cache.getModified(bURL).equalsIgnoreCase(bMod)){
				long oldStart = trees.get(i).getStartPos();
				String oldStartS = "Start: " + oldStart;
				System.out.println(oldStartS);
				long oldEnd = trees.get(i).getEndPos();
				String oldEndS = "End: " + oldEnd;
				System.out.println(oldEndS);
				String URL = trees.get(i).getURL();
				
				URL temp = new URL(URL);
				Scanner in = new Scanner(temp.openStream());
				
				for(int j = 0; j < trees.size(); j++){
					if(trees.get(j).getURL().equals(URL)){
						trees.remove(j);
					}
				}
				
				BalTree temptree = new BalTree();
				temptree.setURL(URL);
				temptree.lastModified(cache.getURLModified(URL));
				trees.add(temptree);
				
				while (in.hasNextLine()){
					String urlLine = in.nextLine();
					String parsed = Jsoup.parse(urlLine).text();
					wordFrequency(URL,parsed);
				}
				
				//Now remove from positions file
				LinkedList<String> file = new LinkedList<String>();
		    	File f = new File("positions.txt");
		    	Scanner sc = new Scanner(f);
		    	while(sc.hasNext()){
		    		String next = sc.next();
		    		if(next.trim().equalsIgnoreCase(oldStartS)){
		    			sc.next();
		    		}else{
		    			file.add(next.trim());
		    		}
		    	}
		    	FileOutputStream newFile = new FileOutputStream("positions.txt", false);
		    	FileWriter fw = new FileWriter("positions.txt",true);
		    	for(int l = 0; l < file.size(); l++){
		    		fw.write(file.get(l));
		    		fw.write(" ");
		    	}
		    	fw.close();
				
				for(int k = 0; k < trees.size(); k++){
					if(trees.get(k).getURL().equals(URL)){
						captureTree(trees.get(k));
					}
				}
				
			}
		}
		}catch(UnknownHostException e){}
	}
	
	public static String compareURL(String userURL){
		try{
			LinkedList<String> userWords = new LinkedList<String>();
			LinkedList<MatchScore> matchScores = new LinkedList<MatchScore>();
			
			URL temp = new URL(userURL);
			Scanner in = new Scanner(temp.openStream());
			
			cache.put(userURL);
			
			while(in.hasNextLine()){
				String urlLine = in.nextLine();
				String parsed = Jsoup.parse(urlLine).text();
				
				Scanner sc = new Scanner(parsed);
				
				//Add to bTree and LL
				while(sc.hasNext()){
					String word = sc.next();
					word = trim(word);
					if(word.length() <= 20){
						if(!common.contains(word) || !common.contains(word.toLowerCase())){
							userWords.add(word.toLowerCase());
							
						}
					}
				}
				//Done adding
			}
			int score = 0;
			for(int i = 0; i < trees.size(); i++){
				
				if(!trees.get(i).getURL().equalsIgnoreCase(userURL)){
					for(int j = 0; j < userWords.size(); j++){
						if(trees.get(i).get(userWords.get(j), false) > 1){
							score++;
						}
					}
					//Balance metric
					double balance;
					
					if(userWords.size() < trees.get(i).getSize()){
						balance = (double) (userWords.size())/(double) (trees.get(i).getSize());
					}else{
						balance = (double) (trees.get(i).getSize())/(double) (userWords.size());
					}
					score = (int) (score*balance);
					//Bm end
					MatchScore tempm = new MatchScore(trees.get(i).getURL(),score);
					matchScores.add(tempm);
					score = 0;
				}
			}
			
			//Now that you have all the match scores in the LL
			//Find all the larger match scores in the LL
			//Return the top three
			int firstBestMS = 0;
			int secondBestMS = 0;
			int thirdBestMS = 0;
			String firstURL = "none";
			String secondURL = "none";
			String thirdURL = "none";
			for(MatchScore ms: matchScores){
				if(ms.getScore() > firstBestMS){
					//First set second to third
					thirdURL = secondURL;
					thirdBestMS = secondBestMS;
					//Set first to second
					secondURL = firstURL;
					secondBestMS = firstBestMS;
					//Now set new score and url
					firstURL = ms.getURL();
					firstBestMS = ms.getScore();
				}else if(ms.getScore() > secondBestMS){
					//First set second to third
					thirdURL = secondURL;
					thirdBestMS = secondBestMS;
					//Now set new score and url
					secondURL = ms.getURL();
					secondBestMS = ms.getScore();
				}else if(ms.getScore() > thirdBestMS){
					thirdURL = ms.getURL();
					thirdBestMS = ms.getScore();
				}
			}
			in.close();
			return "Best Match: " + firstURL + "\nSecond Best Match: " + secondURL + "\nThird Best Match: " + thirdURL;
		}catch(IOException e){
			return "Invalid URL";
		}
	}
	
	private static void updateRAF() throws NumberFormatException, IOException{
		File f = new File("positions.txt");
		Scanner sc = new Scanner(f);
		while(sc.hasNext()){
			String next = sc.next();
			if(!next.equals("c")){
				raf.seek((long) Integer.parseInt(next));
			}
		}
		sc.close();
	}
	
	public static Boolean firstTime() throws IOException{
		if(checkPosFile()){
			System.out.println("Re-building Trees and Updating Pages...");
			updateRAF();
			//We will rebuild the btrees using the positions file
			rebuild();
			//Then check for updated pages
			updatePages();
			cache.updateCacheToRAF();
			return false;
		}else{
			//Start reading all the pages
			PrintWriter posTrack = new PrintWriter("positions.txt","UTF-8");
			fw = new FileWriter("positions.txt",true);
			readPages();
			readExternalLinks();
			
			return true;
		}
	}
	
	public static void rebuild() throws IOException{
		LinkedList<String> positions = new LinkedList<String>();
		//LinkedList<String> extracted = new LinkedList<String>();
		LinkedList<Integer> posExtracted = new LinkedList<Integer>();
		LinkedList<MatchScore> extracted = new LinkedList<MatchScore>();
		File f = new File("positions.txt");
		Scanner sc = new Scanner(f);
		
		while(sc.hasNext()){
			positions.add(sc.next());
		}
		
		for(int i = 0; i < positions.size(); i=i+2){
			if(positions.get(i).equals("c")){
				int pos1 = Integer.parseInt(positions.get(i+1));
				int pos2 = Integer.parseInt(positions.get(i+2));
				
				String tempS = "";
				//long currentPos = pos1;
				int counter = pos1;
				
				while(counter <= pos2-1){
					fc.position(counter);
					//System.out.println(counter);
					ByteBuffer bb = ByteBuffer.allocate(10);
					int bytes = fc.read(bb);
					bb.flip();
					while(bb.hasRemaining()){
						tempS = tempS + (char) bb.get();
					}
					
					counter = counter+10;
				}
				int count = 0;
				if(tempS.trim().length() > 0){
					while(count <= tempS.length()){
						try{
							String nextValues = tempS.substring(count, count+320);
							
							String first = nextValues.substring(0, 300);
							String second = nextValues.substring(300, 320);
							
							//Put in cache now after trimming
							cache.put(first.trim());
							cache.setModified(first.trim(), second.trim());
						}catch(StringIndexOutOfBoundsException e){
							count = tempS.length()+1;
						}
						count = count+320;
					}
				}
				
				i++;
			}else{
				int pos1 = Integer.parseInt(positions.get(i));
				int pos2 = Integer.parseInt(positions.get(i+1));
				posExtracted.add(pos1);
				posExtracted.add(pos2);
				String tempS = "";
				//long currentPos = pos1;
				int counter = pos1;
				
				while(counter <= pos2-1){
					fc.position(counter);
					//System.out.println(counter);
					ByteBuffer bb = ByteBuffer.allocate(10);
					int bytes = fc.read(bb);
					bb.flip();
					while(bb.hasRemaining()){
						tempS = tempS + (char) bb.get();
					}
					
					counter = counter+10;
				}
				extracted.add(new MatchScore(tempS,pos1,pos2));
			}
		}
		int count;
		for(int i = 0; i < extracted.size(); i++){
			int startPos = extracted.get(i).getScore();
			int endPos = extracted.get(i).getAdditional();
			String startSPos = "" + startPos;
			String endSPos = "" + endPos;
			if(extracted.get(i).getURL().trim().length() > 0){
				count = 0;
				String line = extracted.get(i).getURL();
				BalTree temptree = new BalTree();
				temptree.setStartPos(startPos);
				temptree.setEndPos(endPos);
				temptree.setURL(line.substring(0, 300).trim());
				temptree.lastModified(line.substring(300, 320).trim());
				count = 320;
				
				while(count <= line.length()){
					try{
						String nextValues = line.substring(count, count+30);
						
						String first = nextValues.substring(0, 20);
						String second = nextValues.substring(20, 30);
						
						if(!first.trim().isEmpty() && !second.trim().isEmpty()){
							first = first.trim();
							second = second.trim();
							if(temptree.get(first, false) > 0){}
							else{
								temptree.put(first);
								try{
									for(int j = 0; j < Integer.parseInt(second)-1; j++){
										temptree.get(first, true);
									}
								}catch(NumberFormatException e){ temptree.get(first, true); }
							}
						}
						count = count + 30;
					}catch(StringIndexOutOfBoundsException e){
						count = line.length()+1;
					}
				}
				
				//Now remove from positions file
				LinkedList<String> file = new LinkedList<String>();
		    	File file2 = new File("positions.txt");
		    	Scanner fs = new Scanner(f);
		    	while(fs.hasNext()){
		    		String next = fs.next();
		    		if(next.trim().equalsIgnoreCase(startSPos)){
		    			fs.next();
		    		}else{
		    			file.add(next.trim());
		    		}
		    	}
		    	FileOutputStream newFile = new FileOutputStream("positions.txt", false);
		    	FileWriter fw = new FileWriter("positions.txt",true);
		    	for(int l = 0; l < file.size(); l++){
		    		fw.write(file.get(l));
		    		fw.write(" ");
		    	}
		    	fs.close();
		    	fw.close();
				
		    	temptree.writeTree();
				trees.add(temptree);
			}
		}
	}
	
	public static Boolean checkPosFile(){
		//Check if positions file exist
		try{
			File f = new File("positions.txt");
			Scanner sc = new Scanner(f);
			sc.close();
			return true;
		}catch(IOException e){
			return false;
		}
	}
	
	public static void readPages() throws IOException{
		File uf = new File("urls.txt");
		Scanner urls = new Scanner(uf);
		Scanner urls2 = new Scanner(uf);
		
		while(urls2.hasNextLine()){
			baseLinks.add(urls2.nextLine());
		}
		String url = "None";
		while(urls.hasNextLine()){
			try{
				String urlRead = urls.nextLine();
				url = urlRead;
				URL temp = new URL(urlRead);
				Scanner in = new Scanner(temp.openStream());
				
				cache.put(urlRead);
				
				BalTree temptree = new BalTree();
				temptree.setURL(urlRead);
				temptree.lastModified(cache.getURLModified(urlRead));
				trees.add(temptree);
					
				while (in.hasNextLine()){
					String urlLine = in.nextLine();
					String parsed = Jsoup.parse(urlLine).text();
					
					Document doc = Jsoup.parse(urlLine);
					Element link = doc.select("a").first();
					try{
						String linkHref = link.attr("href");
						String linkHrefLower = linkHref.toLowerCase();
						if(!linkHrefLower.contains("http") && !linkHrefLower.contains("wikipedia")){
							if(!linkHrefLower.contains("www")){
								linkHref = "http://en.wikipedia.org" + linkHref;
								linkHrefLower = "http://en.wikipedia.org" + linkHrefLower;
							}
						}
						if(linkHrefLower.contains("http") || linkHrefLower.contains("www")){
							if(!linkHrefLower.equalsIgnoreCase("http://en.wikipedia.org#")){
								if(!linkHrefLower.contains("#cite")){
									if(!linkHrefLower.contains("citethispage")){
										if(!externalLinks.contains(linkHref) && !baseLinks.contains(linkHref)){
											externalLinks.add(linkHref);
										}
									}
								}	
							}
						}
					}catch(NullPointerException e){}
					
					wordFrequency(urlRead,parsed);
				}
			}catch(IOException e){}
			
			for(int i = 0; i < trees.size(); i++){
				if(trees.get(i).getURL().equals(url)){
					captureTree(trees.get(i));
				}
			}
		}
		urls.close();
		urls2.close();
		cache.writeCacheToRAF();
	}
	
	public static void readExternalLinks(){
		try{
			for(String URL:externalLinks){
				URL temp = new URL(URL);
				Scanner in = new Scanner(temp.openStream());
				
				cache.put(URL);
				
				BalTree temptree = new BalTree();
				temptree.setURL(URL);
				temptree.lastModified(cache.getURLModified(URL));
				trees.add(temptree);
				
				while (in.hasNextLine()){
					String urlLine = in.nextLine();
					String parsed = Jsoup.parse(urlLine).text();
					wordFrequency(URL,parsed);
				}
				
				for(int i = 0; i < trees.size(); i++){
					if(trees.get(i).getURL().equals(URL)){
						captureTree(trees.get(i));
					}
				}
			}
			//cache.writeCacheToRAF();
		}catch(IOException e){}
	}
	
	public static void captureTree(BalTree bt) throws IOException{
		for(int i = 0; i < trees.size(); i++){
			if(bt.getURL().equals(trees.get(i).getURL())){
				trees.get(i).writeTree();
			}
		}
	}
	
	public static void readCommonWords() throws FileNotFoundException{
		File f = new File("common.txt");
		Scanner fs = new Scanner(f);
		while(fs.hasNextLine()){
			common.add(fs.nextLine());
		}
		fs.close();
	}

	public static void wordFrequency(String URL, String html) throws IOException{
		Scanner sc = new Scanner(html);
		
		while(sc.hasNext()){
			String word = sc.next();
			word = trim(word);
			if(word.length() <= 20){
				if(!common.contains(word) || !common.contains(word.toLowerCase())){
					for(int i = 0; i < trees.size(); i++){
						if(trees.get(i).getURL().equalsIgnoreCase(URL)){
							if(trees.get(i).get(word, false) == -1){
								trees.get(i).put(word);
							}else{
								trees.get(i).get(word, true);
							}
						}
					}
				}
			}
		}
	}
	
	public static String trim(String in){
		String word = in;
		if(word.contains("."))
			word = word.replace(".", "");
		if(word.contains(","))
			word = word.replace(",", "");
		if(word.contains("("))
			word = word.replace("(", "");
		if(word.contains(")"))
			word = word.replace(")", "");
		if(word.contains(":"))
			word = word.replace(":", "");
		if(word.contains(";"))
			word = word.replace(";", "");
		if(word.contains("\""))
			word = word.replace("\"", "");
		if(word.contains("'"))
			word = word.replace("'", "");
		if(word.contains("["))
			word = word.replace("[", "");
		if(word.contains("]"))
			word = word.replace("]", "");
				
		return word;
	}
	
	public static void openFW() throws IOException{
		fw = new FileWriter("positions.txt",true);
	}
	
	public static void closeFW() throws IOException{
		try{
			fw.close();
		}catch(NullPointerException e){}
	}
	
	public static RandomAccessFile getRAF(){
		return raf;
	}
	
	public static FileWriter getFW(){
		return fw;
	}
	
}
