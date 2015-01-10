/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import ir.PageRank.algorithm;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    public HashMap<String, String> docIDs = new HashMap<String,String>();
    public HashMap<String, Integer> docLengths = new HashMap<String,Integer>();
	
    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
    private boolean loadedFromFile = false;
    private IndexStoragerOnDisk storager = new IndexStoragerOnDisk(this);
    private HashMap<String,Double> leftEigenvector;

    public  HashedIndex(){
		
		/* check if the pagerank already exists on disk */
		File f = new File("indexOnDisk1/pagerank");
		if(f.exists() && !f.isDirectory()) { 
			System.out.println("Pagerank già sul disco.");
			leftEigenvector = (HashMap<String,Double>) IndexStoragerOnDisk.loadObjectFromDisk("pagerank");
			PageRank.docName = (String[]) IndexStoragerOnDisk.loadObjectFromDisk("docNamePR");
		}

		else{
			int T = 100;
	    	int m = 100;
	    	double c = 0.85;

		    PageRank pr = new PageRank("./svwiki_links/links.txt", c);
	    	leftEigenvector = pr.computePagerank(algorithm.monteCarlo3, T, m);
	    	
	    	IndexStoragerOnDisk.saveObjectToFile(leftEigenvector, "pagerank");
	    	IndexStoragerOnDisk.saveObjectToFile(PageRank.docName, "docNamePR");
	    }    	
    }

    public HashMap<String,Double> getLeftEigenvector(){
    	return leftEigenvector;
    }

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


    public HashMap<String,String> docIDsToFilepath(){return docIDs;}
    public HashMap<String,Integer> docIDsToLengths(){return docLengths;}

    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }
}
