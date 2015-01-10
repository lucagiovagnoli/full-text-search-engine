/**
 * 
 */
package ir;

import ir.PageRank.algorithm;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author luca
 *
 */
public class SearchPerformer {

	/** index elimination - cut off high values of idf  
	 *  ex: "den" has a high doc freq so it is not significative enough   
	 **/
	private double IDFcutoff = 1;  //if the term appears in more than IDFcutoffPercentage*documents, it is not considered
	
	
	private PostingsList res = null;
	private Index hashedIndex;
	private Index biwordIndex;


	private Iterator<String> it;
	private Iterator<Double> itWeights;
	
	/**
	 * 
	 */
	public SearchPerformer(Index hashedIndex, Index biwordIndex) {
		this.hashedIndex = hashedIndex;
		this.biwordIndex = biwordIndex;
	}
	
	public SearchPerformer(Index hashedIndex) {
		this.hashedIndex = hashedIndex;
	}

	
	public PostingsList intersectionQuery(Query query){
		
		/* I need to order the lists over the # of elements in order
		 * to optimize the intersection algorithm */	
		
	    /* extract all posting lists*/
    	it = query.terms.iterator();
    	LinkedList<PostingsList> sortedPostings = new LinkedList<PostingsList>();
		while(it.hasNext()){
			String term = it.next();
			PostingsList temp = hashedIndex.getPostings(term);
			if (temp==null){
				System.out.println("Parola "+term + " non presente nell'indice.");
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
		return res;
	}

	public PostingsList phraseQuery(Query query){
		
		int relativeOff = 1;
		
    	it = query.terms.iterator();
    	res = hashedIndex.getPostings(it.next());
		while(it.hasNext()){
			String term = it.next();
			PostingsList temp = hashedIndex.getPostings(term);

			if (temp==null) {
				System.out.println("Parola "+term + " non presente nell'indice.");
				throw new NullPointerException();
			}
			res = res.positional(temp,relativeOff);
			relativeOff++;
		}
		
		return res;
	}
	
	public PostingsList rankedQuery(Query query, Index index, int rankingType){
		
		int N = index.docIDsToFilepath().size();
		double[] scores = new double[N];
		double wtq;
		
		/* puts scores into the array for each document */
		it = query.terms.iterator();
		itWeights = query.weights.iterator();
		while (it.hasNext()){
			String queryTerm = it.next();
			PostingsList plist = index.getPostings(queryTerm);
			if(plist==null) {System.out.println("Parola non presente nell'indice"+queryTerm); continue;}
			
			double idf = Math.log((double)N/((double)plist.get_df()));
			if(plist.get_df() > IDFcutoff * N ) continue; //index elimination - if doc freq too high, term not significative enough
			
			switch(rankingType){
				case Index.TF_IDF:
					wtq = itWeights.next() * idf / query.terms.size();
					plist.tfIdf(scores, wtq,1,N);	
					break;
				case Index.PAGERANK:
					plist.justPagerank(scores,index.getLeftEigenvector(),index.docIDsToFilepath() ,1);	
					break;
				case Index.COMBINATION:
					wtq = 1 * idf / query.terms.size();
					plist.tfIdf(scores, wtq,1,N);	
					plist.justPagerank(scores,index.getLeftEigenvector(),index.docIDsToFilepath(),1000000);	
					break;
				default:
					break;
			}
		}		
		
		/* Construct resulting posting list */
		res = new PostingsList();
		for (int i=0;i<N;i++){
			if(scores[i]!=0){
				PostingsEntry elem = new PostingsEntry(i);
				elem.score = scores[i]/ ((double) index.docIDsToLengths().get(i+""));
				res.add(elem);
			}
		}
		res.sortByScores();
		return res;
	}
	
	 /** Let the maximum indexed phrase length be n words (n = 2 in your case). Let the query length be m.
	  * 
	  * First, an min(n,m)-­‐gram ranked retrieval is performed. (As an example, a 3-­‐gram 
	  * retrieval in the svwiki/files/1000 data set with the query tillvarons yttersta
	  * grunder returns two matches, documents 23 and 47.)  
	  * 
	  * If less than k documents are returned, proceed to do an (n–1)-­‐gram retrieval. (As an 
	  * example, a 2-­‐gram (bi-­‐gram) retrieval in the svwiki/files/1000 data set with the 
	  * query tillvarons yttersta grunder returns three matches, documents 23, 47, and 199.)  
	  * 
	  * If less than k documents are returned from the (n–1)-­‐gram retrieval, and n > 1, proceed 
	  * to do an (n–2)-­‐gram retrieval. Repeat until k documents are found or until n = 1. (As an 
	  * example, a uni-­‐gram (single term) retrieval in the svwiki/files/1000 data set with
	  * the query tillvarons yttersta grunder returns 33 matches.) 
	  * */
	private PostingsList subphraseRetrieval(Query query){

		int k=5; //minimum number of documents that we want to be retrieved
		PostingsList bigramRes=null,unigramRes=null;
		
		if(query.terms.size()>=2){ //if at least 2 terms in the query I perform bigram retrieval
	    	Query biwordQuery = new Query(query);
	    	bigramRes =rankedQuery(biwordQuery ,biwordIndex,Index.TF_IDF); //bigram retrieval
	    	
	    	if(bigramRes.size()<k){
				unigramRes = rankedQuery(query, hashedIndex, Index.TF_IDF); //unigram retrieval
				double factor= computeFactorForSubphrase(bigramRes,unigramRes);
				bigramRes.multiplyAllScoresBy(factor); //weight the score more than the unigram
				unigramRes.merge(bigramRes);
				unigramRes.sortByScores();
				return unigramRes;
	    	}
	    	else return bigramRes;	    	
		}
		
		unigramRes = rankedQuery(query, hashedIndex, Index.TF_IDF); //unigram retrieval
		return unigramRes;

	}
	
	private double computeFactorForSubphrase(PostingsList bigram, PostingsList unigram){

		if(bigram.size()==0 || unigram.size()==0) return 1;
		
		PostingsEntry e = bigram.get(bigram.size()-1);
		PostingsEntry e1 = unigram.get(0);
		
		if(e.score > e1.score) return 1; //if the worst score of bigram is already better than the best of unigram return 1
		else return e1.score/e.score +1; //else return the factor needed to get the worst of the bigram better than the best of the unigram
		
	}
	
	
	  /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search(Query query, int queryType, int rankingType, int structureType ) {
    	
    	PostingsList res = null;
    	Iterator<String> it;

		try{
	    	/* if BIGRAM I implicitly perform RANKED RETRIEVAL using the biword index */
	    	if(structureType==Index.BIGRAM) {
	    		Query biwordQuery = new Query(query);
	    		res=rankedQuery(biwordQuery ,biwordIndex,Index.TF_IDF);
	    	}
	    	else if(structureType==Index.SUBPHRASE) {
	    		res = subphraseRetrieval(query);
	    	}
	    	else{ //UNIGRAM case
				switch (queryType){
		    		case Index.INTERSECTION_QUERY:
		    			res=intersectionQuery(query);
		    			break;
		    		case Index.PHRASE_QUERY:
		    			res=phraseQuery(query);
		    			break;
		    		case Index.RANKED_QUERY:
		    			res=rankedQuery(query, hashedIndex, rankingType);
		    			break;
		    		default:
		    			break;	    
		    	}
			}
		}
		catch (NoSuchElementException e){
			System.out.println("Empty query. "+e);
			res=null;
		}
		catch (NullPointerException e1){
			System.out.println("Term not in index. "+e1);
			res=null;
		}
    	
		return res;
    }
    
	
}
