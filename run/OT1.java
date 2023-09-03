package run;
import java.math.BigInteger;

import helpers.ShamirSecretSharing;
import servers.DVV_Server;
import servers.OT1_Server;

/** This class is consist of all the required function for simulating a 1-out-of-N OT protocol between Alice and Bob **/
public class OT1 {
	
	/** This function return server's list for OT1 server's **/
	public static OT1_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss) {
		
		// Build D OT1outofN list
		OT1_Server[] m_list = new OT1_Server[D];
		for(int i=0;i<D;i++) {	m_list[i]=new OT1_Server(N,D,i,ss);  }
		for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
		return m_list;
		// Build D OT1outofN list
		
	}
	
	/** Bob use this function to choose random unit-vector **/
	public static BigInteger[] randomUnitVector(int N) {
		
		// create random unit vector for Bob
		BigInteger[] arr=new BigInteger[N];
		for(int i=0;i<N;i++) {
			arr[i]=BigInteger.ZERO;
		}
		
		int rand_index =(int) Math.floor(Math.random() *N );
		arr[rand_index]=BigInteger.ONE;
		return arr;
		// create random unit vector for Bob
		
	}
	
	/** Bob use this function to run 1-out-of-N OT **/
	public static void oneoutofNOT(BigInteger[] BobVector,BigInteger[] AliceVector,int D,int k,ShamirSecretSharing ss,DVV_Server[] d_list) {
		
		// Share the vectors with D server's 
		DSP.share_vector(BobVector,k,D,true,ss,d_list);
		DSP.share_vector(AliceVector,k,D,false,ss,d_list);
		// Share the vectors with D server's 
				
		// Bob get the result from shares and validate its correct 
		BigInteger recoveredSecret = DSP.recoverSecretFromservers(d_list,ss);
		BigInteger innerResult=DSP.innerProduct(BobVector,AliceVector);
		System.out.println("Original value: " +innerResult );
		System.out.println("result from servers: " + recoveredSecret);
		if(innerResult.equals(recoveredSecret)) {
			System.out.println("method working");
		}else {
			System.out.println("method failed");
		}
		// Bob get the result from shares and validate its correct 
		
	}
	
	
	/** This function was used to test the DVV process in "Multi-Threading" mode **/
	public static void main(String[] args){ 
		
		// Initial vector's and secret sharing scheme 
		BigInteger[] BobVector= DSP.toBig(new long[]{0,0,1,0,0,0}) ;
		BigInteger[] AliceVector= DSP.toBig(new long[]{31232123,455435,53386634,6444,6632,1044453});
		int D = 7;
		int k = 4; // 4-out-of-7 shares 
		int N=BobVector.length;
		ShamirSecretSharing ss=new ShamirSecretSharing();
		// Initial vector's and secret sharing scheme 
		
		// build D 1-out-of-N server's 
		OT1_Server[] d_list = new OT1_Server[D];
		for(int i=0;i<D;i++) {
			d_list[i]=new OT1_Server(N,D,i,ss);	
			d_list[i].start();
		}
		for(int i=0;i<D;i++) {
			d_list[i].set_servers_list(d_list);
		}
		// build D 1-out-of-N server's
		
			
		// Set N zeros for secret share scrambling
		for(int i2=0;i2<N;i2++) {
			for(int i=0;i<D;i++) {				d_list[i].ServerSetZero(i2)  ;	}
			for(int i=0;i<D;i++) {				d_list[i].set_zeros_share(i2);	}
		}
		// Set N zeros for secret share scrambling
				
		// Run 1-out-of-N-OT
		oneoutofNOT( BobVector, AliceVector, D, k, ss, d_list);
		// Run 1-out-of-N-OT
		
	}
	
}
