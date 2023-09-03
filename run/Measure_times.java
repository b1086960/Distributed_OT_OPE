package run;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigInteger;
import helpers.Setting;
import helpers.ShamirSecretSharing;
import servers.DSP_Server;
import servers.GOT_Server;
import servers.POT_Server;

/** This class is used to measure time in all DSP-based protocols **/
public class Measure_times {
	

	/** This function set server-type list according to Bob vector requirements **/
	public static DSP_Server[] get_m_list(int N,int D,String operation,ShamirSecretSharing ss,int k) {
		if(operation=="DSP" ){	return DSP.get_servers_list(D,N,ss.P()); }
		if(operation=="OT1" ){	return OT1.get_servers_list(D,N,ss); }
		if(operation=="OTK" ){	return OTK.get_servers_list(D,N,ss,k); }
		if(operation=="OPE" ){  return OPE.get_servers_list(D, N, ss);  }
		if(operation=="OMPE"){  return OMPE.get_servers_list(D, N, ss,k);  }
		if(operation=="POT" ){  return POT.get_servers_list(D, N, ss);  }
		if(operation=="GOT" ){  return GOT.get_servers_list(D, N, ss,k);  }
		return null;
	}
	
	/** This function set bob random vector according to Bob vector requirements **/
	public static BigInteger[] BobVector(int N,int k,String operation,ShamirSecretSharing ss) {
		if(operation=="DSP" ){	return DSP.random_vector(N); }
		if(operation=="OT1" ){	return OT1.randomUnitVector(N); }
		if(operation=="OTK" ){	return OTK.randomKUnitsVector(N, k);}
		if(operation=="OPE" ){  return OPE.OPE_vector(N,BigInteger.valueOf(1)); } // random_OPE_vector(N); }
		if(operation=="OMPE"){  return OMPE.random_OMPE_vector(k,N,ss,20000000); } // all random factors are mod 2 to make it fast 0/1
		if(operation=="GOT" ){  return GOT.getBobVector(N,k);  }
		return null;
	}
	
	
	/** Help function -print array into csv **/
	public static void print_arr(PrintWriter out,long[] arr,long t0) { 
		String symb="";
    	for(int i2=0;i2<arr.length;i2++) {
    		if(i2>0) {
    			symb=",";
    		}
    		long fixedvalue = arr[i2];
    		if (fixedvalue >0) {
    			fixedvalue=fixedvalue-t0;
    		}
    		out.print(symb+fixedvalue);
    		
    	}
    	out.println("");
	}
	
	
	/** This function input operation type and value of N,D , and perform the corresponding MPC protocol , and output the times into csv file **/
	public static void test_opeartion(int N,int D,String opeartion,ShamirSecretSharing ss,int k,boolean inKsecretsForm,int testnumber) {
		
		// Bob Vector will fit the form of operation and so on the d_list 
		BigInteger[] BobVector;
		
		// for POT
		BigInteger[] pricesVector = null;
		int T = 0;
		// for POT
		
		if(opeartion=="POT") {
			 k=0;
			 T=100;
			 pricesVector=POT.random_prices_vector(N,T);
			 BobVector=POT.random_selection(pricesVector, T);
		}else {
			BobVector=BobVector(N,k,opeartion,ss);
			
			//System.exit(0);
		}
		
		DSP_Server[] d_list =get_m_list(N,D,opeartion,ss,k);
		
		
		// Bob Vector will fit the form of operation and so on the d_list 
		
		// then we start measuring time and reset counter for number of secret sharing operation ( recover secret and split secret )
		
		
		if(opeartion=="POT") {
			// Alice shares prices 
			POT.share_prices(pricesVector,(int) (Math.floor(D/2)+1),D,ss,(POT_Server[])d_list);
			// Alice shares prices
			
			// Alice and bob send treshold to servers 
			POT.share_threshold(BigInteger.valueOf(T),(int) (Math.floor(D/2)+1),D,ss,(POT_Server[])d_list,true);
			POT.share_threshold(BigInteger.valueOf(T),(int) (Math.floor(D/2)+1),D,ss,(POT_Server[]) d_list,false);
			// Alice and bob send treshold to servers
		}
		
		
		
		BigInteger[] AliceVector=DSP.random_vector(N);
		BigInteger[] setInnerInAdvace=null;
		if(opeartion=="GOT") {
			
			BigInteger[] encryptionKeys= DSP.random_vector(N);
			BigInteger S =  new BigInteger(Setting.fieldSize, GOT.random);  // Secret S
			System.out.println("S :"+S +"k :"+k);
			GOT.share_secretS(S,(int) (Math.floor(D/2)+1),D,ss,(GOT_Server[])d_list,false);
			GOT.share_encryptionsKeys(encryptionKeys,(int) (Math.floor(D/2)+1),D,ss,(GOT_Server[])d_list);
			System.out.println("k="+k);
			BigInteger[] sk = GOT.generate_S_keys(k,N, S,ss);
			
			setInnerInAdvace=OTK.innerProductArr(AliceVector,BobVector);
			AliceVector = GOT.AddShares(GOT.encryptedAliceVector(AliceVector,encryptionKeys),sk);
			
		}
		
		long t0=DSP.time();
		
		
		ss.reset_counters();
		long[][] times = DSP.runDSP(N,D,d_list, BobVector,AliceVector,ss,inKsecretsForm, t0,setInnerInAdvace);
		long recover_counter = ss.recover_counter();
	    long split_counter   = ss.split_counter();
		PrintWriter out; // to write to csv
		// then we start measuring time and reset counter for number of secret sharing operation ( recover secret and split secret )

		// writing to csv
		try {
			
			//  write each file by operation folder / d_[D] 
			String kstr="";
			if(k>0) { kstr="_k_"+k; }		// for OMPE OTK wrote k size
			if(opeartion=="POT") { kstr="_T_"+T; }
			String teststr = "_test_"+testnumber;
		    out = new PrintWriter("./result/result_"+opeartion+"_D_"+D+"_N_"+N+kstr+teststr+".csv");
		    //  write each file by operation folder / d_[D]
		    
		    // share of relevant information in csv format
		    //t0=times[D][0];
		    out.println("server start, validation start, validation end,server end ");	    
		    for(int i=0;i<times.length-1;i++) {	    	print_arr(out,times[i],t0);		    }
		    out.println("process start, secret sharing(ss) start, ss end,process end ");
		    print_arr(out,times[times.length-1],t0);
		    out.println(" time stamp , recover ,  split , (secret sharing opeartions count) ");
		    print_arr(out,new long[]{t0,recover_counter,split_counter},0); // save t0 (time stamp)
		    out.println(" secret sharing opeartions avg time , recover ,  split  ");
		    out.println( " , "+(float)(times[0][2]-times[0][1])/recover_counter +","+((float)(times[D][2]-times[D][1])/split_counter));
		    out.println(" secret sharing opeartions avg time per D , recover ,  split  ");
		    out.println( " , "+(float)(times[0][2]-times[0][1])/(D*recover_counter) +","+((float)(times[D][2]-times[D][1])/(D*split_counter)));
		    out.println(" intial time , sharing time, server's time , recovering time   ");
		    // share of relevant information in csv format
		    
		    
		    // compute server's time by = last server end time - first server start time
		    long start_validation=1000000000;
		    long end_validation=0;
		    for(int i=0;i<D;i++) {
		    	long t1=times[i][1]-t0;
		    	long t2=times[i][2]-t0;		
		    	if(t1 < start_validation) {		   start_validation =  t1;   }
		    	if(t2 > end_validation)   {         end_validation  =  t2; }
		    }
		    long validation_time= end_validation-start_validation;
		    out.println( (times[D][1]-t0)+","+(times[D][2]-times[D][1])+","+validation_time+","+( times[D][3]-times[0][3]  ));
		    // compute server's time by = last server end time - first server start time
		    
		    // output number of operations of split and recover 
		    if(DSP.validation_stack_size > 0) {
		        out.println(" number of validations entries , average validation time per entry , average validation time per entry and per D");
		        float validation_time_avg =( (float) validation_time/DSP.validation_stack_size);
			    out.println(DSP.validation_stack_size + ","+ validation_time_avg+","+(validation_time_avg/D)  );
		    }
		    out.close();
		    // output number of operations of split and recover 
		    
		    
		} catch (FileNotFoundException e) {
		    System.err.println("File doesn't exist");
		    e.printStackTrace();
		}
		// writing to csv
		
		
	}
	// test opeartion and output result to csv fil

	
	
	
}
