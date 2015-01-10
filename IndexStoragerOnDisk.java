 
package ir;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Closeable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class IndexStoragerOnDisk{
	
	public static int wordsPerFile=1000;
	private Index index;
	private HashMap<String,String> termFileMap = new HashMap<String,String>();
	private int nomefile=0;
	private String dirName = "indexOnDisk1";
	
	public IndexStoragerOnDisk(Index index){
		this.index = index;
	}
	
	private String generateFileName(){
		nomefile++;
		return "f"+nomefile;
	}
	
	class RetrievalInfo{

		String filename;
		int offset;

		public RetrievalInfo(String filename,int offset){
			this.filename = filename;
			this.offset = offset;
		}		
	}
		
	public void loadManagementMapsFromDisk(){
		HashMap<String,String> docIDs = (HashMap<String,String>) loadObjectFromDisk("IDtoPaths");
		HashMap<String,Integer> docLengths = (HashMap<String,Integer>) loadObjectFromDisk("IDtoLengths");
				
		Iterator<String> it = docIDs.keySet().iterator();
		while (it.hasNext()){
			String docID = it.next();
			String path = docIDs.get(docID);
			Index.docIDs.put(docID, path);
		}

		it = docLengths.keySet().iterator();
		while (it.hasNext()){
			String docID = it.next();
			Integer offset = docLengths.get(docID);
		    Index.docLengths.put( "" + docID, offset);
		}
		termFileMap = (HashMap<String,String>) loadObjectFromDisk("termFileMap");
	}
	
	
	public Object loadObjectFromDisk(String filename){
		Object res=null;
		try{
			FileInputStream inputFile = new FileInputStream(dirName+"/"+filename);
			ObjectInputStream inStream = new ObjectInputStream(inputFile);
        	res = inStream.readObject();
    		closeStream(inputFile);
    		closeStream(inStream);
    	}
    	catch (IOException e){
    		System.out.println("Errore IO: problemi nella de-serializzazione.\n"+e.getMessage());
    	}
    	catch (ClassNotFoundException e1){
    		System.out.println(e1.getMessage());
    	}
		return res;
	}
	
	public void saveIndexOnDisk(){
    	
		String filename="";
		FileOutputStream outputFile =null;
		ObjectOutputStream outStream = null;	
		FileOutputStream outputFile2 = null;
		BufferedWriter writer = null;
		
		/* create folder where to put the index */
		File dir = new File(dirName);
		dir.mkdir();
		dir = new File(dirName+"/allWordsOfFile");
		dir.mkdir();
		
		Iterator<String> iterOnIndex = index.getDictionary();
		
		long t0 = System.nanoTime();
		
		try{
			while(iterOnIndex.hasNext()){
				
		    	filename = generateFileName();
		     	
	        	outputFile = new FileOutputStream(dirName+"/"+filename);
	        	outStream = new ObjectOutputStream(outputFile);
	        	outputFile2 = new FileOutputStream(dirName+"/allWordsOfFile/"+filename);
	        	writer = new BufferedWriter(new OutputStreamWriter(outputFile2));
		     	
				for(int i=0;i<wordsPerFile && iterOnIndex.hasNext();i++){
					String term = iterOnIndex.next();
	
					/* remember in which file I saved the PostingList for that term */
					termFileMap.put(term,filename);
			    	
			    	/* save the PostingsList into the file */
		       	    outStream.writeObject(index.getPostings(term));
			    	writer.write(term);
			    	writer.newLine();		
				}
				writer.flush();
	    		closeStream(outputFile);
	    		closeStream(outStream);
	    		closeStream(outputFile2);
	    		closeStream(writer);
			}
		}
			
    	catch (IOException e){
    		System.out.println("Errore IO: problemi nella serializzazione.\n"+e.getMessage());
    	}
		
		/** Save mapping ID to paths and ID to lenghts **/
		saveObjectToFile(Index.docIDs,"IDtoPaths");
		saveObjectToFile(Index.docLengths,"IDtoLengths");
		
		/** save TermFileMap on Disk **/
		saveObjectToFile(termFileMap,"termFileMap");
		
		long t1 = System.nanoTime();
		long tRes = t1-t0;
		System.out.println("Time for saving on storage (ns): "+tRes);
		System.out.println("Time for saving on storage (ms): "+tRes/1000000);
	}
	
	private void saveObjectToFile(Object obj,String filename){
    	try{
    		FileOutputStream outputFile = new FileOutputStream(dirName+"/"+filename);
    		ObjectOutputStream outStream = new ObjectOutputStream(outputFile);
        	outStream.writeObject(obj);
    		closeStream(outputFile);
    		closeStream(outStream);
    	}
    	catch (IOException e){
    		System.out.println("Errore IO: problemi nella serializzazione.\n"+e.getMessage());
    	}
	}
	    
    public void closeStream(Closeable stream){
		try{
    		stream.close();
		}
		catch (IOException e){
    		System.out.println("IO Error: problems to close stream.\n"+e.getMessage());
		}    	
    }
    
    public void loadPostingsListFromDisk(HashMap<String,PostingsList> uIndex, String term){
    	
    	PostingsList loadedList = null;
  
    	try{
    		String filename = termFileMap.get(term);
    		if(filename != null) {
		    	FileInputStream inputFile = new FileInputStream(dirName+"/"+filename);
		    	ObjectInputStream inObj = new ObjectInputStream(inputFile);
		    	FileInputStream inputFile2 = new FileInputStream(dirName+"/allWordsOfFile/"+filename);
		    	BufferedReader br = new BufferedReader(new InputStreamReader(inputFile2, Charset.forName("UTF-8")));
		    	String line="";
		    	
		    	while((line = br.readLine()) != null){
		    		loadedList = (PostingsList) inObj.readObject();
		    		uIndex.put(line,loadedList);
		    	}
		    	
		    	closeStream(inputFile);
	    		closeStream(inObj);
		    	closeStream(inputFile2);
	    		closeStream(br);
    		}
    		else{
    			System.out.println("Term not in the index - storage file not found.");
    		}
    	}
    	catch (IOException e){
    		System.out.println("Errore IO: problemi nella de-serializzazione. \n"+e.getMessage());
    	}
    	catch (ClassNotFoundException e1){
    		System.out.println(e1.getMessage());
    	}
    }
}



