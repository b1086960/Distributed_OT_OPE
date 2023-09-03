package run;
import java.math.BigInteger;
import servers.DSP_Server;
import servers.GOT_Server;
import servers.OTK_Server;

import java.security.SecureRandom;

import helpers.Setting;
import helpers.ShamirSecretSharing;

/** This class is used for measuring time in all DSP-based protocols **/
public class DSP { 

	
	// values for run-time analysis 
	public static int validation_stack_size;
	static SecureRandom random = new SecureRandom();
	// values for run-time analysis 
	
	
	
	
	/** This function create inner product between two vector's to validate the DSP process working after the testing **/
	public static BigInteger innerProduct(BigInteger[] arr1, BigInteger[] arr2) {
		
		// Simple function for compute inner product between two BigInteger vector , to validate that the method work 
		BigInteger res=BigInteger.ZERO;
		for(int i =0;i<arr1.length;i++) {
			BigInteger addtores=arr1[i].multiply(arr2[i]);
			res=res.add(addtores);
		}
		return res;
		// Simple function for compute inner product between two BigInteger vector , to validate that the method work 
		
	}
	
	
	/** This function is use to convert long array to bigInteger array for some testing of OMPE that cannot run on very big integer */
	public static BigInteger[] toBig(long[] arr) {
		
		// Just helper function to cover long[] to BigInteger[]
		int N=arr.length;
		BigInteger[] newarr = new BigInteger[N];
		for(int i=0;i<N;i++) {
			newarr[i]=BigInteger.valueOf(arr[i]);
		}
		return newarr;
		// Just helper function to cover long[] to BigInteger[]
		
	}
	
	/** This function is used by bob to recover the result for each MPC protocol he run**/
	public static BigInteger recoverSecretFromservers( DSP_Server[] d_list,ShamirSecretSharing ss) {
		
		// Bob Use this function to recover his secret from the shares he gets 
		int D=d_list.length;
		BigInteger[][] res_shares=new BigInteger[D][2];
		for(int i=0;i<D;i++) {
			BigInteger result=d_list[i].getResult();
			res_shares[i][0]=BigInteger.valueOf(i+1);
			res_shares[i][1]=result;
		}
		return ss.recoverSecret(res_shares).mod(ss.P());
		// Bob Use this function to recover his secret from the shares he gets 
		
	}
	
	/** This function is used to set a list/set of server's for the protocol **/
	public static DSP_Server[] get_servers_list(int D,int N,BigInteger P) {
		
		// Build D server's list
		DSP_Server[] m_list = new DSP_Server[D];
		for(int i=0;i<D;i++) {
			m_list[i]=new DSP_Server(N,D,P);	
		}
		return m_list;
		// Build D server's list
		
	}
	
	/** This function compare the inner product of two vector , to the result of each MPC protocol to validate its working **/
	/** To test it in the case of k-out-of-N OT/POT/GOT (which send back k result's) it validate the inner product of the coefficients instead **/
	public static void check_secret(BigInteger[] BobVector,BigInteger[] AliceVector,DSP_Server[] m_list,ShamirSecretSharing ss,boolean inKsecretsForm,BigInteger[] setInnerInAdvace,int k,int D) {
		
		// Validate if secret shares produce the same value as the function they suppose to compute 
		if(inKsecretsForm) {
			
			// if its k secret s then validate them one by one or exit error method failed 
			int N=BobVector.length;
			
			
			// recover the secrets and the results by computing the DSP of each secret separately 
			BigInteger[] recoveredSecret = OTK.recoverSecretFromkservers(BobVector,(OTK_Server[])m_list,ss,N);
			BigInteger[] innerResult;
			if(setInnerInAdvace==null) {
				innerResult=OTK.innerProductArr(BobVector,AliceVector);
			}else {
				innerResult=setInnerInAdvace;
				
				/** This part of the code is used for the GOT protocol to validate S^A=S^B, for receiving the encryption keys , and decrypting the massages **/
				BigInteger S =GOT.recoverSfromEncryptedVector(AliceVector,BobVector,ss);
				GOT.share_secretS(S, k, D, ss, (GOT_Server[]) m_list, true);
				for(int i=0;i<D;i++) {  ((GOT_Server)m_list[i]).pre_encryption_keys(); }
				BigInteger[] encryption_keys= GOT.get_encryption_keys(N, D, (GOT_Server[]) m_list, BobVector, ss);
				for(int i=0;i<N;i++) { recoveredSecret[i]=recoveredSecret[i].mod(GOT.p).subtract(encryption_keys[i]);} 
				/** This part of the code is used for the GOT protocol to validate S^A=S^B, for receiving the encryption keys , and decrypting the massages **/
			}
			
			// recover the secrets and the results by computing the DSP of each secret separately 
			
			// then validate all are equals to what they should be 
			boolean vectors_are_equal=true;
			for(int i=0;i<N;i++) {
				if(!BobVector[i].equals(BigInteger.ZERO)) {
					System.out.println("Original value at index "+i+": " +innerResult[i] );
					System.out.println("result from servers at index "+i+": " + recoveredSecret[i]);
					if(!innerResult[i].equals(recoveredSecret[i])) {
						vectors_are_equal=false;
					}
				}
			}
			if(vectors_are_equal) {
				System.out.println("method working");
			}else {
				System.out.println("method failed");
				System.exit(0);
			}
			// then validate all are equals to what they should be 
			
			
			// if its k secret s then validate them one by one or exit error method failed 
			
			
		}else {
			
			// else single secret , validate it equal to DSP 
			BigInteger recoveredSecret = recoverSecretFromservers(m_list,ss); // recover value from shares 
			BigInteger innerResult=innerProduct(BobVector,AliceVector).mod(ss.P());         // the vlaue compute directly 
			System.out.println("Inner product value: " +innerResult );
			System.out.println("result recover from servers shares: " + recoveredSecret);
			if(innerResult.equals(recoveredSecret)) {
				System.out.println("method working ");
			}else {
				System.out.println("method failed"); // or exit method failed 
				System.exit(0);
			}
			// else single secret , validate it equal to DSP
			
		}
		// Validate if secret shares produce the same value as the function they suppose to compute
		
	}
	
	/** This function is used in order to generate 64-bit random numbers **/
	public static BigInteger randomNumber() {
		return (BigInteger) new BigInteger(64, DSP.random); 	
	}
	
	/** This function generate vector with size N of 64-bits numbers **/
	public static BigInteger[] random_vector(int N) {
		
		// generate random vector with size N
		BigInteger[] arr=new BigInteger[N];
		
		for(int i=0;i<N;i++) {
			arr[i]= randomNumber();//ThreadLocalRandom.current().nextLong(N);
		}
		return arr;
		// generate random vector with size N
		
	}
	
	/** Bob and Alice use this function to shares their vectors entries to the server's **/
	public static void share_vector_entry(BigInteger value,int index,int k,int D,boolean fromBob,ShamirSecretSharing ss,DSP_Server[] d_list) {
		
		// Each of Bob or Alice vector entry is send to all the D server's after using shamir secret sharing scheme  
		BigInteger[][] shares = ss.splitSecret(value, k, D);
	    for(int i2=0;i2<D;i2++) {
	    	BigInteger share_value= shares[i2][1];
	        int share_index=(int) shares[i2][0].longValue()-1;
	        DSP_Server med = d_list[share_index];
	        if(fromBob) {
	        	med.receiveFromBob(index,share_value);   // Bob send entry
	        }else {
	        	med.receiveFromAlice(index,share_value); // Alice send entry
	        }
	    }
	    // Each of Bob or Alice vector entry is send to all the D server's after using shamir secret sharing scheme 
	    
	}
	
	/** Bob and Alice use this function to shares their vectors to the server's **/
	public static void share_vector(BigInteger[] vector,int k,int D,boolean fromBob,ShamirSecretSharing ss,DSP_Server[] d_list) {
		
		// Alice or Bob share their Vector with the server's , entry by entry 
		int N=vector.length;
		for(int i=0;i<N;i++) {
			share_vector_entry(vector[i],i,k,D,fromBob,ss,d_list);
		}
		// Alice or Bob share their Vector with the server's , entry by entry 
		
	}
	
	/** This Function return the current time in milliseconds **/
	public static long time() {
		return System.currentTimeMillis();	
	}
	
	
	/** To test any DSP-based protocol , use this function for measuring times **/
	public static long[][] runDSP(int N,int D , DSP_Server[] m_list ,BigInteger[] BobVector ,BigInteger[] AliceVector,ShamirSecretSharing ss, boolean inKsecretsForm,long t0,BigInteger[] setInnerInAdvace) { //  D number of server's
		
		// Alice do her part 
		int k = (int) (Math.floor(D/2)+1); // treshold of server's
		share_vector(AliceVector,k,D,false,ss,m_list);
		// Alice do her part
				
		// Sharing the Vector's with the server's
		long sharing_vectors_start=DSP.time();
		share_vector(BobVector,k,D,true,ss,m_list);
		long sharing_vectors_end=DSP.time();
		// Sharing the Vector's with the server's
		
		// Sharing the Vector's with the server's
		for(int i=0;i<m_list.length;i++) {	
			if(Setting.testing_mode) {
				m_list[i].run_unsync();    // simulate the times without real DVV
			}else {
				//m_list[i].start(); // Run the real DVV in "Multi-Threading" mode
			}
		} 
		// Sharing the Vector's with the server's
				
		// Checking the result is equal to the inner product of the vectors
		check_secret( BobVector, AliceVector,m_list, ss,inKsecretsForm,setInnerInAdvace,k,D);
		System.out.println("D:"+D+" N:"+N);
		long process_end=DSP.time();
		validation_stack_size=m_list[0].validation_stack_size();
		// Checking the result is equal to the inner product of the vectors
		
		// create table of times from server's
		long[][] table = new long[D+1][4];
		for(int i=0;i<D;i++) {
			long[] times=m_list[i].get_times();
			for(int i2=0;i2<4;i2++) {
				table[i][i2]=times[i2];
			}
		}
		table[D][0]=t0;
		table[D][1]=sharing_vectors_start;
		table[D][2]=sharing_vectors_end;
		table[D][3]=process_end;
		// create table of times from server's
		
		// returning result
		return table;
		// returning result
	}
};
