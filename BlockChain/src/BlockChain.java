
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

class BlockNode
{
    public Block block;
    public BlockNode parent;
    public UTXOPool utxoPool;
    public Integer height;
    public ArrayList<BlockNode> childnodes;
    public BlockNode(Block b, BlockNode parent, UTXOPool uPool){
        this.block = b;
        this.utxoPool = uPool;
        this.parent = parent;
        this.childnodes = new ArrayList<>();
        if (parent != null) {
                height = parent.height + 1;
                parent.childnodes.add(this);
            } else {
                height = 1;
            }
    }
}

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    
    private HashMap<ByteArrayWrapper, BlockNode> m_BlockPool = new HashMap<ByteArrayWrapper, BlockNode>();
    private Integer m_MaxHeight = 0;
    private TransactionPool m_txPool = new TransactionPool();
    private BlockNode m_MaxHeightBlock;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        ByteArrayWrapper empty = new ByteArrayWrapper(genesisBlock.getHash());
        UTXOPool utxoPool = new UTXOPool();
        BlockNode node = new BlockNode(genesisBlock, null, utxoPool);
        m_BlockPool.put(wrap(genesisBlock.getHash()), node);
        m_MaxHeightBlock = node;
        
        addCoinbaseToUTXOPool(m_MaxHeightBlock.block, m_MaxHeightBlock.utxoPool);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return m_MaxHeightBlock.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return new UTXOPool(m_MaxHeightBlock.utxoPool);
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
        if(block.getPrevBlockHash() == null || block.getHash() == null )
            return false;
        
        BlockNode parentNode = m_BlockPool.get(wrap(block.getPrevBlockHash()));
        if(parentNode == null)
            return false;
        
        if(parentNode.height < this.m_MaxHeightBlock.height - CUT_OFF_AGE)
            return false;
        
        //Check if all the Tx in Block are valid
        TxHandler tmpHandler = new TxHandler(parentNode.utxoPool);
        Transaction[] validTxs = null;
        if(block.getTransactions().size() > 0)
        {
            validTxs = tmpHandler.handleTxs(block.getTransactions().toArray(new Transaction[0]));
            if(validTxs.length != block.getTransactions().size())
                return false;    
        }
        
        UTXOPool utxoPool = new UTXOPool(tmpHandler.getUTXOPool());
        BlockNode currentNode = new BlockNode(block, parentNode, utxoPool);
        addCoinbaseToUTXOPool(currentNode.block, currentNode.utxoPool);
        if(currentNode.height > this.m_MaxHeightBlock.height)
            m_MaxHeightBlock = currentNode;
        
        ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());
        m_BlockPool.put(wrap(block.getHash()), currentNode);

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        m_txPool.addTransaction(tx);
    }
    
    private void addCoinbaseToUTXOPool(Block block, UTXOPool utxoPool) {
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }
    }
    
    private static ByteArrayWrapper wrap(byte[] arr) {
        return new ByteArrayWrapper(arr);
    }
}