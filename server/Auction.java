import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Auction extends Remote {
    public int newUser(String email) throws RemoteException;

    public AuctionItem getSpec(int itemID) throws RemoteException;

    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException;

    public AuctionItem[] listItems() throws RemoteException;

    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException;

    public boolean bid(int userID, int itemID, int price) throws RemoteException;
}
