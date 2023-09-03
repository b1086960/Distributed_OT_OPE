package run;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;
import servers.OPE_Server;

/** This class is consist of all the required function for simulating a OPE protocol between Alice and Bob **/
public class OPE {
	
	/** This function return mediator's list for OPE mediator's **/
	public static OPE_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss) {
		
		// Build D OT1outofN list
		OPE_Server[] m_list = new OPE_Server[D];
		for(int i=0;i<D;i++) {	m_list[i]=new OPE_Server(N,D,i,ss);  }
		for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
		return m_list;
		// Build D OT1outofN list
		
	}
	
	/** Helper function , compute bob vector from the factor **/
	public static BigInteger[] OPE_vector(int N,BigInteger factor) {
		
		// create a random BigInteger[] array of BigInteger factor 
		BigInteger[] arr=new BigInteger[N];
		arr[0]=BigInteger.ONE;
		arr[1]= factor;
		for(int i=2;i<N;i++) {
			arr[i]=arr[i-1].multiply(arr[1]);
		}
		return arr;
		// create a random BigInteger[] array of BigInteger factor
		
	}
	
	/** Bob use this function to get random OPE vector **/
	public static BigInteger[] random_OPE_vector(int N) {
		
		// create random OPE vector for Bob
		int rand_int =(int) Math.floor(Math.random() *10 );
		return OPE_vector(N, BigInteger.valueOf(rand_int));
		// create random OPE vector for Bob
		
	}

	
	/** This function was used to test the DVV process in "Multi-Threading"  mode**/
	public static void main(String[] args){ 
		// this function test that OPE work , you can change Bobvector to illegal value to validate it
			
			// Initial vector's and secret sharing scheme
			BigInteger[] BobVector= DSP.toBig(new long[]{1,10,100,1000,10000,100000,1000000,10000000,100000000}) ;
			int N=BobVector.length;
			BigInteger[] AliceVector= DSP.random_vector(N);
			int D = 7;
			int k = 4;  // 4-out-of-7 shares
			ShamirSecretSharing ss=new ShamirSecretSharing();
			// Initial vector's and secret sharing scheme
			
			
			// Build D OPE mediator's 	
			OPE_Server[] d_list = get_servers_list(D,N,ss); // diffrent validation process
			for(int i=0;i<D;i++) {			d_list[i].start(); 	}
			// Build D OPE mediator's 	
			
			// Run OPE By Using OTK method, since they are both DVV mediator's 
			OT1.oneoutofNOT(BobVector, AliceVector, D, k, ss, d_list);
			// Run OPE By Using OTK method, since they are both DVV mediator's 
			
		// this function test that OPE work , you can change Bobvector to illegal value to validate it	
	}
	
}
