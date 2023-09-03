package servers;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;

/** This is the DVV server that validate bob vector for the OPE("Oblivious Polynomial Evaluation") problem **/
public class OPE_Server  extends DVV_Server{

	/** Initial server **/
	public OPE_Server(int N, int D, int index, ShamirSecretSharing ss) {
		
		// set values for OPE DVV
		super(N, D, index, ss);
		this.validation_stack=new BigInteger[this.N-2][this.D]; // need to validate (N-2) entries
		// set values for OPE DVV
			 
		
	}
	
	/** this function  over-write the empty DVV in the class "DVV_server" to fit it for OPE **/
	protected boolean Bob_vector_entries_validation() {
		
		// b(0)=1 always
		BobVector[0]=BigInteger.ONE; 
		// b(0)=1 always
		
		// send massages for validate b(n)=b(n-1) * b(2) 
		for(int i=2;i<this.N;i++) {
			BigInteger Bn  = BobVector[i].subtract(BobVector[i-1].multiply(BobVector[1]));
			servers_send_shares(i-2,Bn);
		}
	   // send massages for validate b(n)=b(n-1) * b(2)
		
		// when ready validate condition 
		while( validation_stack_counter< (this.N-2)*this.D) {
			this.gotosleep();
		}
		// when ready validate condition
				
		// validate each entry b(n)=b(n-1) * b(2)
		for(int i=2;i<this.N;i++) {
			if(!validate_entry(i-2,BigInteger.ZERO)) {
				System.out.println("validaition failed: entry "+i+" in Bob Vector isent b(n)=b(n-1)*b(2)");
				return false;
			}
		}
		// validate each entry b(n)=b(n-1) * b(2)
				
		// After validation, ready for DSP
		return true;
		// After validation, ready for DSP
	}

}
