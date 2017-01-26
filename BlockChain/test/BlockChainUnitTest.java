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
    
    @Test  //this anotation is very important
    public void TestCreateBlockAfterTransaction(){ 
        
        try {
            
            Block genericsBlock = new Block(null, m_ownerpair.getPublic());
            genericsBlock.finalize();
            
            BlockChain oBlockChain = new BlockChain(genericsBlock);
            Boolean bret = oBlockChain.addBlock(genericsBlock);
            assertFalse(bret);
            
            BlockHandler blockHdlr = new BlockHandler(oBlockChain);
            bret = blockHdlr.processBlock(genericsBlock);
            assertFalse(bret);
            
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
            System.out.println("process valid Tx");
            blockHdlr.processTx(tx);
            
            //Add first block
            Block blockFirst = blockHdlr.createBlock(public_key_alice);
            assertNotNull(blockFirst);
            //the new block show countain the valid Tx
            assertNotSame(blockFirst.getTransactions().size(), 0);
            assertSame(blockFirst.getTransaction(0).getHash(), tx.getHash());
            
            //Add second block
            Block blockSecond = blockHdlr.createBlock(public_key_alice);
            assertNotNull(blockSecond);
            //the new block show countain the valid Tx
            assertSame(blockSecond.getTransactions().size(), 0);
            ByteArrayWrapper first = new ByteArrayWrapper(blockFirst.getHash());
            assertTrue(first.equals(new ByteArrayWrapper(blockSecond.getPrevBlockHash())));
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
    public void TestAddSpentTransactionBlock(){ 
        
        try {
            
            Block genericsBlock = new Block(null, m_ownerpair.getPublic());
            genericsBlock.finalize();
            
            BlockChain oBlockChain = new BlockChain(genericsBlock);
            BlockHandler blkHandler = new BlockHandler(oBlockChain);
            
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey private_key_alice = pair.getPrivate();
            PublicKey public_key_alice = pair.getPublic();
            
            pair = keyGen.generateKeyPair();
            PrivateKey private_key_bob = pair.getPrivate();
            PublicKey public_key_bob = pair.getPublic();
            
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            
            // START - PROPER TRANSACTION
            Transaction tx2 = new Transaction();

            // the Transaction.Output of tx at position 0 has a value of 10
            tx2.addInput(genericsBlock.getCoinbase().getHash(), 0);

            // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
            tx2.addOutput(5, public_key_alice);
            tx2.addOutput(10, public_key_alice);
            tx2.addOutput(10, public_key_alice);

            // There is only one (at position 0) Transaction.Input in tx2
            // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
            signature.initSign(this.m_ownerpair.getPrivate());
            signature.update(tx2.getRawDataToSign(0));
            byte[] sig = signature.sign();
            tx2.addSignature(sig, 0);
            tx2.finalize();

            // START - PROPER TRANSACTION
            Transaction tx3 = new Transaction();

            // the Transaction.Output of tx at position 0 has a value of 10
            tx3.addInput(tx2.getHash(), 0);

            // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
            tx3.addOutput(1, public_key_bob);
            tx3.addOutput(1, public_key_bob);
            tx3.addOutput(3, public_key_bob);

            // There is only one (at position 0) Transaction.Input in tx2
            // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
            signature.initSign(private_key_alice);
            signature.update(tx3.getRawDataToSign(0));
            sig = signature.sign();
            tx3.addSignature(sig, 0);
            tx3.finalize();
            
            blkHandler.processTx(tx2);
            blkHandler.processTx(tx3);
            
            System.out.println("UTXO size in stage 1 is "+oBlockChain.getMaxHeightUTXOPool().getAllUTXO().size());
            
            //Add first block
            Block blockFirst = blkHandler.createBlock(public_key_bob);
            assertFalse(blockFirst == null);
            System.out.println("UTXO size in stage 2 is "+oBlockChain.getMaxHeightUTXOPool().getAllUTXO().size());
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
    public void TestProcessMultipleBlocksOnGenericsBlock() throws NoSuchProviderException, InvalidKeyException, SignatureException{ 
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);
            
            Block genericsBlock = new Block(null, m_ownerpair.getPublic());
            genericsBlock.finalize();
            BlockChain oBlockChain = new BlockChain(genericsBlock);
            BlockHandler blkHandler = new BlockHandler(oBlockChain);
            System.out.println("[MultipleBlocks] create initial block chain");
            
            KeyPair pair = keyGen.generateKeyPair();
            Block second = blkHandler.createBlock(pair.getPublic());
            System.out.println("[MultipleBlocks] create second block node");
            
            KeyPair pair1 = keyGen.generateKeyPair();
            Block forkBlock = new Block(genericsBlock.getHash(), pair1.getPublic());
            forkBlock.finalize();
            Boolean bret = blkHandler.processBlock(forkBlock);
            assertTrue(bret);
            System.out.println("[MultipleBlocks] process another block node 1");
            
            KeyPair pair2 = keyGen.generateKeyPair();
            Block forkBlock2 = new Block(genericsBlock.getHash(), pair2.getPublic());
            forkBlock2.finalize();
            bret = blkHandler.processBlock(forkBlock2);
            assertTrue(bret);
            System.out.println("[MultipleBlocks] process another block node 2");
            
            // START - PROPER TRANSACTION
            Transaction tx2 = new Transaction();
            tx2.addInput(genericsBlock.getCoinbase().getHash(), 0);

            Signature signature = Signature.getInstance("SHA256withRSA");
            
            KeyPair Alice = keyGen.generateKeyPair();
            tx2.addOutput(5, Alice.getPublic());
            tx2.addOutput(10, Alice.getPublic());
            tx2.addOutput(10, Alice.getPublic());
            signature.initSign(m_ownerpair.getPrivate());
            signature.update(tx2.getRawDataToSign(0));
            byte[] sigtx2 = signature.sign();
            tx2.addSignature(sigtx2, 0);
            tx2.finalize();
            blkHandler.processTx(tx2);
            
            KeyPair pair3 = keyGen.generateKeyPair();
            Block second2 = blkHandler.createBlock(pair3.getPublic());
            assertSame(second2.getTransactions().size(), 1);
            System.out.println("[MultipleBlocks] createBlock over maxheight, poolsize is "
                    + oBlockChain.getMaxHeightUTXOPool().getAllUTXO().size());
            
            // START - PROPER TRANSACTION
            Transaction tx3 = new Transaction();

            tx3.addInput(tx2.getHash(), 0);
            tx3.addInput(tx2.getHash(), 1);
            tx3.addInput(tx2.getHash(), 2);

            KeyPair pair4 = keyGen.generateKeyPair();
            tx3.addOutput(6, pair4.getPublic());
            tx3.addOutput(8, pair4.getPublic());
            tx3.addOutput(11, pair4.getPublic());
            signature.initSign(Alice.getPrivate());
            signature.update(tx3.getRawDataToSign(0));
            byte[] sig = signature.sign();
            tx3.addSignature(sig, 0);
            signature.update(tx3.getRawDataToSign(1));
            sig = signature.sign();
            tx3.addSignature(sig, 1);
            signature.update(tx3.getRawDataToSign(2));
            sig = signature.sign();
            tx3.addSignature(sig, 2);
            tx3.finalize();
            blkHandler.processTx(tx3);
            
            KeyPair pair5 = keyGen.generateKeyPair();
            Block second3 = blkHandler.createBlock(pair5.getPublic());
            assertSame(oBlockChain.getMaxHeightUTXOPool().getAllUTXO().size(), 6);
            System.out.println("UTXO pool size " + oBlockChain.getMaxHeightUTXOPool().getAllUTXO().size());
            
            //check if the previous block is equal
            ByteArrayWrapper tmp2 = new ByteArrayWrapper(second2.getHash());
            assertTrue(tmp2.equals(new ByteArrayWrapper(second3.getPrevBlockHash())));
            
            //check if the highest is current block
            ByteArrayWrapper highest = new ByteArrayWrapper(oBlockChain.getMaxHeightBlock().getHash());
            assertTrue(highest.equals(new ByteArrayWrapper(second3.getHash())));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChainUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
