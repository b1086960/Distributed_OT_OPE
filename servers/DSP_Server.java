package servers;
import java.math.BigInteger;
import helpers.Server;
import run.DSP;

/** This version is used to measured time and run on single CPU, it use fake DVV PROCESS to simulated real time**/
public class DSP_Server extends Thread implements Server   {  // 
	
	/** Variables for Alice and Bob vector's**/
	protected BigInteger[] AliceVector;				// Alice vector
	protected BigInteger[] BobVector;				// Bob   vector
	protected int N;								// size of vectors 
	private int BobCounter;							// Number of entries receive from Bob
	protected int AliceCounter;						// Number of entries receive from Alice
	protected int D;								// number of servers 
	protected BigInteger res;						// Inner-product result
	protected boolean finish;						// server is finish
	protected BigInteger P;							// Size of the field
	protected BigInteger[][] validation_stack=null; // For the VDD process
	
	
	/**Get validation stack size **/ 
	public int validation_stack_size() {
		if(validation_stack==null) {	return 0;	}
		return validation_stack.length;
	}
	
	/** Times Variables -- For time measurements **/
	protected   long server_start; 				// server start time
	protected long server_start_validation;	// server start DVV process
	protected long server_end_validation;		// server end DVV process
	protected long server_end;				// server end time
	
	/** return Times Variables in Array **/
	public long[] get_times() {
		long[] times = new long[4];
		times[0]=server_start;
		times[1]=server_start_validation;
		times[2]=server_end_validation;
		times[3]=server_end;
		return times;
	}
	
	
	/** for waiting **/
	protected void gotosleep() {
		// Do nothing , wait
		try {
			Thread.sleep(1);
        } catch (InterruptedException e) {
            System.err.println("Thread Interrupted");
        }  
		// Do nothing , wait	
	}
	
	/** Initial server **/
	public DSP_Server(int N,int D,BigInteger P) {
		this.BobCounter=0;
		this.AliceCounter=0;
		this.N=N;
		this.D=D;
		this.BobVector=new BigInteger[N];
		this.AliceVector=new BigInteger[N];
		this.res=BigInteger.ZERO;
		this.finish=false;
		this.P=P;		
	}
	
	
	@Override
	/** This function validate the vector of Alice and Bob are ready, this function run before the DVV **/
	public boolean validate() {
		// DSP is over when vector receive are fill with all value's
		if ((this.BobCounter == this.N)&(this.AliceCounter ==this.N)){
			this.server_start_validation = DSP.time();
			this.server_end_validation =  DSP.time();
			return true;
		}else {
			return false;
		}
		// DSP is over when vector receive are fill with all value's
	}

	@Override
	/** This function update BoB vector after receiving entry at index [index] From Bob **/
	public void receiveFromBob(int index,BigInteger value) {
		this.BobCounter++;
		this.BobVector[index]=value;
	}

	@Override
	/** This function update Alice vector after receiving entry at index [index] From Alice **/
	public void receiveFromAlice(int index,BigInteger value) {
		this.AliceCounter++;
		this.AliceVector[index]=value;
	}
	
	
	/** This function do inner product between Alice and Bob vectors **/
	protected void run_DSP() {
		// Simple Inner product between BigIntegers vectors
		BigInteger res = BigInteger.ZERO;
		for(int i=0;i < this.N;i++) {
			BigInteger addtores=this.BobVector[i].multiply(AliceVector[i]);
			res=res.add(addtores);
		}
		this.res=res.mod(P);
		System.out.println(" The server result is "+this.res);
		// Simple Inner product between BigIntegers vectors
	}
	
	
	/** This function is active when the server is active until he finish the DVV and inner product **/
	public void run() {
		this.run_unsync();
	}
	
	/** this server is used in testing mode **/
	public void run_unsync() {
				// set time start
				this.server_start = DSP.time();
				// set time start
				
				// Wait for validation 
				while(!validate()) { 	this.gotosleep();	} // testing mode
				// Wait for validation
				
				// Then compute the DSP result 
				this.run_DSP();
				this.finish=true;
				// Then compute the DSP result 
				
				// set time end 
				this.server_end = DSP.time(); 
				// set time end 
	}
	
	@Override
	/** This function return the result when the server is finish **/
	public BigInteger getResult() {
		
		// If validation is Over , return the result 
		while(!this.finish) {		this.gotosleep();		}
		return this.res;
		// If validation is Over , return the result 
		
	}


	

	

	

}
