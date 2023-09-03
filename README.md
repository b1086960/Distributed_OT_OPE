This project includes several classes that can be used to implements several MPC protocols, including: 1-out-of-N Oblivious Transfer (OT1), k-out-of-N Oblivious Transfer (OTK), Priced Oblivious Transfer (POT), Generalized Oblivious Transfer (GOT), Oblivious Polynomial Evaluation (OPE), and Oblivious Multivariate Polynomial Evaluation (OMPE).

All protocols are distributed and are executed by the sender (Alice), the receiver (Bob), and a set of external servers.

The project can be activated in one of two modes: "Testing mode" and "Multi-threading mode".
The testing mode is used mainly for measuring runtimes for the servers.
The multi-threading mode executes the protocols in full mode, where each server is run as a different thread.
To switch between the two modes, it is necessary to change in the file helpers/setting the variable "testing_mode" from true to false or vice-versa.

Guidelines for measuring time in "Testing mode":
To measure the runtime of one (or more) of the protocols, as a function of N and D, go to run/main that includes the main function.
Then, mark in the array "operation_arr" the protocols that you wish to run, where the options are "DSP","OT1","OTK","POT","GOT","OPE","OMPE".
The array "N_arr" has to be filled with the list of N values on which you desire to run the experiment. 
Similalry, the array "D_arr" has to be filled with the list of desired D values.
Finally, the variable "numberOfTests" is the number of repetitions for each setting of N and D (as explained in the paper, we run each experiment several times and report the average runtime).
The results are exported to the folder results, where each experiment's outputs are exported to a separate file, with a name that indicates the name of the protocol, D, N, k (when relevant), and the number of experiment.  

Guidelines for running an MPC protocol in "Multi-Threading" mode:
Code examples in "Multi-Threading" can be found at folder "run" under file with the protocol name.
For example for OPE in folder "run" under file "OPE" ( function "main") you can see example of how to run OPE.
You can change Bob's vector to see how the DVV process works. For example: in the file run/OPE change Bob The legal vector value
{1,10,100,1000,10000,100000} to illegal one. If you select an illegal vector, for example: {1,10,101,1000,10000,100000}, you will get an error from the DVV process: "validation failed: entry 2 in Bob Vector isent b(n)=b(n-1)*b(2)".

How to run the code separately for Alice and Bob:
Since this code simulates each of the protocols it does not separate Alice and Bob Code, and it also add validation to see the protocols works. 
Here are instructions on how to run each protocol separately for Alice and Bob:
(1)DSP: Servers: "DSP_Server", Alice use the "share_vector" ("DSP" file, folder "run") to share her vector with the servers, Bob use "share_vector" to share his vector. and "recoverSecretFromservers" ("DSP" file, folder "run") to recover the inner product result.
(2)OT1: Servers: "OT1_Server", no change for alice and bob.
(3)OTK: Servers: "OTK_Server", no change for Alice, Bob use "recoverSecretFromkservers" ("OTK" file, folder "run") instead of "recoverSecretFromservers" to recover k secrets.
(4)POT: Servers: "POT_Server", Alice use "share_vector" for her vector, then "share_prices" ("POT" file, folder "run") for her prices, 
then "share_treshold" ("POT" file, folder "run") for her treshold. Bob use "share_vector" for his vector, then "share_treshold" for his treshold, then "recoverSecretFromkservers" to recover his massages.
(5)GOT  Servers: "GOT_Server", Alice use "generate_p_and_q" to generate q,p. Then she pick random number S^B (line 210, "GOT" file, folder "run"). Then she share S^A with the servers using "share_secretS" ("GOT" file, folder "run"), then she create shares of S^A according to her access structure using the function "generate_S_keys" ("GOT" file, folder "run"). Lastly she use the functions "encryptedAliceVector" and "AddShares" ("GOT" file, folder "run") to encrypted her vector and add the shares of S^A according to the access structure, and share the result using "share_vector". Bob use "share_vector" for his vector. and then compute the inner product result using "recoverSecretFromkservers". Bob then use "recoverSfromEncryptedVector" ("GOT" file, folder "run") to recover S^B. Bob use "share_secretS" to share S^B with the servers, and "pre_encryption_keys" ("GOT_Server" file, "servers" folder) to tell them to check if S^B==S^A. If S^B==S^A , Bob can get the encryptions keys using "get_encryption_keys" ("GOT" file, folder "run"), and decrypt the result by subtractions (example: line 131, "OTK" file, "run" folder).
(6)OPE  Servers: "OPE_Server",   same as "OT1" (different DVV process).
(7)OMPE Servers: "OMPE_Server",  same as "OT1" (different DVV process).			  
				  







