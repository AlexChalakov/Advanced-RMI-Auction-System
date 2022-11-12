import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.HashMap;
import java.util.List;

public class Server implements Auction{

    private AuctionItem item;
    private AuctionItem[] aItem;

    //A list of all the won bid's details
    private List<WinningDetails> winBidDetails;

    //hash map of users - userID - email
    private HashMap<Integer, String> users = new HashMap<>();

    //hash map of auction items - itemID - userID (one user can have many items)
    private HashMap<Integer, Integer> auctionItemsById = new HashMap<>();

    public Server() throws NoSuchAlgorithmException {
        super();
        aItem = new AuctionItem[2];

        item = new AuctionItem();
        item.itemID = 1;
        item.name = "Bike";
        item.description = "Has two wheels";
        item.highestBid = 100;
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            String name = "Auction";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        //items[0] = item;

        return null; //sealedObject
    }

    @Override
    public int newUser(String email) throws RemoteException {
        //check if the user exists
        if(!users.containsValue(email)){
            users.put(users.size() + 1, email);
            return users.size();
        } else {
            return 0;
        }
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        //check if user exists in system

        //check if auction exists, return its id

        AuctionItem auctionItem = new AuctionItem();

        auctionItem.itemID = auctionItemsById.size() + 1;
        auctionItem.name = item.name;
        auctionItem.description = item.description;
        auctionItem.highestBid = item.reservePrice;
    
        auctionItemsById.put(auctionItem.itemID, userID);
        WinningDetails winningDetails = new WinningDetails(userID, auctionItem);
        winBidDetails.add(winningDetails);

        return auctionItemsById.size();
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        AuctionItem[] auctionItem = new AuctionItem[auctionItemsById.size()];
        for(int i = 0; i < auctionItem.length; i++){
            aItem[i] = auctionItem[auctionItemsById.get(i)];
        }
        return aItem;
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        //check if auction exists

        AuctionCloseInfo auctionCloseInfo = createCloseInfo(null, itemID);
        WinningDetails winningDetails = new WinningDetails(itemID, item);

        return null;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        return false;
    }

    private AuctionCloseInfo createCloseInfo(String winningEmail, int winningPrice) {
        AuctionCloseInfo aCloseInfo = new AuctionCloseInfo();
        aCloseInfo.winningEmail = winningEmail;
        aCloseInfo.winningPrice = winningPrice;

        return aCloseInfo;
        
    }
}