package helpers;
import java.math.BigInteger;

/** This is Interface for all server's to show what each server do **/
public interface Server {
	public void receiveFromBob(  int index, BigInteger value); // The server receive value at index from Bob
	public void receiveFromAlice(int index, BigInteger value); // The server receive value at index from Alice
	public boolean    validate() ; // The server's use VVD validation to validate Bob or Alice vectors before compute DSP
	public BigInteger getResult(); // The server return his DSP share after the validation
}
