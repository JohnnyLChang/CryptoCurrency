
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TxHandler {

    private UTXOPool m_utxoPool;
    private ArrayList<UTXO> m_allUtxo;
    private double m_totalSum;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        m_utxoPool = utxoPool;
        m_allUtxo = m_utxoPool.getAllUTXO();
        for(UTXO o: m_allUtxo)
        {
            m_totalSum += m_utxoPool.getTxOutput(o).value;
        }
    }

    private boolean isValidOutput(Transaction.Output txoutput)
    {
        for(int i=0;i<m_allUtxo.size();i++)
        {
            //System.out.println("txoutput value " + txoutput.value + " pool value: " + m_utxoPool.getTxOutput(m_allUtxo.get(i)).value);
            //System.out.println("txoutput address " + txoutput.address + " pool address: " + m_utxoPool.getTxOutput(m_allUtxo.get(i)).address);
            if(txoutput.value == m_utxoPool.getTxOutput(m_allUtxo.get(i)).value &&
               txoutput.address == m_utxoPool.getTxOutput(m_allUtxo.get(i)).address)
            {
                return true;
            }
        }
        return false;
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
        double total = m_totalSum;
        
        //nullity check
        if(tx.numOutputs() == 0)
            return false;
        
        //(1) all outputs claimed by {@code tx} are in the current UTXO pool
        ArrayList<Transaction.Output> allTxOutput = tx.getOutputs();
        for (Transaction.Output obj: allTxOutput) {
            if(!isValidOutput(obj))
                return false;
            //(4) all of {@code tx}s output values are non-negative,
            if(obj.value < 0)
                return false;
            
            total = total - obj.value;
            if(total < 0)
                return false;
        }
        
        //(2) the signatures on each input of {@code tx} are valid
        if(tx.numInputs() == 0)
            return false;
        
        ArrayList<Transaction.Input> allTxInput = tx.getInputs();
        for (int i=0;i<allTxInput.size();i++) {
            Transaction.Input obj = allTxInput.get(i);
            if(!Crypto.verifySignature(tx.getOutput(obj.outputIndex).address, tx.getRawDataToSign(0), obj.signature))
                return false;
        }
        
        //(3) no UTXO is claimed multiple times by {@code tx},
        UTXOPool tmpPool = new UTXOPool(m_utxoPool);
        for(Transaction.Input obj: allTxInput)
        {
            UTXO utxo = new UTXO(tx.getHash(), obj.outputIndex);
            if(tmpPool.contains(utxo))
                tmpPool.removeUTXO(utxo);
            else
                return false;
        }
        
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> _tmpTxs = new ArrayList<Transaction>();
        HashMap<byte [], Transaction> _tmpMap = new HashMap<byte [], Transaction>();
        Transaction firstTx = new Transaction();
        for(int i=0; i<possibleTxs.length; i++)
        {
            if(isValidTx(possibleTxs[i]))
            {
                _tmpMap.put(possibleTxs[i].getHash(), possibleTxs[i]);
                _tmpTxs.add(possibleTxs[i]);
            }
        }
        for(Transaction tx: _tmpTxs)
        {
            
        }
        return _tmpTxs.toArray(new Transaction[0]);
    }

}
