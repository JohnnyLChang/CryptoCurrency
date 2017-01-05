
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class TxHandler {

    private UTXOPool m_utxoPool;
    private static HashMap<String, String>  m_sxoPool = new HashMap<String, String>();

    /*
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        m_utxoPool = new UTXOPool(utxoPool);
    }

    private boolean isValidOutput(Transaction.Input input)
    {
        return m_utxoPool.contains(new UTXO(input.prevTxHash, input.outputIndex));
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
        double totalOutput = 0;
        double totalInput = 0;
        
        
        //(1) all outputs claimed by {@code tx} are in the current UTXO pool
        ArrayList<Transaction.Input> TxInputs = tx.getInputs();
        for (Transaction.Input obj: TxInputs) {
            if(!isValidOutput(obj))
            {
                System.out.println("output not isValidOutput");
                return false;
            }
            
        }
        
        ArrayList<Transaction.Output> TxOutputs = tx.getOutputs();
        for (Transaction.Output obj: TxOutputs) {
            if(obj.value < 0)
                return false;
            totalOutput += obj.value;
        }
        
        //(2) the signatures on each input of {@code tx} are valid    
        ArrayList<Transaction.Input> allTxInput = tx.getInputs();
        for (int i=0;i<allTxInput.size();i++) 
        {
            if(allTxInput.get(i).prevTxHash != null)
            {
                Transaction.Output out = null;
                UTXO utxo = new UTXO(allTxInput.get(i).prevTxHash, allTxInput.get(i).outputIndex);
                if(m_utxoPool.contains(utxo))
                    out = m_utxoPool.getTxOutput(utxo);
                else
                    return false;
                if(!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), allTxInput.get(i).signature))
                    return false;
                totalInput += out.value; 
            }
            else
                return false;
        }
        
        if(totalOutput > totalInput)
        {
            System.out.println("totalOutput > m_totalSum");
            return false;
        }
        
        //(3) no UTXO is claimed multiple times by {@code tx},
        UTXOPool tmpPool = new UTXOPool(m_utxoPool);
        for(Transaction.Input obj: allTxInput)
        {
            UTXO utxo = new UTXO(obj.prevTxHash, obj.outputIndex);
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
    public Transaction[] handleTxs(Transaction[] possibleTxs) throws UnsupportedEncodingException {
        List<Transaction> _tmpTxs = new ArrayList<Transaction>();
        HashMap<byte [], Transaction> _tmpMap = new HashMap<byte [], Transaction>();
        Transaction firstTx = null;
        Transaction cur;
        for(int i=0; i<possibleTxs.length; i++)
        {
            boolean goodTx = true;
            //System.out.println("transaction " + i);
            if(isValidTx(possibleTxs[i]))
            {              
                //check for doublespent
                for(Transaction.Input obj: possibleTxs[i].getInputs())
                {
                    String str1 = Base64.getEncoder().encodeToString(possibleTxs[i].getHash());
                    String str2 = Base64.getEncoder().encodeToString(obj.prevTxHash);
                    if(TxHandler.m_sxoPool.containsKey(str2))
                    {
                        if(TxHandler.m_sxoPool.get(str2).equals(str1))
                        {
                            goodTx = false;
                        }
                    }
                    else
                    {
                        TxHandler.m_sxoPool.put(str2, str1);
                    }
                }   
                
                if(goodTx)
                {
                    if(possibleTxs[i].getInput(0).prevTxHash != null)
                        _tmpMap.put(possibleTxs[i].getHash(), possibleTxs[i]);
                    else
                        firstTx = possibleTxs[i];
                    _tmpTxs.add(possibleTxs[i]);
                }
            }
        }
        
        UTXOPool tmpPool = new UTXOPool(m_utxoPool);
        cur = firstTx;
        while(cur != null)
        {
            ArrayList<Transaction.Input> tmpInput = cur.getInputs();
            ArrayList<Transaction.Output> tmpOutput = cur.getOutputs();
            
            cur = _tmpMap.get(cur.getHash());
        }
        
        return _tmpTxs.toArray(new Transaction[0]);
    }

}
