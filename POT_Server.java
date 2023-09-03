package servers;
import java.math.BigInteger;
import helpers.ShamirSecretSharing;

/** This is the DVV server that is used for the POT("Priced Oblivious Transfer") problem **/
public class POT_Server extends OTK_Server {
	
	/** Variables for the POT problem**/
	protected BigInteger[] PricesVector;		// Vector of prices
	protected BigInteger   Bob_treshold_share;	// Bob   T^B
	protected BigInteger Alice_treshold_share;	// Alice T^A
	protected int  count_prices_and_tresholds;	// Count prices+T^B+T^A
	
	/** Initial server **/
	public POT_Server(int N, int D, int index, ShamirSecretSharing ss) {
		super(N, D, index, ss, N);
		this.PricesVector = new BigInteger[N];
		this.validation_stack=new BigInteger[2+N][this.D];
		this.zeros_shares_servers=new BigInteger[N+1][this.D]; 	   // For DVV reshuffling
		count_prices_and_tresholds=0;
	}
	
	/** Bob use This function to set T^B **/
	public void BobSetTreshold(BigInteger treshold) {
		this.Bob_treshold_share= treshold;
		count_prices_and_tresholds++;
	}
	
	/** Alice use This function to set T^A **/
	public void AliceSetTreshold(BigInteger treshold) {
		this.Alice_treshold_share= treshold;
		count_prices_and_tresholds++;
	}
	
	/** Alice use This function to set prices vector **/
	public void receivePriceFromAlice(int index,BigInteger value) {
		// server receive price from Bob
		this.PricesVector[index]=value;
		count_prices_and_tresholds++;
		// server receive price from Bob
	}
	
	/** this function  over-write the empty DVV in the class "DVV_server" to fit it for POT **/
	protected boolean Bob_vector_entries_validation() {
		
		// Wait until all prices and treshold's are set
		while( count_prices_and_tresholds < (2+N)) {
				this.gotosleep();
		}
		// Wait until all prices and treshold's are set		
		
		// Server send his share for validate threshold is same for Alice and Bob
		servers_send_shares(0,Bob_treshold_share.subtract(Alice_treshold_share));
		// Server send his share for validate threshold is same for Alice and Bob
		
		// Server set his share for validate sum of product's == treshold 
		BigInteger sum=BigInteger.ZERO;
		for(int i=0;i<this.N;i++) {
			sum=sum.add(BobVector[i].multiply(PricesVector[i]));
		}
		sum=sum.subtract(Bob_treshold_share);
		servers_send_shares(1,sum);
		// Server set his share for validate sum of product's == treshold 
		
		// Server set his share for validate each entry i is 1 or 0
		BigInteger Bn=BigInteger.ZERO;
		for(int i=0;i<this.N;i++) {
			BigInteger bn1=BobVector[i].subtract(BigInteger.ONE); // b(i)-1
			Bn=BobVector[i].multiply(bn1);// b(i)*(b(i)-1)
			BigInteger scrambledZero = get_zeroes_shares(i);
			servers_send_shares(2+i,Bn.add(scrambledZero)); //scramble Bn and send the scramble share;
		}
		// Server set his share for validate each entry i is 1 or 0
	
		
		// Wait until all shares are ready to validate both conditions 
		while( validation_stack_counter< (this.N+2)*this.D) {
			this.gotosleep();
		}
		System.out.println("server "+this.index+" start validation after receving all shares from other server's");
		// Wait until all shares are ready to validate both conditions 
		
		// Then validate validate threshold is same for Alice and Bob
		if(!validate_entry(0,BigInteger.ZERO)) {
			System.out.println("validaition failed: threshold is not the same for Alice and Bob ");
			return false;
		}
		// Then validate validate threshold is same for Alice and Bob
		
		// Then validate validate sum of product's == treshold 
		if(!validate_entry(1,BigInteger.ZERO)) {
				System.out.println("validaition failed: sum of product's dont fit the threshold ");
				return false;
		}
		// Then validate validate sum of product's == treshold 
		
		// Then validate each entry i is 1 or 0
		for(int i=0;i<this.N;i++) {
			if(!validate_entry(i+2,BigInteger.ZERO)) {
				System.out.println("validaition failed: entry "+i+" in Bob Vector isent 0 or 1");
				return false;
			}
		}
		// Then validate each entry i is 1 or 0
		
		
		// After validation, ready for DSP
		return true;	
	}

}
