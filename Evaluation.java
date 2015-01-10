package ir;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


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
			 	
	
	public Evaluation() {
		// TODO Auto-generated constructor stub
	}

	
	public void es23() throws IOException{
						
		HashedIndex index = new HashedIndex();
		index.load();
		
		for(int i=0;i<evaluationQueries.length;i++){
			String str = evaluationQueries[i];
			PostingsList pl = index.search(new Query(str), Index.RANKED_QUERY, Index.TF_IDF, 0);
			for(int c=0;c<20 && c<pl.size();c++){
				String mydocPath =  Index.docIDs.get(pl.get(c).docID+"");
				String name = (mydocPath.split("/")[3]).split(".txt")[0];
				System.out.println((i+1)+" "+name);
			}
		}
	}
	
	public static void main(String[] args) throws IOException{

		Evaluation ev = new Evaluation ();
		ev.es23();
		
	}
	
	
	
}
