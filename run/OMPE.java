package run;
import java.math.BigInteger;
import helpers.KTuplesGenerator;
import helpers.ShamirSecretSharing;
import servers.OMPE_Server;


/** This class is consist of all the required function for simulating a OMPE protocol between Alice and Bob **/
public class OMPE {
	
	 	/** This function return server's list for OMPE server's **/
	   public static OMPE_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss,int kk) {
			
			// Build D OT1outofN list
		   OMPE_Server[] m_list = new OMPE_Server[D];
			for(int i=0;i<D;i++) {	m_list[i]=new OMPE_Server(N,D,i,ss, kk);  }
			for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
			return m_list;
			// Build D OT1outofN list
			
		}
	
	   /** Bob use this function to get random OMPE vector **/
	   public static BigInteger[] random_OMPE_vector(int k,int N,ShamirSecretSharing ss,int mod_value) {
		   
		   // To get valid polynomial by order  
		   BigInteger[] factors = new BigInteger[k];
		   for(int i=0;i<k;i++) {
			   factors[i]=DSP.randomNumber().mod(BigInteger.valueOf(mod_value));  // mod mod_value to make the number not too big for large N=10,000
			   System.out.println(" factor "+i+ " = "+factors[i]);
		   }
		   KTuplesGenerator kt = new KTuplesGenerator(k,N);
		   KTuplesGenerator.clear_tuples();
		   kt.set_tuples();
		   return KTuplesGenerator.setBobVector(N, factors, ss);
		   // To get valid polynomial by order 
		   
	   }
	   
	
	   /** This function was used to test the DVV process in "Multi-Threading" mode **/
	   public static void main(String[] args) { 
		   // this function test that OMPE work , you can change Bob vector to illegal value to validate it
		   
		   	// Initial vector's and secret sharing scheme
			BigInteger[] BobVector= DSP.toBig(new long[]{1,2,0,7,4,0,0,14,0,49,8,0,0,0,28,0,0,98,0,343}) ;
			int D = 7;
			int k = 4;  // 4-out-of-7 shares
			int kk=3;   // dimension of polynomial
			int N =BobVector.length;	
			KTuplesGenerator.clear_tuples();  
			BigInteger[] AliceVector= DSP.random_vector(N);
			ShamirSecretSharing ss=new ShamirSecretSharing();
			// Initial vector's and secret sharing scheme 
							
			// Build D OMPE server's 	
			OMPE_Server[] d_list = new OMPE_Server[D]; // diffrent validation process
			for(int i=0;i<D;i++) {
				d_list[i]=new OMPE_Server(N,D,i,ss,kk);	
				d_list[i].start();
			}
			for(int i=0;i<D;i++) {	d_list[i].set_servers_list(d_list); }
			// Build D OMPE server's  
					
			// Run OMPE using reudction to OT
			OT1.oneoutofNOT(BobVector, AliceVector, D, k, ss, d_list);
			// Run OMPE using reudction to OT
					
	    }
	    
}
