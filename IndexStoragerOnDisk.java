 
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

public class IndexStoragerOnDisk{
	
	private Index index;
	private HashMap<String,String> termFileMap = new HashMap<String,String>();
	private int nomefile=0;
	private String dirName = "indexOnDisk";
	
	public IndexStoragerOnDisk(Index index){
		this.index = index;
	}
	
	private String generateFileName(){
		nomefile++;
		return "f"+nomefile;
	}
		
	public void loadManagementMapsFromDisk(){
		HashMap<String,String> docIDs = (HashMap<String,String>) loadObjectFromDisk("IDtoPaths");
		HashMap<String,Integer> docLengths = (HashMap<String,Integer>) loadObjectFromDisk("IDtoLengths");
		
		Iterator<String> it1 = docIDs.keySet().iterator();
		while (it1.hasNext()){
			String docID = it1.next();
			String path = docIDs.get(docID);
			index.docIDs.put( "" + docID, path );
		}

		Iterator<String> it2 = docLengths.keySet().iterator();
		while (it1.hasNext()){
			String docID = it1.next();
			Integer offset = docLengths.get(docID);
		    index.docLengths.put( "" + docID, offset);
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
	    	
		/* create folder where to put the index */
		File dir = new File("indexOnDisk");
		dir.mkdir();
		
		Iterator<String> iterOnIndex = index.getDictionary();
		
		while(iterOnIndex.hasNext()){
	    	String term = iterOnIndex.next();
	    	filename = generateFileName();
	    	
	    	/* remember in which file I saved the PostingList for that term */
	    	termFileMap.put(term,filename);
	    	
	    	/* try to save the PostingsList into the file */
        	try{
        		FileOutputStream outputFile = new FileOutputStream(dirName+"/"+filename);
        		ObjectOutputStream outStream = new ObjectOutputStream(outputFile);
	        	outStream.writeObject(index.getPostings(term));
	    		closeStream(outputFile);
	    		closeStream(outStream);
	    	}
	    	catch (IOException e){
	    		System.out.println("Errore IO: problemi nella serializzazione. Term: "+term+".\n"+e.getMessage());
	    	}
		}
		
		/** Save mapping ID to paths and ID to lenghts **/
		saveObjectToFile(index.docIDs,"IDtoPaths");
		saveObjectToFile(index.docLengths,"IDtoLengths");
		
		/** save TermFileMap on Disk **/
		saveObjectToFile(termFileMap,"termFileMap");

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
    
    public PostingsList loadPostingsListFromDisk(String term){
    	
    	PostingsList loadedList = null;
  
    	try{
    		String filename = termFileMap.get(term);
    		if(filename != null) {
		    	FileInputStream inputFile = new FileInputStream(dirName+"/"+filename);
		    	ObjectInputStream inObj = new ObjectInputStream(inputFile);
		    	loadedList = (PostingsList) inObj.readObject();
	    		closeStream(inputFile);
	    		closeStream(inObj);
    		}
    	}
    	catch (IOException e){
    		System.out.println("Errore IO: problemi nella de-serializzazione. \n"+e.getMessage());
    	}
    	catch (ClassNotFoundException e1){
    		System.out.println(e1.getMessage());
    	}
    
    	return loadedList;
    }
}



