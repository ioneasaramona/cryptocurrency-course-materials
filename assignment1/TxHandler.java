public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	private UTXOPool utxopool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	this.utxopool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	boolean ret = true;
    	double sumOutputs = 0;
    	double sumInputs = 0;
    	// verify1 - that all outputs are in the current UTXOPool, if at least one is not then
    	// ret is false
    	for (int i = 0; i < tx.numInputs(); i++) { // take all inputs
    		Transaction.Input in = tx.getInput(i); // create temp Input
    		UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex); // create UTXO based on input Hash and index
    		if (!utxopool.contains(utxo)) { // if at least one utxo not contained in the pool then return false
    			return false;
    		}
    	}
    	
    	// verify2 - signatures on each inputs of the transaction are valid
    	for (int i = 0; i < tx.numInputs(); i++) {
    		Transaction.Input in2 = tx.getInput(i);   		
    		if (!Crypto.verifySignature(tx.getOutput(i).address, tx.getRawDataToSign(i), in2.signature)) {
    			return false;
    		};
    		
    	}
    	    	
    	// verify3 - no UTXO is claimed multiple times (double payment)
    	    	
    	//verify4 - all the output values are non-negative
    	for (int i = 0; i < tx.numOutputs(); i++) {
    		Transaction.Output out2 = tx.getOutput(i);   		
    		if (out2.value < 0) {
    			return false;
    		}
    		sumOutputs+=out2.value;
    	}
    	
    	
    	//verify4 - the sum of the inputs values >= the sum of the output values (due to commision)
    	return ret;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	Transaction[] retTxs = possibleTxs;
    	return retTxs;
    }

}
