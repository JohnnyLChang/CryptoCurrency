import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	
	Set<Transaction> pendingTransactions;
	Set<Transaction> initialSetPendingTransactions;
	
	boolean[] followees;
	double p_malicious;
	double p_txDistribution;
	int numRounds;
	int round;
	HashMap<Transaction, Integer> numberOfTxVotes = new HashMap<>();
	private Set<Candidate> lastCandidates; 
	
	

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_malicious=p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds=numRounds;
        this.round=0;
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
//        this.initialSetPendingTransactions = new HashSet<>();
//        this.initialSetPendingTransactions.addAll(pendingTransactions);
        
    	this.pendingTransactions = pendingTransactions;

    }

    public Set<Transaction> sendToFollowers() {
    	if(inExecution()) {
    		return pendingTransactions;    		
    	}
    	else {
//    		return pendingTransactions;    		

    		return selectConsensusTx();
    	}
    	
    }

    public Set<Transaction> selectConsensusTx() {
    	
    	Set<Transaction> newPendingTransactions = new HashSet<>();
    	
    	for (Transaction tx : this.pendingTransactions) {
    		boolean thereis = false;
    		if (this.numberOfTxVotes.get(tx)!=null && this.numberOfTxVotes.get(tx)>1) {
    			thereis = true;
    		}
/*    		for (Candidate c: this.lastCandidates) {
        		if (c.tx.equals(tx)) {
        			thereis = true;
        		}			
    		}
*/    		
    		if (thereis) {
    			newPendingTransactions.add(tx);
    		}
    		
    	}
    	return newPendingTransactions;
    }
	
		
	public boolean inExecution() {
    	return round < numRounds;
    }
	
	private void accept(Set<Candidate> candidates) {
		for (Candidate c: candidates) {
			this.pendingTransactions.add(c.tx);
		}
	}

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	
    	for (Candidate c: candidates) {
    		if (!this.numberOfTxVotes.containsKey(c.tx)) {
    			this.numberOfTxVotes.put (c.tx, 0);
    		}
    		Integer v= this.numberOfTxVotes.get(c.tx);
    		this.numberOfTxVotes.put(c.tx, v+1);
    	}
    	
        accept(candidates);
        round++;        
        
        if (!inExecution()) {
        	this.lastCandidates = candidates;
        }
       
    }
}
