
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

/**
 *
 * @author johnny
 */
public class ScroogeCoinMain {

    private static int m_utxoID = -1;
    
    private static final SecureRandom random = new SecureRandom();
    private static ArrayList<KeyPair> m_keyPair = new ArrayList<KeyPair>();
    private static ArrayList<UTXO> m_utxo = new ArrayList<UTXO>();

    private static String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
  
    private static UTXO generateUTXO(Transaction tx) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        m_utxoID++;
        UTXO utxo = new UTXO(tx.getHash(), m_utxoID);
        m_utxo.add(utxo);
        return utxo;
    }
    
    private static void GenerateKeyPairs() throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.genKeyPair();
        m_keyPair.add(pair);
    }
    
    /**
     * @param args the command line arguments
     */
    //utxoPool.addUTXO(generateUTXO(), );
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
        
        //init the utxo pool
        UTXOPool utxoPool = new UTXOPool();
        
        //init the utxo with some coins
        Transaction tx = new Transaction();
        GenerateKeyPairs();
        tx.addOutput(3.0, m_keyPair.get(0).getPublic());
        GenerateKeyPairs();
        tx.addOutput(1.0, m_keyPair.get(1).getPublic());
        GenerateKeyPairs();
        tx.addOutput(5.0, m_keyPair.get(2).getPublic());
        System.out.println("tx inputs:"+tx.numInputs());
        System.out.println("tx outputs:"+tx.numOutputs());
        System.out.println("tx prev:"+tx);
        tx.finalize();
        
        //push tx into UTXO pool
        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(0));
        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(1));
        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(2));
        
        for(int i=0;i<3;i++)
        {
            Transaction.Output out = utxoPool.getTxOutput(m_utxo.get(i));
            System.out.printf("utxo[%d] outputs:%f\n", m_utxo.get(i).getIndex(), out.value);
        }
        
        //create txhdr and assign utxoPool
        TxHandler txhdr = new TxHandler(utxoPool);
        
        {
            Transaction txgood = new Transaction();
            PublicKey txgood_key = m_keyPair.get(0).getPublic();
            txgood.addOutput(3.0, txgood_key);
            txgood.addInput(null, 0);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(m_keyPair.get(0).getPrivate());
            sig.update(txgood.getRawDataToSign(0));
            txgood.addSignature(sig.sign(), 0);
            txgood.setHash(tx.getHash());
            if(txhdr.isValidTx(txgood))
                System.out.println("test 1 passed");
            else
                System.out.println("test 1 failed");
        }
        
        System.out.println("test finished");
    }
    
}
