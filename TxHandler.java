import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TxHandler {
	private UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is {@code utxoPool}. This should make a copy of utxoPool
	 * by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);
	}

	/**
	 * @return true if: (1) all outputs claimed by {@code tx} are in the current
	 *         UTXO pool, (2) the signatures on each input of {@code tx} are valid,
	 *         (3) no UTXO is claimed multiple times by {@code tx}, (4) all of
	 *         {@code tx}s output values are non-negative, and (5) the sum of
	 *         {@code tx}s input values is greater than or equal to the sum of its
	 *         output values; and false otherwise. //Should the input value and
	 *         output value be equal? Otherwise the ledger will become unbalanced.
	 */
	public boolean isValidTx(Transaction tx) {
		Set<UTXO> assignedUTXO = new HashSet<UTXO>();
		double inSum = 0;
		double outSum = 0;

		List<Transaction.Input> ins = tx.getInputs();
		for (int i = 0; i < ins.size(); i++) {
			Transaction.Input in = ins.get(i);
			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
			Transaction.Output out = utxoPool.getTxOutput(utxo);
			
			/*Verifying is consumed coin available */
			if (!CoinAvailable(in)) return false;
			/*Verifying signature of consumed coin */
			if (!verifySignature(tx, i, in)) return false;
			/*verifying is coin consumed multiple time*/
			if (ConsumedMultipleTimes(assignedUTXO, in)) return false;
			inSum += out.value;

		}

		List<Transaction.Output> outs = tx.getOutputs();
		for (int i = 0; i < outs.size(); i++) {
			Transaction.Output out = outs.get(i);
			if (out.value < 0) return false;

			outSum += out.value;
		}

		// The difference between inputSum and outputSum is the transaction fee
		if (outSum > inSum) return false;
		

		return true;
	}

	private boolean ConsumedMultipleTimes(Set<UTXO> assignedUTXO, Transaction.Input in) {
		return !assignedUTXO.add(new UTXO(in.prevTxHash, in.outputIndex));
	}

	private boolean verifySignature(Transaction transaction, int i, Transaction.Input in) {
		UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
		Transaction.Output out = utxoPool.getTxOutput(utxo);
		RSAKey xx = out.address;
		return xx.verifySignature(transaction.getRawDataToSign(i), in.signature);
	}

	private boolean CoinAvailable(Transaction.Input in) {
		UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
		return utxoPool.contains(utxo);
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions,
	 * checking each transaction for correctness, returning a mutually valid array
	 * of accepted transactions, and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		List<Transaction> validatedTx = new ArrayList<Transaction>();
		for (int i = 0; i < possibleTxs.length; i++) {
			Transaction tx = possibleTxs[i];
			if (isValidTx(tx)) {
				validatedTx.add(tx);

				removeAlreadyConsumedCoinsFromPool(tx);
				addAlreadyGeneratedCoinsToPool(tx);
			}
		}

		Transaction[] result = new Transaction[validatedTx.size()];
		validatedTx.toArray(result);
		return result;
	}

	private void addAlreadyGeneratedCoinsToPool(Transaction transaction) {
		List<Transaction.Output> outs = transaction.getOutputs();
		for (int j = 0; j < outs.size(); j++) {
			Transaction.Output output = outs.get(j);
			UTXO utxo = new UTXO(transaction.getHash(), j);
			utxoPool.addUTXO(utxo, output);
		}
	}

	private void removeAlreadyConsumedCoinsFromPool(Transaction transaction) {
		List<Transaction.Input> ins = transaction.getInputs();
		for (int j = 0; j < ins.size(); j++) {
			Transaction.Input in = ins.get(j);
			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
			utxoPool.removeUTXO(utxo);
		}
	}

}
