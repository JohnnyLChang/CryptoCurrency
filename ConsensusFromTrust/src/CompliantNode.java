import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private Set<Transaction> m_pendingTransactions;
    private Set<Integer> m_followees;
    private Set<Transaction> m_acceptTransactions;
    private double m_p_graph, m_p_malicious, m_p_txDistribution;
    private int m_numRounds;
    
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds)
    {
        m_p_graph = p_graph;
        m_p_malicious = p_malicious;
        m_p_txDistribution = p_txDistribution;;
        m_numRounds = numRounds;
        m_followees = new HashSet<Integer>();
        m_pendingTransactions = new HashSet<Transaction>();
        m_acceptTransactions = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        for(int i=0;i<followees.length;i++)
        {
            if(followees[i]) m_followees.add(i);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        m_pendingTransactions.removeAll(m_pendingTransactions);
        m_pendingTransactions.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return m_pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS? What should I do for candidates?
        for(Candidate obj : candidates)
        {
            //Sanity Check: only receive from followees
            if(!this.m_followees.contains(obj.sender))
                continue;

            //Check if the TX is already accepted
            if(!this.m_acceptTransactions.contains(obj.tx))
            {
                //TODO: do we need to validate the TX before add?
                this.m_acceptTransactions.add(obj.tx);
            }
        }
    }
}
