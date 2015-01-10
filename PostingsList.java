/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.Serializable;
import java.util.Comparator;
import java.lang.Math;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable,Comparable<PostingsList> {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();
    
    /**  Number of postings in this list  */
    public int size() {
    	return list.size();
    }
    
    /** return document frequency of this term (size of the posting list ;) **/
    public int get_df(){
    	return list.size();
    }
    
    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
	return list.get( i );
    }
    
    public void add(PostingsEntry elem){
    	list.add(elem);
    }
    
    public void add(int docID, int offset){
    	PostingsEntry elem,lastEntry;
    	if(list.isEmpty() == false){
    		
    		/** check if this postings list already contains the docID **/
        	/** NOTE that I check the last element only (for the assignment is sufficient and optimized)**/
    		
    		lastEntry = list.getLast();
    		if(lastEntry.docID==docID) {
    			/** update the offset **/
    			lastEntry.addOffset(offset);  //add to the offset list
    			lastEntry.setFrequency(lastEntry.getFrequency()+1); //freq++
    			return;
    		}
/*    		for(int i=list.size()-1; i>=0; i--){
        		if(list.get(i).docID==docID) {
        			System.out.println(list.getLast().docID+" "+list.get(list.size()-1).docID);
        			list.getLast().addOffset(offset); 
        			return;
        		}
    		}*/
    	}
	    elem = new PostingsEntry(docID,offset);
		list.add(elem);
    }
    
    /* Sorting useless because implicit in the inserting of assignment 1*/
//    public void sortList(){
//		Collections.sort(list); 
//    }
    
    /** INTERSECTION ALGORITHM **/
	public PostingsList intersection (PostingsList list2){
    	
		PostingsList resultingList = new PostingsList();
		int i=0,j=0;
				
		while(i<list.size() && j<list2.size()){
			if(list.get(i).docID == list2.get(j).docID){
				resultingList.add(list.get(i));
				i++;
				j++;
			}
			else if (list.get(i).docID<list2.get(j).docID)	i++;
			else j++;
		}
		return resultingList;
	}
    
	/** POSITIONAL INDEXING**/
	public PostingsList positional (PostingsList list2, int relativeOffset){
    	
		PostingsList resultingList = new PostingsList();
		PostingsEntry newElem;
		int i=0,j=0,N1=list.size(),N2=list2.size(); 
				
		while(i<N1 && j<N2){
			if(list.get(i).docID == list2.get(j).docID){
				if((newElem = list.get(i).precedes(list2.get(j),relativeOffset)) != null){
					resultingList.add(newElem);					
				}
				i++;
				j++;
			}
			else if (list.get(i).docID<list2.get(j).docID)	i++;
			else j++;
		}
		return resultingList;
	}
	
	/** RANKED RETRIEVAL **/
	public void rankEntries(int Ndocs, HashMap<String,Integer> docLengths){
		
		Iterator<PostingsEntry> it = list.iterator();
		PostingsEntry elem = null;
		
		while(it.hasNext()){
			elem = it.next();
			elem.computeScore(Ndocs, get_df(), docLengths);
		}
		
		Collections.sort(list);
		
	}
	
	
    
    public String toString()  {
    	String stampa = "Posting list with number of elements:"+list.size()+"\nElements: \n";
    	for(PostingsEntry elemento: list){
    		stampa += elemento.toString()+"\n";
    	}
    	return stampa;
    }

	@Override
	public int compareTo(PostingsList other) {
		// TODO Auto-generated method stub
		return this.list.size()-other.list.size();
	}
}
	

			   
