package run;
import java.math.BigInteger;
import java.security.SecureRandom;
import helpers.Setting;
import helpers.ShamirSecretSharing;
import servers.GOT_Server;

/** This class is consist of all the required function for simulating a GOT protocol between Alice and Bob **/
public class GOT {
	
	/** This function return server's list for GOT server's **/
	  public static GOT_Server[] get_servers_list(int D,int N,ShamirSecretSharing ss,int kk) {
			
			// Build D OT1outofN list
		  	GOT_Server[] m_list = new GOT_Server[D];
			for(int i=0;i<D;i++) {	m_list[i]=new GOT_Server(N,D,i,ss, kk);  }
			for(int i=0;i<D;i++) {	m_list[i].set_servers_list(m_list);  }
			return m_list;
			// Build D OT1outofN list
			
		}
	
	  // Initial variables
	  public static SecureRandom random = new SecureRandom();
	  public static BigInteger p,q;
	  // Initial variables
	  
	  
	  /** This function is used by Alice to send encryption keys to server's **/ 
	  public static void share_encryptionsKeys(BigInteger[] ek, int k , int D,ShamirSecretSharing ss,GOT_Server[] d_list) {
			int N = ek.length;
			for(int i=0;i< N;i++) {
				// share each encryption key to servers 
				BigInteger encryptionkey = ek[i];
				BigInteger[][] shares = ss.splitSecret(encryptionkey, k, D);
			    for(int i2=0;i2<D;i2++) {
			    	BigInteger share_value= shares[i2][1];
			        int share_index=(int) shares[i2][0].longValue()-1;
			        GOT_Server med = d_list[share_index];
			        med.AliceSetEncryptionKey(i,share_value);
			    }   
			   // share each encryption key to servers 
			}
	 }
	  
	  /** This function is used by Alice/Bob to send S^A/S^B to server's **/ 
	  public static void share_secretS(BigInteger S, int k , int D,ShamirSecretSharing ss,GOT_Server[] d_list,boolean fromBob) {
		  BigInteger[][] shares = ss.splitSecret(S, k, D);
		  for(int i2=0;i2<D;i2++) {
		    	BigInteger share_value= shares[i2][1];
		       
		        GOT_Server med = d_list[i2];
		        if(fromBob) { med.BobSetSecretS(share_value); }
		        else { med.AliceSetSecretS(share_value);}
		    }
	  }
	
	  /** This function is used to create random prime **/ 
	  public static BigInteger random_prime(int size) {
	        return BigInteger.probablePrime(size, random);
	  }
	  
	  /** This function is used by Alice/Bob so send shares in S^A/S^B to server's **/ 
	  public static BigInteger[] split_into_k(int k,BigInteger S) {
		  BigInteger[] shares = new BigInteger[k];
		  BigInteger sum=BigInteger.ZERO;
		  for(int i=0;i<k-1;i++) {
			  BigInteger sumshare = new BigInteger(31, random);
			  sum=sum.add(sumshare);
			  shares[i]=sumshare;
		  }
		  BigInteger remain = GOT.p.subtract(sum.mod(p)).add(S).mod(p);
		  if(remain.signum()==-1) {
			  System.out.println("remain.signum()==-1");
			  System.exit(0);
		  }
		  remain = remain.mod(GOT.p);
		  shares[k-1]=remain;
		  return shares;
	  }
	  
	  /** This function is used by Alice to generate simple access structure **/
	  /** IN which every 10 corresponding index's are permissive set , and every other set is not **/
	  /** Meaning [0,...,9] , [10,...,19] , ... are permissive set's **/ 
	  public static BigInteger[] generate_S_keys(int k,int N ,BigInteger S,ShamirSecretSharing ss) {
		  // generate array of N cells in which every 10 cells is ss of S 
		  BigInteger[] sk =new BigInteger[N];
		  int counter=0;
		  for(int i=0;i<N/k;i++) {
			  BigInteger[] shares =   split_into_k(k,S); // ss.splitSecret(S, k, k);
			  for(int i2=0;i2<k;i2++) {
				  BigInteger share=shares[i2]; //shares[i2][1];
				  sk[counter]=share;
				  counter++;
			  }
		  }
		  
		  
		  return sk;
		  // generate array of N cells in which every 10 cells is ss of S 
	  }
	  
	  /** Alice use this function to encrypt her massages **/
	  public static BigInteger[] encryptedAliceVector(BigInteger[] AliceVector,BigInteger[] encryptionKeys) {
		  int N =AliceVector.length;
		  BigInteger[] encryptedVector = new BigInteger[N];
		  for(int i=0;i<N;i++) {
			  encryptedVector[i]=AliceVector[i].add(encryptionKeys[i]);
		  }
		  return encryptedVector;
	  }
	  
	 
	  /** Alice use this function to generate q,p **/
	  public static void generate_p_and_q() {
		  // find two primes so q>p^2
		  GOT.p = random_prime(Setting.fieldSize+2);
		  //GOT.q = random_prime(512); //(Setting.fieldSize+1)*2
		 // while( GOT.q.compareTo(GOT.p.multiply(GOT.p))!=1 ) {	   GOT.q = random_prime(Setting.fieldSize*2+3);   }
		  //System.out.println("q :"+q);
		  System.out.println("p :"+p);
		  // find two primes so q>p^2
		  
		 
	  }
	  
	 
	  /** Alice use this function to add the secrets shares of the access structure S^A to her massages **/
	  public static BigInteger[] AddShares(BigInteger[] encryptedVector,BigInteger[] sk) {
		  int N =encryptedVector.length;
		  BigInteger[] newVector = new BigInteger[N];
		  for(int i=0;i<N;i++) {
			  newVector[i]=encryptedVector[i].add( sk[i].multiply(GOT.p));
		  }
		  return newVector;
	  }
	  
	  /** Bob Use this function to recover the shares of the access structure to compute S^B **/
	  public static BigInteger recoverSfromEncryptedVector(BigInteger[] ev,BigInteger[] BobVector,ShamirSecretSharing ss) {
		  	int N=ev.length;
		  	BigInteger sum=BigInteger.ZERO;
			for(int i=0;i<N;i++) {
				if(BobVector[i].equals(BigInteger.ONE)) {
					BigInteger part=ev[i].subtract(ev[i].mod(GOT.p)).divide(GOT.p);
					sum=sum.add(part);
				}	
			}
			BigInteger s= sum.mod(GOT.p); 
			System.out.println(" bob recover s="+s);
			
			return s;
	  }
	  
	  /** bob use this function get the encryptions keys from the server's after the validate S^A=S^B **/
	  public static BigInteger[] get_encryption_keys(int N,int D,GOT_Server[] d_list,BigInteger[] BobVector,ShamirSecretSharing ss) {
		  
		   // Get shares for encryptions keys
		  BigInteger[][] from_servers = new BigInteger[D][N];
		  BigInteger[] result = new BigInteger[N];
			for(int i=0;i<D;i++) {
				BigInteger[] from_server=d_list[i].get_encryption_keys();
				for(int i2=0;i2<N;i2++) {
					from_servers[i][i2]=from_server[i2];
				}
			}
			// Get shares for encryptions keys
			
			// Interpolate the encryption keys 
			for (int i2 = 0; i2 < N;i2++) {
				result[i2]=BigInteger.ZERO;
				if(BobVector[i2].equals(BigInteger.ONE)) {
					BigInteger[][] res_shares=new BigInteger[D][2];
					for(int i=0;i<D;i++) {
						res_shares[i][0]=BigInteger.valueOf(i+1);
						res_shares[i][1]=from_servers[i][i2];
					}
					result[i2]= ss.recoverSecret(res_shares).mod(GOT.p);
					
				}
			}
			// Interpolate the encryption keys
			
			return result;
	  }
	  
	  
	  /** This function define which index's bob choose for testing **/
	  public static BigInteger[] getBobVector(int N,int k) {
		  // change this function int start,end to validate the GOT is working 
		  // permissive group is  [0,k-1]  [k,2k-1]  [2k,3k-1] ...
		  // for example for k=10 [0,9] , [10,19] , [20,29] ... 
		  int start=0;
		  BigInteger[] bv = new BigInteger[N];
		  for(int i=0;i<N;i++) {
			  bv[i]=BigInteger.ZERO;
			  if((i >=start) && (i<=start+k-1)) {
				  bv[i]=BigInteger.ONE;
			  }
			  if(bv[i].equals(BigInteger.ONE)) {
				  System.out.println("bv["+i+"]="+bv[i]);
			  }
			  
		  }
		  return bv;
	  }
	  

	  /** This function was used to test the DVV process in "Multi-Threading" mode **/
	  public static void main(String[] args) {
		// this function test that GOT work , you can change Bob vector to illegal value to validate it
		  
		// Initial vector's and secret sharing scheme
		  int D = 7;
		  int k = 4;  // 4-out-of-7 shares
		  int kk=10;  // For OTK in GOT
		  int N=10000;		  
		  //for(int i2=0;i2<10;i2++) {
			 

			  generate_p_and_q();
			  BigInteger[] encryptionKeys= DSP.random_vector(N);
			  ShamirSecretSharing ss=new ShamirSecretSharing();
			  BigInteger S =  new BigInteger(Setting.fieldSize, GOT.random);  // Secret S
			  System.out.println("S :"+S);
			  System.out.println("SSS.P:"+ss.P());
			  BigInteger[] BobVector =getBobVector(N,kk);
			  BigInteger[] AliceVector= DSP.random_vector(N);
			  
		// Initial vector's and secret sharing scheme
		  
		// Build D GOT server's
		  GOT_Server[] d_list = get_servers_list(D,N,ss,kk); // diffrent validation process
		  for(int i=0;i<D;i++) {			d_list[i].start(); 	}
		// Build D GOT server's
		  
		// Set N zeros for secret share scrambling
			for(int i2=0;i2<N;i2++) {
				for(int i=0;i<D;i++) {				d_list[i].ServerSetZero(i2)  ;	}
				for(int i=0;i<D;i++) {				d_list[i].set_zeros_share(i2);	}
			}
			// Set N zeros for secret share scrambling
		  
		// Alice share S and the encryption keys with server's
		  GOT.share_secretS(S,k,D,ss,(GOT_Server[])d_list,false);
		  GOT.share_encryptionsKeys(encryptionKeys,k,D,ss,(GOT_Server[])d_list);
		// Alice share S and the encryption keys with server's
		  
		// Alice encrypt and use the shares of S to add them to each massage
		  BigInteger[] sk = GOT.generate_S_keys(kk,N, S,ss);
		  BigInteger[] AlicenNewVector = AddShares(encryptedAliceVector(AliceVector,encryptionKeys),sk);
		 // Alice encrypt and use the shares of S to add them to each massage
		  
		  
		  
		// Run GOT using reudction to OTK
		  
		 OTK.koutofN(BobVector, AlicenNewVector, k, D, N, ss, d_list,OTK.innerProductArr(BobVector, AliceVector));
		// Run GOT using reudction to OTK
		  //}
		  
		  
	  }
	  
}
