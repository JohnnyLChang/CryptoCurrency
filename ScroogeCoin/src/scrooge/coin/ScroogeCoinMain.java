package scrooge.coin;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;

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
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, SignatureException, NoSuchProviderException {
        
                // Crypto setup
        // You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);
 
        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();
 
        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();
 
        // START - ROOT TRANSACTION
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
        // which I then can put in the UTXOPool, which will be passed to the TXHandler
        Transaction tx = new Transaction();
        tx.addOutput(10, public_key_scrooge);
 
        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
        tx.addInput(initialHash, 0);
 
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(private_key_scrooge);
        signature.update(tx.getRawDataToSign(0));
        byte[] sig = signature.sign();
 
        tx.addSignature(sig, 0);
        tx.finalize();
        // END - ROOT TRANSACTION
 
        // The transaction output of the root transaction is unspent output
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(),0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));
 
        // START - PROPER TRANSACTION
        Transaction tx2 = new Transaction();
 
        // the Transaction.Output of tx at position 0 has a value of 10
        tx2.addInput(tx.getHash(), 0);
 
        // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
        tx2.addOutput(5, public_key_alice);
        tx2.addOutput(3, public_key_alice);
        tx2.addOutput(2, public_key_alice);
 
        // There is only one (at position 0) Transaction.Input in tx2
        // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
        signature.initSign(private_key_scrooge);
        signature.update(tx2.getRawDataToSign(0));
        sig = signature.sign();
        tx2.addSignature(sig, 0);
        tx2.finalize();
 
        // START - PROPER TRANSACTION
        Transaction tx3 = new Transaction();
 
        // the Transaction.Output of tx at position 0 has a value of 10
        tx3.addInput(tx2.getHash(), 0);
        tx3.addInput(tx2.getHash(), 1);
        tx3.addInput(tx2.getHash(), 2);
 
        // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
        tx3.addOutput(3, public_key_scrooge);
        tx3.addOutput(2, public_key_scrooge);
        tx3.addOutput(1, public_key_scrooge);
 
        // There is only one (at position 0) Transaction.Input in tx2
        // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
        signature.initSign(private_key_alice);
        signature.update(tx3.getRawDataToSign(0));
        sig = signature.sign();
        tx3.addSignature(sig, 0);
        tx3.finalize();// START - PROPER TRANSACTION
        System.out.println("Arrays.equals " + Arrays.equals(tx3.getInput(0).prevTxHash, tx2.getInput(0).prevTxHash));
 
        // remember that the utxoPool contains a single unspent Transaction.Output which is the coin from Scrooge
        TxHandler txHandler = new TxHandler(utxoPool);
        MaxFeeTxHandler maxFeeTxHandler  = new MaxFeeTxHandler(utxoPool);
        System.out.println(txHandler.isValidTx(tx2));
        //System.out.println(txHandler.handleTxs(new Transaction[]{tx2, tx3}).length);
        //System.out.println(txHandler.handleTxs(new Transaction[]{tx2, tx3}).length);
        System.out.println(maxFeeTxHandler.handleTxs(new Transaction[]{tx3, tx2}).length);
        
//        
//        //init the utxo pool
//        UTXOPool utxoPool = new UTXOPool();
//        
//        //init the utxo with some coins
//        Transaction tx = new Transaction();
//        GenerateKeyPairs();
//        tx.addOutput(3.0, m_keyPair.get(0).getPublic());
//        GenerateKeyPairs();
//        tx.addOutput(1.0, m_keyPair.get(1).getPublic());
//        GenerateKeyPairs();
//        tx.addOutput(5.0, m_keyPair.get(2).getPublic());
//        System.out.println("tx inputs:"+tx.numInputs());
//        System.out.println("tx outputs:"+tx.numOutputs());
//        System.out.println("tx prev:"+tx);
//        tx.finalize();
//        
//        //push tx into UTXO pool
//        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(0));
//        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(1));
//        utxoPool.addUTXO(generateUTXO(tx), tx.getOutput(2));
//        
//        for(int i=0;i<3;i++)
//        {
//            Transaction.Output out = utxoPool.getTxOutput(m_utxo.get(i));
//            System.out.printf("utxo[%d] outputs:%f\n", m_utxo.get(i).getIndex(), out.value);
//        }
//        
//        //create txhdr and assign utxoPool
//        TxHandler txhdr = new TxHandler(utxoPool);
//        
//        {
//            Transaction txgood = new Transaction();
//            PublicKey txgood_key = m_keyPair.get(0).getPublic();
//            txgood.addOutput(3.0, txgood_key);
//            txgood.addInput(tx.getHash(), 0);
//            Signature sig = Signature.getInstance("SHA256withRSA");
//            sig.initSign(m_keyPair.get(0).getPrivate());
//            sig.update(txgood.getRawDataToSign(0));
//            txgood.addSignature(sig.sign(), 0);
//            txgood.setHash(tx.getHash());
//            if(txhdr.isValidTx(txgood))
//                System.out.println("test 1 passed");
//            else
//                System.out.println("test 1 failed");
//        }
//        
//        System.out.println("test finished");
    }
    
}
