package helpers;
import java.math.BigInteger;
import java.security.SecureRandom;

/** This is class use for the shamir secret sharing scheme **/
public class ShamirSecretSharing {

	// values for ShamirSecretSharing
	private BigInteger P; 			// factor P = random prime number 
	private long   split_counter=0; // for analysis of run-time
	private long   recover_counter=0; // for analysis of run-time
	// values for ShamirSecretSharing
	
	public void reset_P_2_Q(BigInteger q) { /** This function is used by GOT p,q **/
		this.P=q;
	}
	
	
	/** this function return the field size P*/
	public BigInteger P() {
		return P;
	}
	
	/** this function is used for time analysis for the testing mode P*/
	public long split_counter() {
		return split_counter;
	}
	
	/** this function is used for time analysis for the testing mode P*/
	public long recover_counter() {
		return recover_counter;
	}
	
	/** this function is used for time analysis for the testing mode P*/
	public void reset_counters() {
		// reset values 
		split_counter=0;
		recover_counter=0;
		// reset values 
	}
	
	
	
	/** SSS set P */
	public ShamirSecretSharing() {
		
		// P is initialized 
        this.P = BigInteger.probablePrime(Setting.P_size,  new SecureRandom()); // need to be bigger then max value * N which can be very big
        // P is initialized 
        
	}
	
	/** This function reset the prime p **/

	/** This function is used by Alice/Bob/mediator's to split secret into k-out-of-n shrae's */
    public BigInteger[][] splitSecret(BigInteger secret, int k, int n) {
    	
    	//count split counters 
    	synchronized(ShamirSecretSharing.class){	split_counter++;   }
    	//count split counters 
    	
    	// The polynomial coefficients are generated randomly
    	SecureRandom random = new SecureRandom();
        BigInteger[] coefficients = new BigInteger[k];
        coefficients[0] = secret;
        for (int i = 1; i < k; i++) {
            coefficients[i] = new BigInteger(Setting.fieldSize, random); //secret.bitLength()
        }
        // The polynomial coefficients are generated randomly

        // The secret shares are generated based on the polynomial coefficients 
        BigInteger[][] shares = new BigInteger[n][2];
        for (int i = 0; i < n; i++) {
            BigInteger x = BigInteger.valueOf(i + 1);
            BigInteger y = BigInteger.ZERO;
            for (int j = 0; j < k; j++) {
                y = y.add(coefficients[j].multiply(x.pow(j))).mod(this.P);
            }
            
            shares[i][0] = x;
            shares[i][1] = y;
        }
        return shares;
        // The secret shares are generated based on the polynomial coefficients 
        
    }

    /** This function is used by Alice/Bob/mediator's to recover secret from k-out-of-n shrae's */
    public BigInteger recoverSecret(BigInteger[][] shares) {
    	
    	//count recover counters 
    	 synchronized(ShamirSecretSharing.class){	recover_counter++;   }
    	//count recover counters 
    	 
    	// Lagrange interpolation is in use to recover the secret from the shares
        BigInteger secret = BigInteger.ZERO;
        BigInteger prime = this.P;
        for (int i = 0; i < shares.length; i++) {
            BigInteger num = BigInteger.ONE;
            BigInteger den = BigInteger.ONE;
            for (int j = 0; j < shares.length; j++) {
                if (i != j) {
                    num = num.multiply(shares[j][0].negate()).mod(prime);
                    den = den.multiply(shares[i][0].subtract(shares[j][0])).mod(prime);
                }
            }
            BigInteger lagrange = num.multiply(den.modInverse(prime)).mod(prime);
            secret = secret.add(shares[i][1].multiply(lagrange)).mod(prime);
        }
        return secret;
        // Lagrange interpolation is in use to recover the secret from the shares
        
    }
}