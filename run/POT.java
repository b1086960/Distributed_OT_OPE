package run;
import java.math.BigInteger;
import java.util.Random;

import helpers.ShamirSecretSharing;
import servers.POT_Server;

/** This class is consist of all the required function for simulating a POT protocol between Alice and Bob **/
public class POT {

	/** This function return server's list for POT server's **/
	public static POT_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss) {
		
		// Build D POT list
		POT_Server[] m_list = new POT_Server[D];
		for(int i=0;i<D;i++) {	m_list[i]=new POT_Server(N,D,i,ss);  }
		for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
		return m_list;
		// Build D POT list
		
	}
	
	/** Bob/Alice use this function to share T^A/T^B **/
	public static void share_threshold(BigInteger value, int k , int D,ShamirSecretSharing ss,POT_Server[] d_list,boolean fromBob) {
		BigInteger[][] shares = ss.splitSecret(value, k, D);
		if(fromBob) { System.out.println(" Bob share treshold "+value);}
		else { System.out.println(" Alice share treshold "+value);}
	    for(int i2=0;i2<D;i2++) {
	    	BigInteger share_value= shares[i2][1];
	        int share_index=(int) shares[i2][0].longValue()-1;
	        POT_Server med = d_list[share_index];
	        if(fromBob) { 	med.BobSetTreshold(share_value); }  // Bob send entry
	        else {  		med.AliceSetTreshold(share_value);  }  // Alice send entry 
	    }
	}
	
	/** Alice use this function to share the prices vecotr with the server's **/
	public static void share_prices(BigInteger[] pricesVector, int k , int D,ShamirSecretSharing ss,POT_Server[] d_list) {
		int N = pricesVector.length;
		for(int i=0;i< N;i++) {
			// share each price to servers 
			BigInteger price = pricesVector[i];
			//System.out.println("share price "+i+ " : "+ price);
			BigInteger[][] shares = ss.splitSecret(price, k, D);
		    for(int i2=0;i2<D;i2++) {
		    	BigInteger share_value= shares[i2][1];
		        int share_index=(int) shares[i2][0].longValue()-1;
		        POT_Server med = d_list[share_index];
		        med.receivePriceFromAlice(i,share_value);
		    }
		 // share each price to servers 
		}
		
	}
	
	/** Alice use this function to generate random prices vector **/
	public static BigInteger[] random_prices_vector(int N,int T) {
		BigInteger[] prices = new BigInteger[N];
		for(int i=0;i<T;i++) {
			prices[i]=BigInteger.valueOf(i+1);
		}
		Random rando = new Random();
		for(int i=T;i<N;i++) {
			int rand;
			rand = rando.nextInt(10 ) + 1;
			prices[i]=BigInteger.valueOf(rand);
		}
		return prices;
	}
	
	/** Bob use this function to select a random set of prices , with sum = T **/
	public static BigInteger[] random_selection(BigInteger[] prices,int T) {
		BigInteger Treshold = BigInteger.valueOf(T);
		BigInteger sum=BigInteger.ZERO;
		Random rando = new Random();
		int N=prices.length;
		BigInteger[] zeros_and_ones=new BigInteger[N];
		for(int i=0;i<T;i++) {
			zeros_and_ones[i]=BigInteger.ZERO;
		}
		for(int i=T;i<N;i++) {
			zeros_and_ones[i]=BigInteger.ZERO;
			if(sum.add(prices[i]).compareTo(Treshold) <1) {
				if( (rando.nextInt(10 ) + 1)%2==1) {
					sum=sum.add(prices[i]);
					zeros_and_ones[i]=BigInteger.ONE;
				}
			}
		}
		//System.out.println("sum="+sum);
		if(sum.compareTo(Treshold) <0) {
			BigInteger diff = Treshold.subtract(sum).subtract(BigInteger.ONE);
			zeros_and_ones[diff.intValue()]=BigInteger.ONE;
			//System.out.println("diff="+diff);
		}
		
		
		return zeros_and_ones;
	}
	
	  /** This function was used to test the DVV process in "Multi-Threading" mode **/
	  public static void main(String[] args) {
		  
		// Initial vector's and secret sharing scheme
		BigInteger[] BobVector= DSP.toBig(new long[]{1,1,1,0,0,0}) ;
		int N=BobVector.length;
		BigInteger[] AliceVector= DSP.random_vector(N);
		BigInteger[] AlicePricesVector= DSP.toBig(new long[]{1,2,3,4,5,6}) ;
		int D = 6;
		int k = 3; 
		ShamirSecretSharing ss=new ShamirSecretSharing();
		BigInteger bobTreshold = BigInteger.valueOf(6);
		BigInteger aliceTreshold = BigInteger.valueOf(6);
		// Initial vector's and secret sharing scheme
		
		// Build D POT server's 	
		POT_Server[] d_list = get_servers_list(D,N,ss); // diffrent validation process
		for(int i=0;i<D;i++) {			d_list[i].start(); 	}
		// Build D POT server's 
		
		// Set N zeros for secret share scrambling
		for(int i2=0;i2<N;i2++) {
			for(int i=0;i<D;i++) {				d_list[i].ServerSetZero(i2)  ;	}
			for(int i=0;i<D;i++) {				d_list[i].set_zeros_share(i2);	}
		}
		// Set N zeros for secret share scrambling
		
		// Alice shares prices 
		share_prices(AlicePricesVector,k,D,ss,d_list);
		// Alice shares prices
		
		// Alice and bob send treshold to servers 
		share_threshold(bobTreshold,k,D,ss,d_list,true);
		share_threshold(aliceTreshold,k,D,ss,d_list,false);
		// Alice and bob send treshold to servers
		
		// Run POT By Using OTK method, since they are both DVV server's 
		OTK.koutofN(BobVector, AliceVector, k, D, N, ss, d_list,null);
		// Run POT By Using OTK method, since they are both DVV server's 
	  }
	  
}
