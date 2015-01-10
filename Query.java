/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
	public LinkedList<Double> weights = new LinkedList<Double>();
	public double queryLength = 0;

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
		StringTokenizer tok = new StringTokenizer( queryString );
		while ( tok.hasMoreTokens() ) {
		    terms.add( tok.nextToken() );
		    weights.add( new Double(1) );
		    queryLength+=1;
		}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {return terms.size();}
    
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
		Query queryCopy = new Query();
		queryCopy.terms = (LinkedList<String>) terms.clone();
		queryCopy.weights = (LinkedList<Double>) weights.clone();
		queryCopy.queryLength = queryLength;
		return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
	
    	int nRelevant = 0;
    	double alpha = 1;
    	double beta = 0.75;
    	
       	for(int i=0;i<docIsRelevant.length;i++){
    		if(docIsRelevant[i]==true)	{
    			nRelevant++;
    		}
    	}

       	for(int i=0;i<docIsRelevant.length;i++){
    		if(docIsRelevant[i]==true)	{
    		    LinkedList<String> docTerms = getDocTerms(Index.docIDs.get(results.get(i).docID+""));
    			
    		    Iterator<String> it = docTerms.iterator();
    		    while(it.hasNext()){
    		    	insertOrUpdateWeight(it.next(),beta,nRelevant);    		    	
    		    }
    		}
       	}
    }

    private LinkedList<String> getDocTerms(String filepath){
    	
    	File f = new File(filepath);
    	FileReader reader;
		LinkedList<String> result = new LinkedList<String>(); 

		try {
			reader = new FileReader(f);
			SimpleTokenizer tok = new SimpleTokenizer(reader); 	
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				result.add(token);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return result;
    }
    
    private void insertOrUpdateWeight (String term, double beta, int nRelevant){
    	
    	double wUpdate = (beta/ (double) nRelevant);
    	
    	for (int i=0;i<terms.size();i++){
    		if(terms.get(i).compareTo(term)==0) {
    			Double oldWeight = weights.remove(i);
    			weights.add(i,oldWeight + wUpdate);
    			return;
    		}
    	}
    	terms.add(term);
    	weights.add(new Double(wUpdate));
    }    
}



    
