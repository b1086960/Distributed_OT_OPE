package run;
import helpers.KTuplesGenerator;
import helpers.ShamirSecretSharing;

/** This is the main class which is use for time-testing the project **/
public class Main { 
	
	 
	/** This function is used to create list of CSV files in the "result" folder for time measurements  **/
	/** To create graphs from the result's GOTO main.py **/
	public static void main(String[] args) {
		
		
		// test all protocols along different D values 
		int numberOfTests=1;	// change this value to determine the amount of test held on each value of D/N
		ShamirSecretSharing ss=new ShamirSecretSharing();
		int[] D_arr= { 11 }; //3,5,7,9,11 // possible values of D //3,5,7,9,11
		String[] operation_arr= {"GOT"}; // ,"POT","GOT"// test this protocol's //  "DSP","OTK","POT","GOT","OPE","OMPE"
		int[] N_arr={1000000}; //100,1000,10000,100000,1000000  // N possible values //100,1000,10000,100000,1000000
		// test all protocols along different D values 
		
		
		// test protocols 
		for(int iOpeartion=0;iOpeartion<operation_arr.length;iOpeartion++) { 
			
			// operation name
			String opeartion=operation_arr[iOpeartion];
			
			// test all protocols along different  N values;
			if(opeartion=="OPE" || opeartion=="OMPE") {
				int[] N_arr2={5,10,20,30,40,50};     // for OPE/OMPE
				N_arr=N_arr2;
			}
			// test all protocols along different  N values;
				
			
			if(opeartion=="GOT") {
				GOT.generate_p_and_q();
				System.out.println("P: "+ss.P());
				//ss.reset_P_2_Q(GOT.q); 
			}
			// operation name
			
			 // k is dimension of polynomial in OMPE or k in k-out-of-n OT
			boolean inKsecretsForm = false;
			int k=0;
			if(opeartion=="OTK" || opeartion=="GOT")  { k = 10; inKsecretsForm= true; }
			if(opeartion=="POT") { inKsecretsForm= true;}
			if(opeartion=="OMPE") {
				k = 1;
				for(int i=0;i<N_arr.length;i++) {
					N_arr[i]=KTuplesGenerator.binomialCoeff(N_arr[i],k);
					System.out.println(i+" " +N_arr[i]);
				}
			} 
			if(opeartion=="OPE") { k=0;}
			
			
			//int[] k_arr= {5,10,20,30,40,50};   // To test diffrent k values for OTK get this line out of comment
			
			// for any possible value of D and N
			for(int iD=0;iD<D_arr.length;iD++) {
				for(int iN=0;iN<N_arr.length;iN++) {
					 //for(int ik=0;ik<k_arr.length;ik++) { // To test diffrent k values for OTK get this line out of comment 
						System.out.println("k="+k);
						int N=N_arr[iN];
						int D=D_arr[iD];
						for(int test=1;test<=numberOfTests;test++) {
							System.out.println(opeartion+" N "+ N + " D "+D+" test"+test);
							Measure_times.test_opeartion(N,D,opeartion,ss,k,inKsecretsForm,test);
						}	
					//} // To test diffrent k values for OTK get this line out of comment
				}
			}
			// for any possible value of D and N
			
		}
		// test protocols 
		
		// test all protocols along different D/N values 
				
	}
}
