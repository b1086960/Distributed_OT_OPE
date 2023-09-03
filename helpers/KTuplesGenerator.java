package helpers;
import java.math.BigInteger;
import java.util.*;

/** This is Helper class for converting tuple into converting tuple into integer and verse versa for the OMPE problem **/
/** The help class's/function's are for support of the class only **/
public class KTuplesGenerator {

	
	/** intial functions **/
	// All tuples list with sum < T , and k numbers 
    private int k;
    private int T;
    private int N;
    static private ArrayList<ArrayList<Integer>> tuples;
    // All tuples list with sum < T , and k numbers 
    
    public int size() {
    	return tuples.size();
    }
    
    public static void clear_tuples() {
    	tuples=null;
    }
    
    
    /** function for testing **/
    public static void print_tuple(ArrayList<Integer> tt) {
		System.out.println("");
		for(int i=0;i<tt.size();i++) {
			System.out.print(tt.get(i)+ " ");
		}
	}
    
    /** function to create copy of tuples array **/
    public ArrayList<Integer> makeDeepCopyInteger(ArrayList<Integer> old){
    	
    	// Simple function to copy tuple
        ArrayList<Integer> copy = new ArrayList<Integer>(old.size());
        for(Integer i : old){           copy.add(i);        }
        return copy;
        // Simple function to copy tuple
        
    }
    
    /** Bob use this function to remember which tuple fit to which index for the OMPE problem , he send only the k factors **/
    public static BigInteger[] setBobVector(int N,BigInteger[] factors,ShamirSecretSharing ss) {
    	
    	
    	// Bob use this function to set his vector in the right order 
    	BigInteger[] coefficent= new BigInteger[N];
    	coefficent[0]=BigInteger.ONE;
    	   for(int i=1;i<N;i++) {
 
    		  BigInteger sum=BigInteger.ONE;
    		  ArrayList<Integer> t = tuples.get(i);
    		  for(int i2=0;i2<factors.length;i2++) {
    			int power=t.get(i2);
    			if(power>0) {	sum = sum.multiply(  factors[factors.length-1-i2].pow(power) );   }
    		}
    		coefficent[i]=sum; 
    		
    	}
    	//System.out.println("end setting Bob vector");
    	return coefficent;
    	// Bob use this function to set his vector in the right order 
    	
    }
    
    /** Create empty array**/
    public ArrayList<Integer> makezerosArray(int N){
    	
    	// Make zero's tuple 
    	ArrayList<Integer> arr = new ArrayList<Integer>();
		for(int index=0;index<N;index++) {
			arr.add(0);
		}
		return arr;
		// Make zero's tuple 
		
    }
    
    /** Help function **/
 	public static int binomialCoeff(int n, int k)
 	{
 	    // Base Cases
 	    if (k > n)
 	        return 0;
 	    if (k == 0 || k == n)
 	        return 1;
 	 
 	    // Recur
 	    return binomialCoeff(n - 1, k - 1)
 	           + binomialCoeff(n - 1, k);
 	}  

 	 /** This function help find the tuple by index T (with k factors)**/
 	public static long find_N_for_T(int T,int k) { 
 		// for N k tuples , what will be the max value of T 
 		return binomialCoeff(T+k-1,k);
 	}

 	 /** Initial class**/
    public KTuplesGenerator(int k, int N) {
    	// Set all tuples list with sum < T , and k numbers 
        this.k = k;
        int T=1;
        while( find_N_for_T(T,k) < N) {
        	T++;
        }
        this.T=T;
        this.N = N;
       
        
    }
    
    /** This function set the tuples**/
    public void set_tuples() {
    	synchronized (KTuplesGenerator.class) {
    		 if( KTuplesGenerator.tuples==null) {
    			 KTuplesGenerator.tuples = new ArrayList<>();
                 generateKTuplesHelper(new ArrayList<>());
                 Collections.sort(KTuplesGenerator.tuples, new TupleComparator());
    		 }
		}
    }
    
    /** Help function **/
    private int binarySearch(ArrayList<Integer> target,int start,int end) {
    	int middle = (int) (start+end)/2;
    	int comp = compare_tuples_for_search(KTuplesGenerator.tuples.get(middle), target);
    	if(comp==0) {
    		return middle;
    	}else {
    		if(middle==start) {	return -1; }
    		
    		if(comp <0) {	return binarySearch(target,middle,end);  }
    		return binarySearch(target,start,middle);
    	}
    }
    
    /** Help function **/
    public int binarySearch( ArrayList<Integer> target) { 	
    	// binary search	
       int N     = KTuplesGenerator.tuples.size();
       int index = binarySearch(target,0,N);
       // binary search
       return index;  
    }

    /** Return index of tuple by tuple **/
    public int getTupleOrder(ArrayList<Integer> tuple) {
    	
    	// Tuple to number 
        return binarySearch(tuple);
        // Tuple to number
        
    }

    /** return tuple by index **/
    public ArrayList<Integer> getTupleByOrder(int order) {
    	
    	// Number to tuple
    	
        ArrayList<Integer> tuple= KTuplesGenerator.tuples.get(order);
        return makeDeepCopyInteger(tuple);
        // Number to tuple
        
    }

    /** Help function **/
    private void generateKTuplesHelper(ArrayList<Integer> currentTuple) {
    	if(KTuplesGenerator.tuples.size() > this.N) {
    		return;
    	}
    
    	
    	// Set all tuples list with sum < T , and k numbers 
        if (currentTuple.size() == k) {
            int sum = 0;
            for (int num : currentTuple) {
                sum += num;
            }
            if (sum < T) {
                KTuplesGenerator.tuples.add(currentTuple);
            }
            return;
        }
        for (int i = 0; i < T; i++) {
            ArrayList<Integer> newTuple = new ArrayList<>(currentTuple);
            newTuple.add(i);
            generateKTuplesHelper(newTuple);
        }
        // Set all tuples list with sum < T , and k numbers
        
    }
    
    /** Help function **/
    public int compare_tuples_for_search(ArrayList<Integer> tuple1, ArrayList<Integer> tuple2) {
    	
    	// helper for binary search
    	int sum1 = 0;
        int sum2 = 0;
        for (int num : tuple1) {
            sum1 += num;
        }
        for (int num : tuple2) {
            sum2 += num;
        }
        if(sum1!=sum2) {
        	return sum1 - sum2;
        }
        
        for(int i=0;i<tuple1.size();i++) {
        	if(!tuple1.get(i).equals(tuple2.get(i))) {
        		return tuple1.get(i)-tuple2.get(i);
        	}
        }
        return 0;
        // helper for binary search
        
    }

    /** Help Class **/
    private class TupleComparator implements Comparator<ArrayList<Integer>> {
        @Override
        public int compare(ArrayList<Integer> tuple1, ArrayList<Integer> tuple2) {
        	
        	// helper for creation 
            int sum1 = 0;
            int sum2 = 0;
            for (int num : tuple1) {
                sum1 += num;
            }
            for (int num : tuple2) {
                sum2 += num;
            }
            return sum1 - sum2;
            // helper for creation 
            
        }
    }

 

}