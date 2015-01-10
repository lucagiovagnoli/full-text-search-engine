/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
	public double score;
	private int frequency;
    private LinkedList<Integer> positions;

	public PostingsEntry(int docID){
		this.docID = docID;
		positions = new LinkedList<Integer>();
	}
    
	public PostingsEntry(int docID,int offset){
		this.docID = docID;
		this.frequency = 1;
		positions = new LinkedList<Integer>();
		positions.add(offset);
    }
	
	public void addOffset(int offset) {
		frequency++;
		positions.add(offset);
	}
	
	public double computeTfIdf(int df,int N){
		//System.out.println("docname: "+Index.docIDs.get(""+docID)+" frequency: "+frequency+" size: "+positions.size()+" N: "+N+"df: "+df+"lenD : "+Index.docLengths.get(""+docID));
		return this.frequency * Math.log(N/((double)df));
	}
	
	/** Algorithm for **/
	public PostingsEntry precedes(PostingsEntry other, int relativeOffset){
		PostingsEntry newElem = new PostingsEntry(this.docID);
		int flag=0,offset1,offset2;
		for(int i=0;i<positions.size();i++){
			offset1 = positions.get(i)+relativeOffset;
			int j=other.positions.size()-1;
			offset2 = other.positions.get(j);
			/*scan from the end to be able to stop sooner*/
			while(offset1<=offset2 && j>=0){  
				offset2 = other.positions.get(j);
				if(offset1==offset2) {
					newElem.addOffset(positions.get(i));
					flag=1;
				}
				j--;
			}
		}
		if (flag==1) return newElem;
		return null;
	}
     
    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo(PostingsEntry other) {
	return Double.compare(other.score, score);
    }
    
    public String toString(){
    	String stampa = "<DocID:"+docID+";Frequency:"+frequency+">(";
    	for(Integer i:positions){
        	stampa += i.toString()+",";
    	}
    	return stampa+")";
    }
    public int getFrequency() {
		return frequency;
	}
    
    public void setFrequency(int freq){
    	this.frequency = freq;
    }

}

    
