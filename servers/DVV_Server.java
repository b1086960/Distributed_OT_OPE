package servers;
import java.math.BigInteger;

import helpers.Setting;
import helpers.ShamirSecretSharing;
import run.DSP;

/** TO create your own DVV server use "extends DVV_Server" for your class **/
public class DVV_Server  extends DSP_Server{

		/** Variables for DVV validation process**/
		protected int index;					 // server index
		protected int validation_stack_counter;	 // number of entries that the server validate
		protected ShamirSecretSharing ss;		 // shamir secret sharing scheme 
		private DVV_Server[] m_list;			 // list of other server's
		protected boolean server_finish_DVV;
		
		protected int add_zeros_counter; 		// count how many zeros shares you add
		protected BigInteger[][] zeros_shares_servers; 	// servers use there own zeros shares by reshuffle them common zeros shares
		protected BigInteger[] zeros_shares;	// this shared use for equations that need to be hide
		
		/** Initial server **/
		public DVV_Server(int N, int D,int index,ShamirSecretSharing ss) {
			super(N, D,ss.P());
			this.index =index;
			this.ss=ss;
			this.m_list=null;
			this.add_zeros_counter=0;
			this.validation_stack_counter=0; 
			if(Setting.testing_mode) { this.validation_stack_counter=(this.N*2)*this.D;} 
			this.server_finish_DVV=false;
		}
		
		/** This function set the server's list **/
		public void set_servers_list(DVV_Server[] d_list) {
			this.m_list=d_list; 
		}
		
		/** This function is used to receive share of equation from other server's for the DVV process **/
		private void server_recive_value(int from,int validation_stack_index,BigInteger value) {
			this.validation_stack[validation_stack_index][from]=value;
			synchronized(this){ this.validation_stack_counter++; } // To sync the number of massage received	
		}
		
		/** This function is used to send share of equation from other server's for the DVV process **/
		protected void servers_send_shares(int stack_validation_index, BigInteger my_value) {
			
			// Each server first set his share 
			this.validation_stack[stack_validation_index][this.index]=my_value;
			synchronized(this){ this.validation_stack_counter++; } // To sync the number of massage received
			// Each server first set his share 
			
			// Then send his share to the other server's
			for(int i=0;i<this.D;i++) {
				if(i!=this.index) {
					this.m_list[i].server_recive_value(this.index,stack_validation_index,my_value);
				}
			}
			// Then send his share to the other server's
			
		}
		
		//** This function is used to validate single equation in DVV process **/
		protected boolean validate_entry(int entry_index, BigInteger validation_value) {
			
			// After the server receive D shares, he use them to recover entry at entry_index 
			BigInteger[][] res_shares=new BigInteger[this.D][2];
			for(int i=0;i<this.D;i++) {
				res_shares[i][0]=BigInteger.valueOf(i+1);
				if(Setting.testing_mode) { res_shares[i][1]=  BigInteger.ZERO; }
				else {res_shares[i][1]=  this.validation_stack[entry_index][i]; }
				 //BigInteger.ZERO; // To activated "Multi-Threading" mode change this value to this.validation_stack[entry_index][i];
			}
			BigInteger recover_value=ss.recoverSecret(res_shares);
			if(Setting.testing_mode) {
				return true;
			}
			 boolean istrue =(recover_value.equals(validation_value)); //  To activated "Multi-Threading" remove the comment mark
			return istrue; // true; 												//  To activated "Multi-Threading" change this line to "return istrue;" 
			// After the server receive D shares, he use them to recover entry at entry_index
			
		}
		
		//** By over-writing this fucntion you can create custom made DVV process  **/
		protected boolean Bob_vector_entries_validation() {
			return true;
		}
		
		@Override
		/** this function stop the process if the DVV fail, and start it when the vectors are ready  **/
		public boolean validate() {
			
			// Wait until you have the vector's and the server's list
			while(!super.validate() | this.m_list==null) {
				this.gotosleep();
			}
			// Wait until you have the vector's and the server's list
			
			
			// Then use the DVV process to validate Bob vector
			this.server_start_validation = DSP.time();
			if (! Bob_vector_entries_validation()) {
				System.out.println("entries validation fail");
	            System.exit(0);
			}
			this.server_end_validation =  DSP.time();
			this.server_finish_DVV=true;
			// Then use the DVV process to validate Bob vector
			
			// After validation, ready for DSP
			return true;
			// After validation, ready for DSP
			
		}
		
		/** Secure creation of shares of n-out-of-n zero number **/
		
		/** This function return the share the server hold for zero number [index] **/
		protected BigInteger get_zeroes_shares(int index) {
			if(Setting.testing_mode) {
				return BigInteger.ZERO;
			}
			return zeros_shares[index];
		}
		
		/** After all servers set zeros at index [index] , the server use this function to create the share **/
		public void set_zeros_share(int index) {
			BigInteger sum = BigInteger.ZERO;
			for(int i=0;i<D;i++) {
				sum=sum.add(this.zeros_shares_servers[index][i]);
			}
			zeros_shares[index]=sum.mod(ss.P());
		}
		
		/** Each server use this function to send shares of zero number **/
		public void ServerSetZero(int zeroes_index) {
			// reshuffel my value
			BigInteger[][] shares = ss.splitSecret(BigInteger.ZERO,this.D, this.D);
			this.setZero(shares[this.index][1], zeroes_index, this.index);
			for(int i2=0;i2<D;i2++) {
				if(i2!=this.index) {
					this.m_list[i2].setZero(shares[i2][1], zeroes_index, this.index);
				}
			}
			// reshuffel my value
		}
		
		/** Each server use this function to set his shares of zero number **/
		protected void setZero(BigInteger value,int zeroes_index,int server_index) {
			this.zeros_shares_servers[zeroes_index][server_index]=value; 
			synchronized(this){ add_zeros_counter++; };//
		}
	
		/** Secure creation of shares of n-out-of-n zero number **/

}
