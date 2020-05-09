import java.util.ArrayList;
import java.util.List;

public class MaxFeeTxHandler extends TxHandler {
	public MaxFeeTxHandler(UTXOPool utxoPool) {
		super(utxoPool);
		// TODO Auto-generated constructor stub
	}

	/*
	 * returns -1 if the transaction is not valid
	 * returns value of the fee (double >= 0.0 ) if the transaction is valid
	 */
	public double isValidTxFee(Transaction tx) {
        // IMPLEMENT THIS
    	double sumOutputs = 0;
    	double sumInputs = 0;
    	// verify1 - that all outputs are in the current UTXOPool, if at least one is not then
    	// ret is false
    	for (int i = 0; i < tx.numInputs(); i++) { // take all inputs
    		Transaction.Input in = tx.getInput(i); // create temp Input in
    		UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex); // create UTXO based on input Hash and index
    		if (!utxopool.contains(utxo)) { // if at least one utxo not contained in the pool then return false
    			return -1; // -1 is invalid
    		}
    	}
    	
    	//verify2bobmio
    	for (int i = 0; i < tx.numInputs(); i++) {
    		Transaction.Input in1 = tx.getInput(i);
    		UTXO utxo1 = new UTXO(in1.prevTxHash, in1.outputIndex);
    		Transaction.Output out1prev = utxopool.getTxOutput(utxo1);
    		if (!Crypto.verifySignature(out1prev.address, tx.getRawDataToSign(i), in1.signature)) {
    			return -1;
    		}
    		sumInputs+=out1prev.value;
    	}
    	    	    	
    	// verify3 - no UTXO is claimed multiple times (double payment)
    	for (int i = 0; i < tx.numInputs()-1; i++) {
    		for (int j = i+1; j < tx.numInputs(); j++) {
    			UTXO utxo_i = new UTXO(tx.getInput(i).prevTxHash, tx.getInput(i).outputIndex);
    			UTXO utxo_j = new UTXO(tx.getInput(j).prevTxHash, tx.getInput(j).outputIndex);
    			if (utxo_i.compareTo(utxo_j) == 0) {
    				return -1;
    			}
    		}
    	}
	
    	//verify4 - all the output values are non-negative
    	for (int i = 0; i < tx.numOutputs(); i++) {
    		Transaction.Output out2 = tx.getOutput(i);   		
    		if (out2.value < 0) {
    			return -1;
    		}
    		sumOutputs+=out2.value;
    	}
    	    	
    	//verify4 - the sum of the inputs values >= the sum of the output values (due to commision)
    	if (sumInputs < sumOutputs) {
    		return -1;
    	} else {
    		return sumInputs-sumOutputs;
    	}
	}

	@Override
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
    	List<Transaction> tx1 = new ArrayList<>();
    	double MaxFeeSum = 0;
    	// step1, choose only valid Transactions
    	for (Transaction tx :possibleTxs) {
    		if ((this.isValidTxFee(tx)>=0) && (MaxFeeSum+this.isValidTxFee(tx) > MaxFeeSum)) {
    			tx1.add(tx); // add only valid Transactions from the block
    			for (Transaction.Input in : tx.getInputs()) {
    				UTXO utxo_in = new UTXO(in.prevTxHash, in.outputIndex); // create an UTXO for each input of a valid Transaction
    				utxopool.removeUTXO(utxo_in); // remove the UTXO from UTXOPool
    			}
    			int index_out = 0;
    			for (Transaction.Output out : tx.getOutputs()) {
    				UTXO utxo_out = new UTXO(tx.getHash(), index_out); // create UTXO from the Outputs of the valid Transactions
    				index_out++;
    				utxopool.addUTXO(utxo_out, out); // add the UTXO to the UTXOPool
    			}
    		}    			
    	}
    	return tx1.toArray(new Transaction[tx1.size()]);
	}

}
