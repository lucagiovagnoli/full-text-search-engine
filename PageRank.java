/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  
package ir;

import java.security.SecureRandom;
import java.util.*;
import java.io.*;


public class PageRank{
	
	public static enum algorithm{
		powerIteration,
		approxNoSinks,
		monteCarlo1,
		monteCarlo2,
		monteCarlo3,
	}

    //Random rnd = new SecureRandom(); // SLOW but GOOD ;)
    public static Random rnd = new Random();     
    private HashMap<String,Double> leftEigenvector = new HashMap<String,Double>();
    private int numberOfDocs; //real n. of docs
    private int N;
    private double c;
    
    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;
    
    /**
     *   Mapping from document names to document numbers.
     */
    private Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    public static String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    private Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    private int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    private int numberOfSinks = 0;
    
    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;
    
    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */

    public PageRank(String filename, double c) {
    	this.numberOfDocs = readDocs( filename );
    	this.N = numberOfDocs*numberOfDocs;
    	this.c=c;
    }

    /* --------------------------------------------- */

    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /**
     *   Computes the pagerank of each document.
     */
    public HashMap<String,Double> computePagerank(algorithm method, int T, int m) {
    	
    	double[] le = new double[numberOfDocs];
    	
    	switch(method){
    	case powerIteration:
    		le = powerIteration();
    		break;
    	case approxNoSinks:
    		le = approximationPagerank();
    		break;
    	case monteCarlo1:
    		le = monteCarlo1(T);
    		break;
    	case monteCarlo2:
    		le = monteCarlo2(T,m);
    		break;
    	case monteCarlo3:
    		le = monteCarlo3(T,m);
    		break;
    	}		
    	
    	for (int i=0;i<numberOfDocs;i++){
    		leftEigenvector.put(docName[i], le[i]);
    	}
    	
    	return leftEigenvector;
    }
    
    private double[] monteCarlo3(int T, int m){
    	
    	double[] pi = new double[numberOfDocs];
    	Integer[][] P = buildMatrixForRandomWalk();
    	
    	System.out.println("Monte Carlo 3 with n="+numberOfDocs+", T="+T+", m="+m);
    	
    	/* Simulate the random walk {Xt }t≥0 exactly m times from each page. 
    	 * For any page i, evaluate πj as the total number of VISITS to 
    	 * page j multiplied by (1 − c)/(n ∗ m). [Paper Avrachenkov]*/
    	for(int i=0;i<numberOfDocs;i++){ //need to start from EVERY doc 
        	for(int j=0;j<m;j++){ // EXACTLY m walks starting from each i document
	        	LinkedList<Integer> traversedNodes = randomWalkMonteCarlo3(T,P,i); // start from document i
	    
	        	/* save into pi */
	        	for(int z=0;z<traversedNodes.size();z++){
	        		pi[traversedNodes.get(z)]++;  	        		
	        	}
        	}
    	}
    	/* build solution */
    	for (int i=0;i<numberOfDocs;i++) {
    		pi[i] = pi[i] * (1-c) / (double) (numberOfDocs*m);
    	}
    	return pi;
    }
    
    private double[] monteCarlo2(int T, int m){
    	
    	double[] pi = new double[numberOfDocs];
    	Integer[][] P = buildMatrixForRandomWalk();
    	
    	System.out.println("Monte Carlo 2 with n="+numberOfDocs+", T="+T+", m="+m);
    	
    	/* Simulate N = mn runs of the random walk {Xt }t≥0 initiated at each 
    	 * page exactly m times. Evaluate πj as a fraction of N random walks which 
    	 * end at page j = 1, . . . , n. [Paper Avrachenkov] */
    	for(int i=0;i<numberOfDocs;i++){ //need to start from EVERY doc 
        	for(int j=0;j<m;j++){ // EXACTLY m walks starting from each i document
	        	int endNode = randomWalk(T,P,i); // start from document i
	    		pi[endNode]++;  
        	}
    	}

    	/* build solution */
    	for (int i=0;i<numberOfDocs;i++) pi[i] = pi[i] / (double) (numberOfDocs*m);
    	return pi;
    }
    
    private double[] monteCarlo1(int T){
    	
    	double[] pi = new double[numberOfDocs];
    	Integer[][] P = buildMatrixForRandomWalk();
    	
    	System.out.println("Monte Carlo 1 with n="+numberOfDocs+", T="+T+", N="+N);
    	
    	for(int i=0;i<N;i++){

    		/* "Consider a random walk {X t} t≥0 that starts from a randomly chosen page." [Paper Avrachenkov] */
        	int startingPoint=teleport(numberOfDocs); // randomly choose a starting doc
        	int endNode = randomWalk(T,P,startingPoint);
    		pi[endNode]++;    		
    	}

    	/* build solution */
    	for (int i=0;i<numberOfDocs;i++) pi[i] = pi[i] / (double)N;
    	return pi;
    }
    
    private int randomWalk(int T, Integer[][] P, int startingPoint){
    	    	    	
    	 /*c=0.85 in Google*/
    	int currentNode = startingPoint;
    	for(int t=0;t<T;t++){
        	
	    	/* "Assume that at each step, the random walk terminates with probability (1 − c),
	    	 * and makes a transition according to the matrix P with probability c." [Paper Avrachenkov] */
			double cRand = rnd.nextDouble();
			
			/* TERMINATION probability 1-c (the surfer is teleporting, so we stop the 
			 * random walk and consider this node as the ending node) */
			if (cRand >= c) return currentNode; 
			
			else { /* with probability c let's consider the matrix P */
				if (P[currentNode]==null) currentNode = teleport(numberOfDocs); // no OUTLINKS -> must teleport

				else{ //case of outlinks
					Integer[] array = P[currentNode];
				   	int indexOfNext = teleport(array.length);
				   	currentNode = array[indexOfNext];
				}
			}
    	}		
    	
    	return currentNode;
    }

    /** "At any node that has outgoing links, the surfer invokes the 
	 *  teleport operation with probability 1-c and the standard random 
	 *  walk (follow an out-link chosen uniformly at random) with 
	 *  probability c." [Manning, An Introduction to Information Retrieval]
	 * */
    private LinkedList<Integer> randomWalkMonteCarlo3(int T, Integer[][] P, int startingPoint){
    	
    	/*c=0.85 in Google*/
    	int currentNode = startingPoint; // don't return starting point
    	LinkedList<Integer> traversedNodes = new LinkedList<Integer>();
    	for(int t=0;t<T;t++){

    		/* "Assume that at each step, the random walk terminates with probability (1 − c),
	    	 * and makes a transition according to the matrix P with probability c." [Paper Avrachenkov] */
			double cRand = rnd.nextDouble();
			
			/* TERMINATION probability 1-c (the surfer is teleporting, so we stop the 
			 * random walk and consider this node as the ending node) */
			if (cRand >= c) return traversedNodes; 

			else { /* with probability c let's consider the matrix P */
				if (P[currentNode]==null) currentNode = teleport(numberOfDocs); // no OUTLINKS -> must teleport

				else{ //case of outlinks
					Integer[] array = P[currentNode];
			    	int indexOfNext = teleport(array.length);
			    	currentNode = array[indexOfNext];
				}
			}
    		traversedNodes.add(currentNode);
    	}		
    	return traversedNodes;
    }
    
    private Integer[][] buildMatrixForRandomWalk(){
    	
    	Integer[][] P = new Integer[numberOfDocs][];
    	
    	for (int i=0;i<numberOfDocs;i++){
    		if(link.get(i)!=null) { // SINK - case of no out links
    			P[i] = link.get(i).keySet().toArray(new Integer[link.get(i).size()]);
    		}
    		else{P[i] = null;}
    	}    	
    	return P;
    }
    
    private int teleport(int numberOfDocs){    	
    	return rnd.nextInt(numberOfDocs); // TELEPORT randomly choose a doc
    }
    
    /**
     * Pagerank with Approximation method (no sinks/dangling nodes)
     */
    private double[] approximationPagerank() {
    	
    	double[] x = new double[numberOfDocs];
    	double[] x1 = new double[numberOfDocs];
    	double[] swap;
    	
    	Arrays.fill(x1, 1/(double)numberOfDocs); // uniform distribution vector

		/* stop if error less than Epsilon or reached max number of iteration*/
    	int t=0;
		for(; computeError(x,x1) > EPSILON && t<MAX_NUMBER_OF_ITERATIONS;t++){
    		swap = x;
    		x = x1;
    		x1 = swap;
    		Arrays.fill(x1,0);
    		approximateStep(x1, x);
		} 
		
		System.out.println("Approximation no-sinks, # of iterations: "+t);

    	return x1;
    }
    
    private void approximateStep(double[] x1, double[] x){
    	
    	for (int i=0;i<numberOfDocs;i++){    		
    		if(link.get(i)!=null) { // case of out links
    			Iterator<Map.Entry<Integer,Boolean>> it = link.get(i).entrySet().iterator();
    			while(it.hasNext()){
	    			int j = it.next().getKey();
	    			x1[j] += x[i]*c/(double)out[i];   
    			}
    		}
			x1[i] += (1-c)/(double)numberOfDocs;
			//x1[i] += numberOfSinks/(double)numberOfDocs/(double)numberOfDocs;
    	}
    }
    	
    /**
     * Compute Pagerank with powerIteration
     */
    private double[] powerIteration() {
    	
    	double[][] P = new double[numberOfDocs][numberOfDocs]; 

    	/* build P matrix for Markov Chain Process*/
    	for (int i=0;i<numberOfDocs;i++){
    		if(link.get(i)==null || link.get(i).size()==0) { // SINK - case of no out links
    			for(int j=0;j<numberOfDocs;j++){
    				P[i][j] =  1/(double)numberOfDocs; // SINK - random teleport with probability (1+c-c)/N
    			}
    		}else{ // case of out links
    			for(int j=0;j<numberOfDocs;j++){
    				P[i][j] = (1-c)/(double)numberOfDocs;	// random teleport with probability (1-c)/N
    				if(link.get(i).get(j) != null){ 		// true in connectivity matrix, nodes connected
    					P[i][j] += c/(double)out[i]; 		// c/(#outlinks)    					
    				}
    			}
    		}
    	}
    	
    	/* Computing pagerank */
    	double[] x = new double[numberOfDocs];
    	double[] x1 = new double[numberOfDocs];
    	double[] swap;
    	
    	Arrays.fill(x1, 1/(double)numberOfDocs); // uniform distribution vector
    	
    	int t = 0;
    	do{
    		swap = x;
    		x = x1;
    		x1 = swap;
    		Arrays.fill(x1,0);
    		nextIteration(x1,x,P);
    		t++;
    		
        	/* stop if error less than Epsilon or reached max number of iteration*/
       	} while(computeError(x,x1) > EPSILON && t<MAX_NUMBER_OF_ITERATIONS);

    	System.out.println("Power Iteration, # of iterations: "+t);
    	
    	return x1;
    }
    
    private void nextIteration(double[] x1, double[] x,double[][] P){
    	    	
    	for(int i=0;i<x.length;i++){    		
    		for (int j=0;j<x.length;j++){
    			x1[i] += x[j]*P[j][i];
      		}
    	}
    }
    
    private double computeError(double[] x,double[] x1){
    	
    	double res = 0;    	
    	for (int i=0;i<x.length;i++){
    		res += Math.abs(x1[i]-x[i]);
    	}
    	return res;
    }
    
}
