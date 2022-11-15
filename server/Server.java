import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class Server implements Auction{

    //used for just creating initial item
    private AuctionItem item;
    private AuctionItem[] aItem;

    //A list of all the won bid's details
    private List<WinningDetails> winBidDetails = new ArrayList<WinningDetails>();

    //hash map of users - userID - email
    private HashMap<Integer, String> users = new HashMap<>();

    //hash map of auction items - itemID - userID (one user can have many items)
    private HashMap<Integer, Integer> auctionItemsById = new HashMap<>();

    public Server() throws NoSuchAlgorithmException {
        super();
        /*aItem = new AuctionItem[2];

        item = new AuctionItem();
        item.itemID = 1;
        item.name = "Bike";
        item.description = "Has two wheels";
        item.highestBid = 100;*/
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
        for(WinningDetails winningDetails: winBidDetails){
            if(winningDetails.winningID == itemID){
                return winningDetails.getAuctionItem();
            }
        }

        return null;
    }

    @Override
    public int newUser(String email) throws RemoteException {
        //check if the user exists
        if(!users.containsValue(email)){
            users.put(users.size() + 1, email);
            System.out.println(users);
            return users.size();
        } else {
            return 0;
        }
    }
    
    public String getEmail(int userID) {
        if(users.containsKey(userID)){
            return users.get(userID);
        } else {
            return null;
        }
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        //check if user exists in system
        if(!users.containsKey(userID)){
            String errorMsg = "User ID does not exist in the system";
            System.out.println(errorMsg);
            return -1;
        }
        //check if auction exists, return its id

        //creating new auction item
        AuctionItem auctionItem = new AuctionItem();

        auctionItem.itemID = auctionItemsById.size() + 1;
        auctionItem.name = item.name;
        auctionItem.description = item.description;
        auctionItem.highestBid = item.reservePrice;
        
        //putting itemID and user in auction hashmap
        auctionItemsById.put(auctionItem.itemID, userID);
        System.out.println(userID);
        System.out.println(auctionItem.itemID);
        System.out.println(auctionItem.highestBid);
        System.out.println("Item array is " + auctionItemsById.size());
        //putting userID and the item in the winning details
        WinningDetails winningDetails = new WinningDetails(userID, auctionItem, auctionItem.highestBid);
        winBidDetails.add(winningDetails);

        return auctionItem.itemID;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        AuctionItem[] auctionItem = new AuctionItem[auctionItemsById.size()];
        aItem = new AuctionItem[auctionItem.length];
        for(int i = 0; i < auctionItem.length; i++){
            aItem[i] = winBidDetails.get(i).auctionItem;
        }
        return aItem;
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        //check if auction exists
        if(auctionItemsById.get(itemID) == null){
            return null;
        }

        //get the itemID and notate it towards the winning details
        Integer AuctionItemID = auctionItemsById.get(itemID);
        WinningDetails winningDetails = new WinningDetails(AuctionItemID);

        //close the auction by removing the item and the user ID from the auction
        auctionItemsById.remove(itemID, userID);
        winBidDetails.remove(winningDetails);

        //check the last bet price
        if(winningDetails.getLastBetPrice() >= winningDetails.getAuctionItem().highestBid) {
            AuctionCloseInfo auctionCloseInfo = createCloseInfo(null, 0);
            return auctionCloseInfo;
        }

        //getting the auction close info - getting the email from the function and the highest bid from the winning details
        AuctionCloseInfo auctionCloseInfo = createCloseInfo(getEmail(winningDetails.winningID), winningDetails.getAuctionItem().highestBid);

        return auctionCloseInfo;
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        
        //get item from list of winning details by the itemID
        WinningDetails winningDetails = winBidDetails.get(itemID);
        AuctionItem auctionItem = winningDetails.getAuctionItem();

        //check if price is smaller than highest bid
        if(auctionItem.highestBid >= price) {
            return false;
        }

        //make price the highest bid
        winningDetails.getAuctionItem().highestBid = price;
        //set the person having the highest bid
        winningDetails.setWinningID(userID);

        return true;
    }

    private AuctionCloseInfo createCloseInfo(String winningEmail, int winningPrice) {
        AuctionCloseInfo aCloseInfo = new AuctionCloseInfo();
        aCloseInfo.winningEmail = winningEmail;
        aCloseInfo.winningPrice = winningPrice;

        return aCloseInfo;
        
    }
}