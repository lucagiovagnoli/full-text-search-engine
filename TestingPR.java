package ir;

import ir.PageRank.algorithm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TestingPR {
	
	private int[] targetDocNames = {1081,522,454,2634,365,36,526,3930,1324,483,7031,3094,2381,1306,9765,6287,1432,4762,2353,5608,5115,5621,425,6070,838,6722,8184,1584,3931,6907,3105,723,6074,2635,8071,8098,2343,2136,21,1524,837,6039,3743,2,4919,664,6451,8813,5559,2134};
	private double[] targetPR = {0.00393,0.00382,0.00357,0.00354,0.00286,0.00277,0.00269,0.00264,0.00246,0.00239,0.00235,0.00228,0.00221,0.00214,0.00192,0.00192,0.00188,0.00186,0.00186,0.00176,0.00170,0.00169,0.00160,0.00156,0.00155,0.00154,0.00152,0.00150,0.00149,0.00149,0.00148,0.00143,0.00142,0.00142,0.00141,0.00140,0.00138,0.00137,0.00137,0.00137,0.00136,0.00135,0.00134,0.00132,0.00132,0.00131,0.00131,0.00129,0.00127,0.00127};
	
	public static int K=50;
	List<PRelement> sortedDocsPR;
	
    /* structure to represent the solution and be able to sort it */
	private class PRelement implements Comparable<PRelement>{
		String docName;
		double pagerank;
		public PRelement(String docName,double pagerank){
			this.docName = docName;
			this.pagerank = pagerank;
		}

		public int compareTo(PRelement other) {
			if (pagerank > other.pagerank) return -1;
			if (pagerank == other.pagerank) return 0;
			else return 1;			}
	}
	
	public TestingPR(HashMap<String,Double> leftEigenvector) {
		builtSortedListPR(leftEigenvector);	
	}
	
	private void builtSortedListPR(HashMap<String,Double> leftEigenvector){
		
		this.sortedDocsPR = new LinkedList<PRelement>();
		// fill the list to be sorted
		Iterator<String> it = leftEigenvector.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			sortedDocsPR.add(new PRelement(key,leftEigenvector.get(key)));
		}
		Collections.sort(sortedDocsPR); 
	}
			
	public void printFirstKResults() {
		
		for (int i=0;i<K && i<sortedDocsPR.size();i++){
    		Double rank = sortedDocsPR.get(i).pagerank;
    		String doc = sortedDocsPR.get(i).docName;
    		System.out.println((i+1)+": "+doc+"\t"+ rank);   
    	}
	}
	
	public void printLastKResults() {
		
		for (int i=sortedDocsPR.size()-K;i>=0 && i<sortedDocsPR.size();i++){
    		Double rank = sortedDocsPR.get(i).pagerank;
    		String doc = sortedDocsPR.get(i).docName;
    		System.out.println((i+1)+": "+doc+"\t"+ rank);   
    	}
	}
	
	public double printfirstKtests(){
		
		double avgDst =0, avgDiff=0;
		
		for (int i=0;i<K;i++){
    		String doc = sortedDocsPR.get(i).docName;
    		Double rank = sortedDocsPR.get(i).pagerank;
			for (int j=0;j<K;j++){
				if(Integer.parseInt(doc) == targetDocNames[j]){
					System.out.printf("%d.\t%d\t%.4f\t",i+1,Integer.parseInt(doc),rank);
					if(i==j){System.out.print("(ranked OK)\t");}
					else{System.out.print(" +- "+Math.abs(i-j)+" pos\t");}
					
					avgDst += Math.abs(i-j);
					double diff = Math.abs(rank-targetPR[j]);
					avgDiff+=diff;
					System.out.printf("diff:%.6f",diff);
					if(diff > 0.002){System.out.println("\tGreater than the 0.002 limit!");}
					else{System.out.println("\t(within 0.002)");}
					break;
				}
			}
		}		
		
		System.out.println("Average document distance from solution: "+avgDst/(double)K);
		System.out.println("Avg pagerank difference from solutioin: "+avgDiff/(double)K);
		
		return avgDst/(double)K;
	}
	

	public void printTest2(){
		
		double avgDst =0, avgDiff=0;
		int[] prova1 = {1081,522,454,483,526,36,7031,2634,1324,1306,3094,2381,1432,425,9765,2353,5608,365,6287,664,5621,8184,4762,3248,1637,3105,6907,5115,2635,6074,51,8071,8098,6039,6070,2136,3743,838,3931,6722,723,1584,21,4919,840,2134,8179,1327,2022,837}; 
		int[] prova = {1081,522,454,2634,365,36,526,3930,1324,483,7031,3094,2381,1306,9765,6287,1432,2353,4762,5608,5115,5621,425,6722,838,6070,8184,1584,6907,3931,3105,723,6074,2635,2343,8071,8098,1524,2136,21,6039,3743,837,4919,2,664,6451,5559,8813,6071};
		
		for (int i=0;i<K;i++){
    		int doc = prova[i];
    		for (int j=0;j<K;j++){
				if(doc == targetDocNames[j]){
					if(i==j){System.out.println("Doc: "+doc+"\t ranked correctly.\t");}
					else{System.out.println("Doc: "+doc+"\t wrong order +-"+Math.abs(i-j)+" pos\t");}
					
					avgDst += Math.abs(i-j);
					break;
				}
			}
		}		
		
		System.out.println("Average document distance from solution: "+avgDst/(double)K);
		System.out.println("Avg pagerank difference from solutioin: "+avgDiff/(double)K);
		
	}
	
	public static void someGraphs(){
		
		int[] mValues = {1,2,5,10,20,100,200,500};
		long[] times = new long[mValues.length];
		double[] avgs = new double[mValues.length];
		
		PageRank pr = new PageRank("./svwiki_links/links10000.txt", 0.85);

		for(int i=0;i<mValues.length;i++){
			
			int m = mValues[i];
			int T = 100;
			
			long t0 = System.nanoTime();
			
			HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.monteCarlo3, T, m);
			
			long t1 = System.nanoTime();
			System.out.println("Time (ms): "+((t1-t0)/1000000));
			times[i] = (t1-t0)/1000000;
			
			TestingPR tester = new TestingPR(leftEigenvector);
			avgs[i] = tester.printfirstKtests();
		}

		/* prepare output for python file */
		System.out.print("xm = [");
		for(int i=0;i<mValues.length;i++){
			System.out.print(mValues[i]+",");
		}
		System.out.print("]\n yt = [");
		for(int i=0;i<mValues.length;i++){
			System.out.print(times[i]+",");
		}
		System.out.print("]\n ya = [");
		for(int i=0;i<mValues.length;i++){
			System.out.print(avgs[i]+",");
		}
		
	}
	
	
    /* --------------------------------------------- */


    public static void main( String[] args ) {
    	
    	if ( args.length != 1 ) {
    	    System.err.println( "Please give the name of the link file" );
    	    return;
    	}

		int T = 100;
    	int m = 500;
    	double c = 0.85;

	//    PageRank pr = new PageRank("./svwiki_links/links10000.txt", c);

	//    	HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.powerIteration, T, m);
//    	HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.approxNoSinks, T, m);
//    	HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.monteCarlo1, T);
//    	HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.monteCarlo2, T, m);
//		HashMap<String,Double> leftEigenvector = pr.computePagerank(algorithm.monteCarlo3, T, m);
    	
	//	TestingPR tester = new TestingPR(leftEigenvector);
    	//tester.printTest2();
    	//tester.printFirstKResults();
    	//tester.printLastKResults();
    	//tester.printfirstKtests();
    	TestingPR.someGraphs();
		 
    }
    
}
