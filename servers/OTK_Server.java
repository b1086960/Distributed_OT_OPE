package servers;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;

/** This is the DVV mediator that validate bob vector for the "k-out-of-N Oblivious Transfer" problem **/
public class OTK_Server extends OT1_Server{
	private BigInteger[] res_arr;  // Result is compute in cofficent's array instead of single result
	private boolean finishkDSP;

	/** Initial Mediator **/
	public OTK_Server(int N, int D, int index, ShamirSecretSharing ss,int k) {
		super(N, D, index, ss);
		this.max_amount_of_massage=BigInteger.valueOf(k); // Up to k secrets 
		this.finishkDSP=false;
	}
	
	/** This function overwrite "run_DSP" for cofficent's array form**/
	protected void run_DSP() {	
		// After validation compute each coefficient shares separately
		this.res_arr=new BigInteger[N];
		for(int i=0;i < this.N;i++) {
			this.res_arr[i]=this.BobVector[i].multiply(AliceVector[i]);
		}
		this.finishkDSP=true;
		// After validation compute each coefficient shares separately	
	}
	
	/** This function is used to get the result in cofficent's array form**/
	public BigInteger[] getkResult() {
		// After validation result is the coefficient shares separately
		while(!this.finish  || !server_finish_DVV || !finishkDSP) {		this.gotosleep();		}
		return this.res_arr;
		// After validation result is the coefficient shares separately	
	}
	
}
