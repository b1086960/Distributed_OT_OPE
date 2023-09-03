package servers;
import java.math.BigInteger;
import java.util.ArrayList;
import helpers.KTuplesGenerator;
import helpers.ShamirSecretSharing;

/** This is the DVV server that validate bob vector for the OMPE("Oblivious Multivariate Polynomial Evaluation") problem **/
public class OMPE_Server extends DVV_Server {
	
	/** Variables for the DVV process**/
	protected int k;				// k
	protected KTuplesGenerator kt;	// Help class converting tuple into integer and verse versa
	
	/** Initial server **/
	public OMPE_Server(int N, int D, int index, ShamirSecretSharing ss,int k) {
		
		// set values for OMPE DVV
		super(N, D, index, ss);
		this.k=k;
		this.validation_stack=new BigInteger[this.N-this.k-1][this.D];  // need to validate (N-k-1) entries
		this.kt = new KTuplesGenerator(k,N); // helper to convert natural->k-tuple and k-tuple->natural 
		this.kt.set_tuples();
		// set values for OMPE DVV
		
	}
	
	/** this function  over-write the empty DVV in the class "DVV_server" to fit it for OMPE **/
	protected boolean Bob_vector_entries_validation() {
		
		// b(0)=1 always 
		BobVector[0]=BigInteger.ONE; 
		// b(0)=1 always 
		
		//  server set his share  for validate each b(n) in tier L = some d(n) in tier (L-1) * a unit vector
		for(int i=0;i<this.N-this.k-1;i++) {
			
			int point = i+k+1;
			int new_point = -1;
			ArrayList<Integer> ktuple =kt.getTupleByOrder(point); // for natural->k-tuple ktuple in tier L
			ArrayList<Integer> unit_vector = kt.makezerosArray(ktuple.size()); // 
			for(int index=0;index<ktuple.size();index++) {
				Integer value= ktuple.get(index);
				if( value !=0 ) {
					ktuple.set(index, value-1);
					unit_vector.set(index, 1); // find unit vector 
					new_point=kt.getTupleOrder(ktuple); // and new-ktuple tuple tier L-1
					index=ktuple.size()+1;
				}
			}
			
			// validate new-ktuple*unit vector= ktuple by converting them to indexs in b(n) 
			int unit_vector_point=kt.getTupleOrder(unit_vector);
			BigInteger Bn  = BobVector[point].subtract( BobVector[new_point].multiply(BobVector[unit_vector_point]));
			servers_send_shares(i,Bn);
			// validate new-ktuple*unit vector= ktuple by converting them to indexs in b(n) 
			
		}
		//  server set his share  for validate each b(n) in tier L = some d(n) in tier (L-1) * a unit vector
		
		
		// Wait until all shares are ready to validate both conditions 
		while( validation_stack_counter< (this.N-this.k-1)*this.D) {
			this.gotosleep();
		}
		// Wait until all shares are ready to validate both conditions 
		
				
		//  Then validate each b(n) in tier L = some d(n) in tier (L-1) * a unit vector
		for(int i=0;i<this.N-k-1;i++) {
			if(!validate_entry(i,BigInteger.ZERO)) {
				System.out.println("validaition failed: entry "+(this.k+i+1)+" is'nt fit");
				return false;
			}
		}
		//  Then validate each b(n) in tier L = some d(n) in tier (L-1) * a unit vector
				
		// After validation, ready for DSP
		return true;
		// After validation, ready for DSP
	}

	

}
