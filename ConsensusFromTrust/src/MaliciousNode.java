import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class MaliciousNode implements Node {
    private Set<Transaction> m_pendingTransactions;
    private ArrayList<Integer> m_followees;
    private double m_p_graph, m_p_malicious, m_p_txDistribution;
    private int m_numRounds;
    
    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        m_p_graph = p_graph;
        m_p_malicious = p_malicious;
        m_p_txDistribution = p_txDistribution;
        m_numRounds = numRounds;
        m_followees = new ArrayList<Integer>();
        m_pendingTransactions = new HashSet<Transaction>();
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
        return;
    }

    public Set<Transaction> sendToFollowers() {
        return new HashSet<Transaction>();
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        return;
    }
}
