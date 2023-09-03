package run;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;
import servers.GOT_Server;
import servers.OTK_Server;

/** This class is consist of all the required function for simulating a k-out-of-N OT protocol between Alice and Bob **/
public class OTK {
	
	/** This function return server's list for OTK server's **/
	public static OTK_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss,int k) {
		
		// Build D OTkoutofN list
		//System.out.println(" k ="+k);
		OTK_Server[] m_list = new OTK_Server[D];
		for(int i=0;i<D;i++) {	
			m_list[i]=new OTK_Server(N,D,i,ss,k);  
		}
		for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
		return m_list;
		// Build D OTkoutofN list
		
	}
	
	/** Bob use this function to create vector with N-k zeros and k ones **/
	public static BigInteger[] randomKUnitsVector(int N,int k) {
		
		// create random k ones and rest zeros vector for Bob
		BigInteger[] arr=new BigInteger[N];
		for(int i=0;i<N;i++) {
			arr[i]=BigInteger.ZERO;
		}
		
		int counter=0;
		int[] k_random_uniqe_indexs=new int[k];
		while(counter < k) {
			int rand_index =(int) Math.floor(Math.random() *N );
			boolean is_in_arr=false;
			for(int i=0;i<=counter;i++) {
				if(k_random_uniqe_indexs[i]==rand_index) {
					is_in_arr=true;
				}
			}
			
			if(!is_in_arr) {
				arr[rand_index]=BigInteger.ONE;
				k_random_uniqe_indexs[counter]=rand_index;
				counter++;
			}
		}
		
		
		return arr;
		// create random k ones and rest zeros vector for Bob
		
	}
	
		/** This function is used to create inner product between coeffient's , to validate the protocol work after it run **/
		public static BigInteger[] innerProductArr(BigInteger[] BobVector,BigInteger[] AliceVector) {
			
			// Simple funtion for Inner product coefficent's 
			int N=BobVector.length;
			BigInteger[] res = new BigInteger[N ];
			for(int i=0;i<N;i++) {
				res[i]=BobVector[i].multiply(AliceVector[i]);
			}
			return res;
			// Simple funtion for Inner product coefficent's 
			
		}
		
		/** This function allow Bob to recover all secret's he choose by his 1s entries **/
		public static BigInteger[] recoverSecretFromkservers(BigInteger[] BobVector, OTK_Server[] m_list,ShamirSecretSharing ss,int N) {
			
			// Simple help function to recover the k secret's 
			int D=m_list.length;
			BigInteger[] result = new BigInteger[N];
			BigInteger[][] from_servers = new BigInteger[D][N];
			
			for(int i=0;i<D;i++) {
				BigInteger[] from_server=m_list[i].getkResult();
				for(int i2=0;i2<N;i2++) {
					from_servers[i][i2]=from_server[i2];
				}
			}
			
			for (int i2 = 0; i2 < N;i2++) {
				result[i2]=BigInteger.ZERO;
				if(BobVector[i2].equals(BigInteger.ONE)) {
					BigInteger[][] res_shares=new BigInteger[D][2];
					for(int i=0;i<D;i++) {
						res_shares[i][0]=BigInteger.valueOf(i+1);
						res_shares[i][1]=from_servers[i][i2];
						
					}
					result[i2]= ss.recoverSecret(res_shares).mod(ss.P());
					System.out.println("bob recover secret "+result[i2]);
					
				}
			}
			return result;
			// Simple help function to recover the k secret's 
			
		}
		
		/** Bob use this function to run k-out-of-N OT **/
		public static void koutofN(BigInteger[] BobVector,BigInteger[] AliceVector,int k,int D,int N,ShamirSecretSharing ss,OTK_Server[] d_list,BigInteger[] setInnerInAdvace) {
			
			// Share the vectors with D server's 
			DSP.share_vector(BobVector,k,D,true,ss,d_list);
			DSP.share_vector(AliceVector,k,D,false,ss,d_list);
			// Share the vectors with D server's 
			
			// Bob get the result from shares and validate its correct 
			BigInteger[] recoveredSecret;
			BigInteger[] innerResult;
			if(setInnerInAdvace== null) {
				innerResult=innerProductArr(BobVector,AliceVector);
				recoveredSecret = recoverSecretFromkservers(BobVector,d_list,ss,N);
			}else {
				innerResult=setInnerInAdvace; // for GOT in which we want to validate the result , delete this varible in real implementation
				
				// GOT recovery 
				BigInteger S =GOT.recoverSfromEncryptedVector(AliceVector,BobVector,ss);
				GOT.share_secretS(S, k,D, ss, (GOT_Server[]) d_list, true);
				for(int i=0;i<D;i++) {  ((GOT_Server)d_list[i]).pre_encryption_keys(); }
				BigInteger[] encryption_keys= GOT.get_encryption_keys(N, D, (GOT_Server[]) d_list, BobVector, ss);
				recoveredSecret = recoverSecretFromkservers(BobVector,d_list,ss,N);
				for(int i=0;i<N;i++) { 
					if(BobVector[i].equals(BigInteger.ONE)) {
						BigInteger formerrecover =recoveredSecret[i].mod(GOT.p);
						recoveredSecret[i]=formerrecover.subtract(encryption_keys[i]);
					}
				} 
				// GOT recovery
			
			}
			
			boolean vectors_are_equal=true;
			for(int i=0;i<N;i++) {
				if(BobVector[i].equals(BigInteger.ONE)) { // for every entry of bob vector=1 
					System.out.println("Original value at index "+i+": " +innerResult[i] );
					System.out.println("result from servers at index "+i+": " + recoveredSecret[i]);
					if(!innerResult[i].equals(recoveredSecret[i])) { // Bob Should be able to recover his secret
						vectors_are_equal=false; // or method failed
						System.out.println("method failed");
						System.exit(0);
					}
				}
				
			}
			if(vectors_are_equal) {
				System.out.println("method working");
			}else {
				System.out.println("method failed");
			}
			// Bob get the result from shares and validate its correct 
			
		}
		
		/** This function was used to test the DVV process in "Multi-Threading" mode **/
		
		public static void main(String[] args){
				
				// Initial vector's and secret sharing scheme  
				BigInteger[] BobVector= DSP.toBig(new long[]{1,1,0,1,0,1}) ;
				BigInteger[] AliceVector= DSP.toBig(new long[]{31232123,455435,53386634,6444,6632,1044453});
				int D = 7;
				int k = 4; // 4-out-of-7 shares
				int kk=4; // number of secret's allowed
				int N=BobVector.length;
				ShamirSecretSharing ss=new ShamirSecretSharing();
				// Initial vector's and secret sharing scheme 
				
				
				// Build D k-out-of-N server's 
				OTK_Server[] d_list = new OTK_Server[D];
				for(int i=0;i<D;i++) {
					d_list[i]=new OTK_Server(N,D,i,ss,kk);	// k = 3
					d_list[i].start();
				}
				for(int i=0;i<D;i++) {
					d_list[i].set_servers_list(d_list);
				}
				// Build D k-out-of-N server's 
				
				// Set N zeros for secret share scrambling
				for(int i2=0;i2<N;i2++) {
					for(int i=0;i<D;i++) {				d_list[i].ServerSetZero(i2)  ;	}
					for(int i=0;i<D;i++) {				d_list[i].set_zeros_share(i2);	}
				}
				// Set N zeros for secret share scrambling
						
				// Run k-out-of-N OT ( for k=3)
				koutofN( BobVector, AliceVector, k, D, N, ss, d_list,null);
				// Run k-out-of-N OT
				
			}
			
}
