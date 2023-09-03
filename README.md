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

Guidelines for running an MPC protocol in "Multi-Threading" mode: code examples for each protocol can be found in the folder "run" under the file with the protocol's name. E.g., the code example for OPE is given in run/OPE. It is possible to modify there Bob's input vector in order to see how the DVV process works. The vector that is currently specified there is a legal vector --- (1,10,100,1000,10000,100000); one can change it to an illegal vector by changing one of the entries, and then see how the DVV process returns an error message such as "validation failed: entry 2 in Bob Vector isent b(n)=b(n-1)*b(2)".

Next, we specify for each protocol the class of servers that should be run, and the functions that Alice and Bob run;

(1) DSP: "DSP_Server". Alice and Bob run "share_vector" (run/DSP) to share their vectors with the servers. In addition, Bob runs "recoverSecretFromservers" (run/DSP) to recover the inner product.

(2) OT1: "OT1_Server". No change for Alice and Bob (see DSP above).

(3) OTK: "OTK_Server". No change for Alice. Bob uses "recoverSecretFromkservers" (run/OTK), instead of "recoverSecretFromservers", to recover k secrets.

(4) POT: "POT_Server". Alice runs "share_vector" (run/DSP) to share her vector, "share_prices" (run/POT) for her prices, and "share_threshold" (run/POT) to share her threshold. Bob runs "share_vector" (run/DSP) for his vector, then "share_threshold" for his threshold, and "recoverSecretFromkservers" (run/OTK) to recover his massages.

(5) GOT: "GOT_Server". Alice runs "generate_p_and_q" to generate the two primes p and q.  She runs "share_secretS" (run/GOT) to share S^A. Then she creates shares of S^A according to her access structure using the function "generate_S_keys" (run/GOT). Then, she runs the functions "encryptedAliceVector" and "AddShares" to mask her vector and add the shares of S^A according to the access structure. Lastly, she shares the result using "share_vector" (run/DSP). Bob runs "share_vector" (run/DSP) for his vector. and then computes the inner product result using "recoverSecretFromkservers" (run/OTK). Subsequently, Bob runs "recoverSfromEncryptedVector" (run/GOT) to recover S^B. Bob uses "share_secretS" to share S^B with the servers, and "pre_encryption_keys" (servers/GOT_Server) to tell them to check whether S^B==S^A. If S^B==S^A , Bob can get the encryptions keys using "get_encryption_keys" (run/GOT), and decrypt the result.

(6) OPE: "OPE_Server", Alice and Bob as in "OT1" (the only difference is in the DVV process).

(7) OMPE: "OMPE_Server", Alice and Bob as in "OT1" (the only difference is in the DVV process).
