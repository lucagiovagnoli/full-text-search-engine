package ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;


public class Evaluation {

	private String[] evaluationQueries = new String[] {"antikens underverk" ,
														"olympiska spel och fred",
														"europacupen",
														"konflikten i palestina",
														"snowboard",
														"den europeiska bilindustrin",
														"enhetlig europeisk valuta",
														"sex i reklam",
														"lutande tornet i pisa",
														"genteknik"};

	String[] dirNames = new String[]{"svwiki/files/1000","svwiki/files/2000","svwiki/files/3000","svwiki/files/4000","svwiki/files/5000","svwiki/files/6000","svwiki/files/7000","svwiki/files/8000","svwiki/files/9000","svwiki/files/10000"};
	
	public Evaluation() {
		// TODO Auto-generated constructor stub
	}

	
	public void es23() throws IOException{
						

		HashedIndex index = new HashedIndex();
		index.load();
		SearchPerformer searcher = new SearchPerformer(index);
		
		for(int i=0;i<evaluationQueries.length;i++){
			String str = evaluationQueries[i];
			PostingsList pl = searcher.search(new Query(str), Index.RANKED_QUERY, Index.TF_IDF, 0);
			for(int c=0;c<20 && c<pl.size();c++){
				String mydocPath =  index.docIDsToFilepath().get(pl.get(c).docID+"");
				String name = (mydocPath.split("/")[3]).split(".txt")[0];
				System.out.println((i+1)+" "+name);
			}
		}
	}
	
	
	/** compute time taken for each query when the index is already in memory 
	 * (used to compute changing on times with the "Index Elimination" speedup)
	 * */
	public void es33() {
		
		Indexer indexer = new Indexer();

		for (int i=0; i<dirNames.length; i++ ) {
			File dokDir = new File(dirNames[i]);
			indexer.processFiles( dokDir );
		}

		SearchPerformer searcher = new SearchPerformer(indexer.index);

		System.out.println("Average time (us):");			
		for(int i=0;i<evaluationQueries.length;i++){
			String str = evaluationQueries[i];

			double avg = 0;
			double tries = 100;
			for(int z=0;z<tries;z++){ // average time over "tries" number of tentatives
				long t0 = System.nanoTime();
				PostingsList pl = searcher.search(new Query(str), Index.RANKED_QUERY, Index.TF_IDF, 0);
				long t1 = System.nanoTime();
				avg += t1-t0;
			}
//			System.out.println(str+"\t\t- Average time (us) over "+tries+" tries: "+((avg/tries)/1000));			
			System.out.println(avg/tries);			
		}		
	}
	
	public static void main(String[] args) throws IOException{

		Evaluation ev = new Evaluation ();
		ev.es33();
		
	}
	
	
	
}
