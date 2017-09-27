package scrooge.coin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MaxFeeTxHandler {

    private UTXOPool m_utxoPool;
    HashMap<String, Transaction> _txsMap = new HashMap<String, Transaction>();

    /*
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
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
    
    private void GetMaxTxsChain(List<Transaction> _tmpTxs, Transaction _txs)
    {
        if(_txs.numInputs() > 0)
        {
            //System.out.println("process inputs");
            for(Transaction.Input obj : _txs.getInputs())
            {
                Transaction tx = _txsMap.get(Arrays.toString(obj.prevTxHash));
                if(tx == null)
                {
                    return;
                }
                else
                {
                    if(0 > _tmpTxs.indexOf(tx))
                    {
                        _tmpTxs.add(tx);
                        GetMaxTxsChain(_tmpTxs, tx);
                    }
                }
            }
        }
        else
            return;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        List<Transaction> _tmpTxs = new ArrayList<Transaction>();
        double max = 0.0;
        int maxchain = 0;
        
        for(int i=0; i<possibleTxs.length; i++)
        {
            _txsMap.put(Arrays.toString(possibleTxs[i].getHash()), possibleTxs[i]);
            //System.out.println("Tx Hash:" + Arrays.toString( possibleTxs[i].getHash()));
            //System.out.println("prevhash:" + Arrays.toString( possibleTxs[i].getInput(0).prevTxHash));
            int j = 0;
            for(Transaction.Output out : possibleTxs[i].getOutputs())
            {
                UTXO utxo = new UTXO(possibleTxs[i].getHash(), j);
                m_utxoPool.addUTXO(utxo, out);
                j++;
            }
        }
        
        //System.out.println("contains key:" + Arrays.toString(possibleTxs[1].getInput(0).prevTxHash));
        //System.out.println("contains key:" + _txsMap.containsKey(Arrays.toString(possibleTxs[1].getInput(0).prevTxHash)));
        for(int i=0; i<possibleTxs.length; i++)
        {
            //if(!isValidTx(possibleTxs[i]))
                //continue;
            //boolean goodTx = true;
            //System.out.println("transaction " + i);
            //if(isValidTx(possibleTxs[i]))
            //{
            List<Transaction> _localTxs = new ArrayList<Transaction>();
            _localTxs.add(possibleTxs[i]);
            //GetMaxTxsChain(_localTxs, possibleTxs[i]);
            double inputfee = 0.0;
            double outputfee = 0.0;
            //System.out.println("localTxs size:" + _localTxs.size());
            //for(Transaction tx : _localTxs)
            {
                for(Transaction.Input obj : possibleTxs[i].getInputs())
                {
                    UTXO utxo = new UTXO(obj.prevTxHash, obj.outputIndex);
                    if(m_utxoPool.contains(utxo))
                    {
                        inputfee += m_utxoPool.getTxOutput(utxo).value;
                    }
                    else
                    {
                        System.out.println("not found");
                    }
                }
                {
                    //if(tx.getHash() != null)
                    int j=0;
                    for(Transaction.Output obj : possibleTxs[i].getOutputs())
                    {
                        outputfee += obj.value;
                        j++;
                    }
                }
            }
            double tmp = inputfee - outputfee;
            //System.out.printf("%f - %f \n", inputfee, outputfee);
            //System.out.println("tmp:" + tmp);
            _tmpTxs.add(possibleTxs[i]);
        }
        return _tmpTxs.toArray(new Transaction[0]);
    }
}
