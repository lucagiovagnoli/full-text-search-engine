package ir;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;



public class BiwordIndex implements Index  {

	
    public HashMap<String, String> docIDs = new HashMap<String,String>();
    public HashMap<String, Integer> docLengths = new HashMap<String,Integer>();


	/* hash the concatenation of the 2 words*/
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    
	public void insert(String token, int docID, int offset) {
		/* if the term is there already I add it to the postings list */
		if(index.containsKey(token)){
		    PostingsList lista = index.get(token);
		    lista.add(docID,offset);
		}
		/* otherwise I first create the Posting's List and then add it */
		else{
		    PostingsList lista = new PostingsList();
		    lista.add(docID,offset);
		    index.put(token,lista);
		}
	}


	public PostingsList getPostings(String token) {
    	return index.get(token); //could be null
	}
	
	public int size() {
    	return index.keySet().size();    	
	}
	public Iterator<String> getDictionary() {
    	return index.keySet().iterator();
	}

	// no saving and loading for this index
	public void save() {}
	public void load() {}

	// pagerank implemented only with the hashedIndex
	public HashMap<String, Double> getLeftEigenvector() {
		return null;
	}
	
    public HashMap<String,String> docIDsToFilepath(){return docIDs;}
    public HashMap<String,Integer> docIDsToLengths(){return docLengths;}
    
	public void cleanup() {
	}
	
	
}
