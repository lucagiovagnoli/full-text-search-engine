/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.Iterator;
import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();


    /**  Number of postings in this list  */
    public int size() {
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
    	PostingsEntry elem;
    	if(list.isEmpty() == false){
    		/** check if this postings list already contains the docID **/
        	/** NOTE that I check the last element only **/
    		if(list.getLast().docID==docID) {
    			//System.out.println(list.getLast().docID+" "+list.get(list.size()-1).docID);
    			/** update the offset **/
    			list.getLast().addOffset(offset); 
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
	
    
    public String toString()  {
    	String stampa = "";
    	for(PostingsEntry elemento: list){
    		stampa += elemento.toString()+"\n";
    	}
    	return stampa;
    }

}
	

			   
