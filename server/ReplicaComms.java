import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

/**
 * Replica Communication interface that does all the data transfer, as it gets called in each replica
 */
public interface ReplicaComms extends Auction {
    public void updateAll() throws RemoteException;
    public void setBidDetails(List<AuctionDetails> detailsRep) throws RemoteException;
    public void setUsers(HashMap<Integer, String> usersRep) throws RemoteException;
    public void setAuctions(HashMap<Integer, Integer> auctionsRep) throws RemoteException;
}
