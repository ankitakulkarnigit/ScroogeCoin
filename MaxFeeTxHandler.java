import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxFeeTxHandler {
	private UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is {@code utxoPool}. This should make a copy of utxoPool
	 * by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public MaxFeeTxHandler(UTXOPool utxoPool) {
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
			Transaction.Output utxoMatchedOutput = utxoPool.getTxOutput(utxo);
			
			/*Verifying is consumed coin available */
			if (!CoinAvailable(in)) return false;
			/*Verifying signature of consumed coin */
			if (!verifySignature(tx, i, in)) return false;
			/*verifying is coin consumed multiple time*/
			if (ConsumedMultipleTimes(assignedUTXO, in)) return false;
			inSum += utxoMatchedOutput.value;

		}

		List<Transaction.Output> outs = tx.getOutputs();
		for (int i = 0; i < outs.size(); i++) {
			Transaction.Output out = outs.get(i);
			if (out.value < 0) return false;
			outSum += out.value;
		}
		if (outSum > inSum) return false;
		return true;
	}
	
	private boolean ConsumedMultipleTimes(Set<UTXO> assignedUTXO, Transaction.Input in) {
		return !assignedUTXO.add(new UTXO(in.prevTxHash, in.outputIndex));
	}

	private boolean verifySignature(Transaction transaction, int i, Transaction.Input in) {
		UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
		Transaction.Output out = utxoPool.getTxOutput(ut);
		RSAKey xx = out.address;
		return xx.verifySignature(transaction.getRawDataToSign(i), in.signature);
	}

	private boolean CoinAvailable(Transaction.Input in) {
		UTXO ut = new UTXO(in.prevTxHash, in.outputIndex);
		return utxoPool.contains(ut);
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions,
	 * checking each transaction for correctness, returning a mutually valid array
	 * of accepted transactions, and updating the current UTXO pool as appropriate.
	 * 
	 * Sort the accepted transactions by fee
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		List<SortTx> validatedTx = new ArrayList<SortTx>();
		for (Transaction tx : possibleTxs) {
			if (isValidTx(tx)) {
				SortTx SortTx = new SortTx(tx);
				validatedTx.add(SortTx);
				removeAlreadyConsumedCoinsFromPool(tx);
				addAlreadyGeneratedCoinsToPool(tx);
			}
		}

		Collections.sort(validatedTx);
		Transaction[] result = new Transaction[validatedTx.size()];
		for (int i = 0; i < validatedTx.size(); i++) {
			result[i] = validatedTx.get(validatedTx.size() - i - 1).transaction;
		}

		return result;
	}

	class SortTx implements Comparable<SortTx> {
		public Transaction transaction;
		private double f;

		public SortTx(Transaction transaction) {
			this.transaction = transaction;
			this.f = Fee(transaction);
		}

		@Override
		public int compareTo(SortTx otherTx) {
			double d = f - otherTx.f;
			if (d > 0) {
				return 1;
			} else if (d < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private double Fee(Transaction transaction) {
		double inSum = InSum(transaction);
		double outSum = OutSum(transaction);

		return inSum - outSum;
	}

	private double OutSum(Transaction tx) {
		double outputSum = 0;
		List<Transaction.Output> outputs = tx.getOutputs();
		for (int j = 0; j < outputs.size(); j++) {
			Transaction.Output output = outputs.get(j);
			outputSum += output.value;
		}
		return outputSum;
	}

	private double InSum(Transaction transaction) {
		List<Transaction.Input> ins = transaction.getInputs();
		double inSum = 0;
		for (int j = 0; j < ins.size(); j++) {
			Transaction.Input in = ins.get(j);
			UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
			Transaction.Output Out = utxoPool.getTxOutput(utxo);
			inSum += Out.value;
		}
		return inSum;
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
