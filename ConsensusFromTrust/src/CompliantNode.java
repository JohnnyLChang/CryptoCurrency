import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private Set<Transaction> m_pendingTransactions;
    private Set<Integer> m_followees;
    private HashMap<Transaction, Set<Integer>> m_candidateTransactions;
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
        m_candidateTransactions = new HashMap<Transaction, Set<Integer>>();
    }

    public void setFollowees(boolean[] followees) {
        for(int i=0;i<followees.length;i++)
        {
            if(followees[i]) m_followees.add(i);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        m_pendingTransactions = new HashSet<Transaction>(pendingTransactions);
        for(Transaction tx : m_pendingTransactions)
        {
            m_candidateTransactions.put(tx, new HashSet<Integer>());
        }
    }

    public Set<Transaction> sendToFollowers() {
        if(this.m_numRounds > 0)
            return this.m_candidateTransactions.keySet();
        else
        {
            HashSet<Transaction> proposal = new HashSet<Transaction>();
            double nFollowee = (double)this.m_followees.size() / 2;
            for(Transaction tx : this.m_candidateTransactions.keySet())
            {
                //Add only those accept transacion if the Set is more than half aggreed.
                if(this.m_candidateTransactions.get(tx).size() >  nFollowee)
                    proposal.add(tx);
            }
            for(Transaction tx : this.m_pendingTransactions)
                proposal.add(tx);
            return proposal;
        }
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS? What should I do for candidates?
        for(Candidate obj : candidates)
        {
            //Sanity Check: only receive from followees
            if(!this.m_followees.contains(obj.sender))
                continue;

            m_pendingTransactions.add(obj.tx);
            //Check if the TX is already accepted
            if(!this.m_candidateTransactions.containsKey(obj.tx))
            {
                this.m_candidateTransactions.put(obj.tx, new HashSet<Integer>(Arrays.asList(obj.sender)));
            }
            else
            {
                this.m_candidateTransactions.get(obj.tx).add(obj.sender);
            }
        }
        m_numRounds--;
    }
}
