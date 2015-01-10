/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    private boolean loadedFromFile = false;
    private IndexStoragerOnDisk storager = new IndexStoragerOnDisk(this);
    
    public void load(){ 
    	storager.loadManagementMapsFromDisk();
    	loadedFromFile = true;
    }
    public void save(){ 
    	storager.saveIndexOnDisk();
    }
    
    /**
     *  Inserts this token in the index.
     */
    public void insert(String token, int docID, int offset ) {
    	
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


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
    	return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings(String token) throws NullPointerException  {
    	if(loadedFromFile==true) return storager.loadPostingsListFromDisk(token);    	
    	return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
    	
    	PostingsList res = null;
    	
    	/** CARICA LE PARTI DELL'INDICE NECESSARIE BASANDOTI SULLE PAROLE DELLA QUERY**/
		
    	Iterator<String> it = query.terms.iterator();
    	try{
			res = getPostings(it.next());
	    	switch (queryType){
	    		case Index.INTERSECTION_QUERY:
	    			
	       			/* I need to order the lists over the # of elements in order
	    			 * to optimize the intersection algorithm */
	       			
	    			while(it.hasNext()){
	       				res = res.intersection(getPostings(it.next()));
	       			}    				
	    			break;
	    		
	    		case Index.PHRASE_QUERY:
	    			int relativeOff = 1;
	    			while(it.hasNext()){
	    				res = res.positional(getPostings(it.next()),relativeOff);
	    				relativeOff++;
	    			}
	    			break;
	    		default:
	    			break;
	    	}
    	}
    	catch (NullPointerException e){
    		System.out.println("Parola non presente nell'indice.");
    		res=null;
    	}
	    	
    	//System.out.println(res);
    	return res;
    }
    
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
