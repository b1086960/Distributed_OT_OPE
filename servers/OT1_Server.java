package servers;
import java.math.BigInteger;

import helpers.Setting;
import helpers.ShamirSecretSharing;

/** This is the DVV server that validate bob vector for the "1-out-of-N Oblivious Transfer" problem **/
public class OT1_Server extends DVV_Server  {
	
	/** Variables for the DVV process**/
	protected BigInteger max_amount_of_massage;
	protected boolean validation_passed;
	
	
	/** Initial server **/
	public OT1_Server(int N, int D, int index, ShamirSecretSharing ss) {
		
		// set values for OT1 DVV
		super(N, D, index, ss);
		max_amount_of_massage=BigInteger.ONE; 				 // Up to one secret 
		this.validation_stack=new BigInteger[1+N][this.D];  // Need to validate (N+1) entries
		this.zeros_shares_servers=new BigInteger[N][this.D]; 	   // For DVV reshuffling
		this.zeros_shares=new BigInteger[N];
		validation_passed=false;						  // only if DVV is passed
		// set values for OT1 DVV
		
	}

	/** this function  over-write the empty DVV in the class "DVV_Server" to fit it for 1-out-of-N OT **/
	protected boolean Bob_vector_entries_validation() {
		
		// server send his share for validate sum == max_amount_of_massage
		BigInteger sum=BigInteger.ZERO;
		for(int i=0;i<this.N;i++) {
			sum=sum.add(BobVector[i]);
		}
		servers_send_shares(0,sum);
		// server send his share for validate sum == max_amount_of_massage
		
		// server set his share for validate each entry i is 1 or 0
		BigInteger Bn;
		for(int i=0;i<this.N;i++) {
			BigInteger bn1=BobVector[i].subtract(BigInteger.ONE); // b(i)-1
			Bn=BobVector[i].multiply(bn1);// b(i)*(b(i)-1)
			BigInteger scrambledZero = get_zeroes_shares(i);
			servers_send_shares(i+1,Bn.add(scrambledZero)); //scramble Bn and send the scramble share;
		}
		// server set his share for validate each entry i is 1 or 0
		
		// Wait until all scrambled shares are ready to validate both conditions 
		if(!Setting.testing_mode) {
			while( validation_stack_counter< (this.N+1)*this.D) {	this.gotosleep();}
		}
		System.out.println("server "+this.index+" start validation after receving all shares from other server's");
		// Wait until all scrambled shares are ready to validate both conditions 
		
		// Then validate each entry i is 1 or 0
		for(int i=0;i<this.N;i++) {
			if(!validate_entry(i+1,BigInteger.ZERO)) {
				System.out.println("validaition failed: entry "+i+" in Bob Vector isent 0 or 1");
				return false;
			}
		}
		// Then validate each entry i is 1 or 0
		
		// Then validate sum == max_amount_of_massage
		if(!validate_entry(0,max_amount_of_massage)) {
			System.out.println("validaition failed: Bob choose more or less then "+max_amount_of_massage+" massages ");
			return false;
		}
		// Then validate sum == max_amount_of_massage
		
		// After validation, ready for DSP
		validation_passed=true;
		return true;	
	}


	
	
}
