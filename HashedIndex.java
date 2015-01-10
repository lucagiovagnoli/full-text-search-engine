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
import java.util.LinkedList;
import java.util.NoSuchElementException;


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
    
    public int size(){
    	return index.keySet().size();    	
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
    public PostingsList getPostings(String token)  {
    	
    	PostingsList temp = index.get(token);
    	if(temp!=null) return temp; // if the postingsList is in memory return 
    	
    	/** otherwise if there's an index to load I try to load the PostingsList of the token */
    	if(loadedFromFile==true) {  
    		storager.loadPostingsListFromDisk(index,token);    	
    	}
    	
    	/* I return everything I have got. Could be null*/
    	return index.get(token);
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
    	
    	PostingsList res = null;
    	Iterator<String> it;
    	
    	long tavg = 0;
    	for (int i=0;i<11;i++){    		

    	long t0 = System.nanoTime();

		try{
			switch (queryType){
	    		case Index.INTERSECTION_QUERY:
	    			
	       			/* I need to order the lists over the # of elements in order
	    			 * to optimize the intersection algorithm */	
	    			
	    		    /* extract all posting lists*/
	    	    	it = query.terms.iterator();
	    	    	LinkedList<PostingsList> sortedPostings = new LinkedList<PostingsList>();
	    			while(it.hasNext()){
	    				String term = it.next();
	    				PostingsList temp = getPostings(term);
	    				if (temp==null){
	    					//System.out.println("Parola "+term + " non presente nell'indice.");
	    					throw new NullPointerException();
	    				}
	    				sortedPostings.add(temp);
	    	   		}    	
	    				
	    			/* sort posting Lists */
	    			Collections.sort(sortedPostings);
	    			
	    			res = sortedPostings.remove();
	    			while(sortedPostings.isEmpty() == false){
	       				res = res.intersection(sortedPostings.remove());
	       			}    				
	    			break;

	    		case Index.PHRASE_QUERY:
	    			int relativeOff = 1;
	    			
	    	    	it = query.terms.iterator();
	    	    	res = getPostings(it.next());
	    			while(it.hasNext()){
	    				String term = it.next();
	    				PostingsList temp = getPostings(term);

	    				if (temp==null) {
	    					//System.out.println("Parola "+term + " non presente nell'indice.");
	    					throw new NullPointerException();
	    				}
	    				res = res.positional((temp),relativeOff);
		    			relativeOff++;
	    			}
	    			break;
	    		default:
	    			break;
	    	}
		}
		catch (NoSuchElementException e){
			//System.out.println("Empty query. "+e);
			res=null;
		}
		catch (NullPointerException e1){
			//System.out.println("Term not in index. "+e1);
			res=null;
		}
	    	
    	//System.out.println(res);
    	
		long t1 = System.nanoTime();
		long tRes = t1-t0;
		//System.out.println("Time for evaluating the query (ns): "+tRes);
		
		if(i!=0) tavg+=tRes;
    	}
    	tavg/=10;
    	System.out.println(query.terms+" "+tavg);
		return res;
    }
    
    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}