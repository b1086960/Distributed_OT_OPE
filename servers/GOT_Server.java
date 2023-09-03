package servers;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;

/** This is the DVV server that is used for the GOT("General Oblivious Transfer") problem **/
public class GOT_Server extends OTK_Server{
	
	/** Variables for the GOT problem**/
	protected BigInteger[] encryptionKeys;	// Array of encryptions keys
	protected BigInteger AliceSecretS;		// S^A
	protected BigInteger BobSecretS;		// S^B
	protected int countSandEncryptionkeys;	// Count encryptionKeys+S^A+S^B 

	/** Initial server **/
	public GOT_Server(int N, int D, int index, ShamirSecretSharing ss, int k) {
		super(N, D, index, ss, k);
		this.encryptionKeys=new BigInteger[N];
		this.validation_stack=new BigInteger[N+2][this.D];
		synchronized(this){ countSandEncryptionkeys=0; }
	}
	
	/** Alice use this function to set the encryption keys shares **/
	public void AliceSetEncryptionKey(int index,BigInteger key) {
		encryptionKeys[index]=key;
		synchronized(this){ countSandEncryptionkeys++;}
	}
	
	/** Alice use this function to set S^A **/
	public void AliceSetSecretS(BigInteger s) {
		AliceSecretS=s;
		synchronized(this){ countSandEncryptionkeys++;}
	}
	
	/** Bob use this function to set S^B  **/
	public void BobSetSecretS(BigInteger s) {
		BobSecretS=s;
		synchronized(this){countSandEncryptionkeys++;}
	}
	
	/** server's compute the equation S^A-S^B and compared it to 0  **/
	public void pre_encryption_keys() {
		// send massages for validate s_b==s_a
		while((AliceSecretS==null) || (BobSecretS==null)) {
			this.gotosleep();
		}
		servers_send_shares(this.N+1,AliceSecretS.subtract(BobSecretS));
		System.out.println(" server "+this.index+" send  share's in s_b-s_a for validation");
		// send massages for validate s_b==s_a
	}

	/** If pre_encryption_keys work , send encryption keys **/
	public BigInteger[] get_encryption_keys() {
		
		// wait for s_a, s_b and encryption keys to be ready
		while( countSandEncryptionkeys< (this.N+2)) {	this.gotosleep(); } 
		// wait for s_a, s_b and encryption keys to be ready
		
		// don't return keys unless previous N+1 conditions are validate in the OTK process
		while(!this.finish  || !server_finish_DVV) {		this.gotosleep();		}
		// don't return keys unless previous N+1 conditions are validate in the OTK process
		
		
		// encryption keys and both s_b and s_a are ready,  when ready validate condition
		while( validation_stack_counter< (this.N+2)*this.D) { this.gotosleep();  }
		// encryption keys and both s_b and s_a are ready,  when ready validate condition
		
		// validate s_b==s_a
		if(!validate_entry(N+1,BigInteger.ZERO)) {
			System.out.println("validaition failed: s_a and s_b are not equal");
			System.exit(0);
		}
		// validate s_b==s_a
		
		// validation complete return encryption keys 
		System.out.println(" after validate s_a == s_b medaitor "+this.index+" send Bob his encryption keys shares  ");
		return encryptionKeys;
		
	}
}
