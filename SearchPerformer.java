/**
 * 
 */
package ir;

import ir.PageRank.algorithm;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author luca
 *
 */
public class SearchPerformer {

	private PostingsList res = null;
	private Iterator<String> it;
	private Index hashedIndex;
	
	/**
	 * 
	 */
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
	
	public PostingsList rankedQuery(Query query, int rankingType){
		
		int N = Index.docIDs.size();
		double[] scores = new double[N];
		int[] length = new int[N];
		PostingsList plist = null;
		double wtq;
		
		/* puts scores into the array for each document */
		it = query.terms.iterator();
		while (it.hasNext()){
			plist = hashedIndex.getPostings(it.next());
			switch(rankingType){
				case Index.TF_IDF:
					wtq = 1 * Math.log((double)N/((double)plist.get_df())) / query.terms.size();
					plist.tfIdf(scores, wtq,1);	
					break;
				case Index.PAGERANK:
					plist.justPagerank(scores,hashedIndex.getLeftEigenvector(),1);	
					break;
				case Index.COMBINATION:
					wtq = 1 * Math.log((double)N/((double)plist.get_df())) / query.terms.size();
					plist.justPagerank(scores,hashedIndex.getLeftEigenvector(),1000000);	
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
				elem.score = scores[i]/ ((double) Index.docLengths.get(i+""));
				res.add(elem);
			}
		}
		res.sortByScores();
		
		return res;
	}
	
	
}
