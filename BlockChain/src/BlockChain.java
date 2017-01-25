
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.



public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TxHandler m_txHandler;
    private HashMap<ByteArrayWrapper, Block> m_BlockPool = new HashMap<ByteArrayWrapper, Block>();
    private Block m_headerBlock;
    private Integer m_MaxHeight = 0;
    private HashMap<Integer, ArrayList<Block>> m_MaxHeightBlock = new HashMap<Integer, ArrayList<Block>>();
    private TransactionPool m_txPool = new TransactionPool();
    private UTXOPool m_utxoPool = new UTXOPool();

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        System.out.println("create new BlockChain");
        ByteArrayWrapper empty = new ByteArrayWrapper(genesisBlock.getHash());
        ArrayList<Block> tmp = new ArrayList<Block>();
        tmp.add(genesisBlock);
        m_MaxHeightBlock.put(0, tmp);
        m_BlockPool.put(empty, genesisBlock);
        System.out.println("Add header block");
        m_headerBlock = m_BlockPool.get(empty);
        
        UTXO utxo = new UTXO(genesisBlock.getCoinbase().getHash(), 0);
        m_utxoPool.addUTXO(utxo, genesisBlock.getCoinbase().getOutput(0));
        m_txHandler = new TxHandler(m_utxoPool);
        System.out.println("create new BlockChain finished");
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return m_MaxHeightBlock.get(m_MaxHeight).get(0);
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        //process the Max Height Block transaction and output the UTXO
        Block block = getMaxHeightBlock();
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(block.getCoinbase().getHash(), 0);
        utxoPool.addUTXO(utxo, block.getCoinbase().getOutput(0));
        TxHandler txHandler = new TxHandler(utxoPool);
        ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
        for(Transaction tx : block.getTransactions())
        {
            if(txHandler.isValidTx(tx))
            {
                validTxs.add(tx);
            }
        }
        Object[] objectList = validTxs.toArray();
        Transaction[] TxArray = Arrays.copyOf(objectList,objectList.length,Transaction[].class);
        txHandler.handleTxs(TxArray);
        return txHandler.getUTXOPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return m_txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        
        /*If you receive a block which claims to be a genesis block (parent is a null hash) 
        in the addBlock(Block b) function, you can return false.*/
        if(block.getPrevBlockHash() == null)
        {
            System.out.println("cannot add generic block");
            return false;
        }
        
        //the previous Block is not existed
        ByteArrayWrapper prevHash = new ByteArrayWrapper(block.getPrevBlockHash());
        if(!m_BlockPool.containsKey(prevHash))
        {
            System.out.println("prev is not existed");
            return false;
        }
        
        //Check if all the Tx in Block are valid
        TxHandler tmpHandler = new TxHandler(m_utxoPool);
        Transaction[] validTxs = tmpHandler.handleTxs(block.getTransactions().toArray(new Transaction[0]));
        if(validTxs.length != block.getTransactions().size())
        {
            System.out.println("some tx in not valid");
            return false;
        }
        
        //Test 11: Process a block containing a transaction that claims a UTXO from earlier in its branch that has not yet been claimed
        //Test 10: Process a block containing a transaction that claims a UTXO not on its branch
        if(prevHash.equals(new ByteArrayWrapper(this.getMaxHeightBlock().getHash())))
        {
            UTXO utxo = new UTXO(block.getCoinbase().getHash(), 0);
            this.m_utxoPool.addUTXO(utxo, block.getCoinbase().getOutput(0));
            m_MaxHeight++; 
            ArrayList<Block> tmp = new ArrayList<Block>();
            tmp.add(block);
            m_MaxHeightBlock.put(m_MaxHeight, tmp);
        }
        
        ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());
        m_BlockPool.put(blockHash, block);
        
        for(Transaction tx : block.getTransactions())
        {
            m_txPool.removeTransaction(tx.getHash());
        }
        System.out.println("Add Block successfully");
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        m_txPool.addTransaction(tx);
    }
}