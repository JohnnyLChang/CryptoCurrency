/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author river
 */
public class BlockChainUnitTest {
    private KeyPair m_ownerpair;
    
    public BlockChainUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            m_ownerpair = keyGen.genKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    @Test  //this anotation is very important
    public void TestBlockChainBasics(){ 
        System.out.println(m_ownerpair.getPublic().toString());
        Block genericsBlock = new Block(null, m_ownerpair.getPublic());
        genericsBlock.finalize();
        BlockChain oBlockChain = new BlockChain(genericsBlock);
        Boolean bret = oBlockChain.addBlock(genericsBlock);
        assertFalse(bret);
    }
    
    @Test  //this anotation is very important
    public void TestAddTransactionBlock(){ 
        
        try {
            
            Block genericsBlock = new Block(null, m_ownerpair.getPublic());
            genericsBlock.finalize();
            
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            
            // Generating two key pairs, one for Scrooge and one for Alice
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey private_key_generic = pair.getPrivate();
            PublicKey public_key_generic = pair.getPublic();
            
            pair = keyGen.generateKeyPair();
            PrivateKey private_key_alice = pair.getPrivate();
            PublicKey public_key_alice = pair.getPublic();
            
            // START - ROOT TRANSACTION
            // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
            // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
            // which I then can put in the UTXOPool, which will be passed to the TXHandler
            Transaction tx = new Transaction();
            tx.addOutput(25, public_key_generic);
            
            // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
            byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
            tx.addInput(genericsBlock.getCoinbase().getHash(), 0);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(m_ownerpair.getPrivate());
            signature.update(tx.getRawDataToSign(0));
            byte[] sig = signature.sign();
            
            tx.addSignature(sig, 0);
            tx.finalize();
            
            BlockChain oBlockChain = new BlockChain(genericsBlock);
            Boolean bret = oBlockChain.addBlock(genericsBlock);
            assertFalse(bret);
            
            //Add first block
            Block blockFirst = new Block(genericsBlock.getHash(), m_ownerpair.getPublic());
            blockFirst.addTransaction(tx);
            blockFirst.finalize();
            System.out.println("Add a transcation with valid Tx");
            bret = oBlockChain.addBlock(blockFirst);
            assertTrue(bret);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Test  //this anotation is very important
    public void TestAddDoubleSpendTransactionBlock(){ 
        
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            
            // Generating two key pairs, one for Scrooge and one for Alice
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey private_key_generic = pair.getPrivate();
            PublicKey public_key_generic = pair.getPublic();
            
            pair = keyGen.generateKeyPair();
            PrivateKey private_key_alice = pair.getPrivate();
            PublicKey public_key_alice = pair.getPublic();
            
            pair = keyGen.generateKeyPair();
            PrivateKey private_key_bob = pair.getPrivate();
            PublicKey public_key_bob = pair.getPublic();
            
            // START - ROOT TRANSACTION
            // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
            // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
            // which I then can put in the UTXOPool, which will be passed to the TXHandler
            Transaction tx = new Transaction();
            tx.addOutput(10, public_key_generic);
            
            // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
            byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
            tx.addInput(initialHash, 0);
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(private_key_generic);
            signature.update(tx.getRawDataToSign(0));
            byte[] sig = signature.sign();
            
            tx.addSignature(sig, 0);
            tx.finalize();
            
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
            signature.initSign(private_key_generic);
            signature.update(tx2.getRawDataToSign(0));
            sig = signature.sign();
            tx2.addSignature(sig, 0);
            tx2.finalize();

            // START - PROPER TRANSACTION
            Transaction tx3 = new Transaction();

            // the Transaction.Output of tx at position 0 has a value of 10
            tx3.addInput(tx.getHash(), 0);

            // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
            tx3.addOutput(5, public_key_bob);
            tx3.addOutput(3, public_key_bob);
            tx3.addOutput(2, public_key_bob);

            // There is only one (at position 0) Transaction.Input in tx2
            // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
            signature.initSign(private_key_generic);
            signature.update(tx3.getRawDataToSign(0));
            sig = signature.sign();
            tx3.addSignature(sig, 0);
            tx3.finalize();
            
            Block genericsBlock = new Block(null, m_ownerpair.getPublic());
            genericsBlock.finalize();
            
            BlockChain oBlockChain = new BlockChain(genericsBlock);
            
            //Add first block
            Block blockFirst = new Block(genericsBlock.getHash(), m_ownerpair.getPublic());
            blockFirst.addTransaction(tx);
            blockFirst.addTransaction(tx2);
            blockFirst.addTransaction(tx3);
            blockFirst.finalize();
            Boolean bret = oBlockChain.addBlock(blockFirst);
            assertFalse(bret);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
