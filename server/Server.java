import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class Server implements Auction{

    //Used for listing the items
    private AuctionItem[] aItem;

    //A list of all the items in auctions that are going to be bid on
    private List<WinningDetails> winBidDetails = new ArrayList<WinningDetails>();

    //hash map of users - userID - email
    private HashMap<Integer, String> users = new HashMap<>();

    //hash map of auction items - itemID - userID (one user can have many items)
    private HashMap<Integer, Integer> auctionItemsById = new HashMap<>();

    public Server() throws NoSuchAlgorithmException {
        super();
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

        if(auctionItem.length == 0){
            System.out.println("There are no open auctions at the moment!");
        }
        
        return aItem;
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        //check if auction exists
        if(auctionItemsById.get(itemID) == null){
            System.out.println("Auction does not exist!");
            return null;
        }

        //loop through the winning details (where items are put in auctions) 
        //for an item that's real and corresponding to the itemID in the arguments, match it and remove it
        WinningDetails winningDetails = null;
        for(WinningDetails auctionDetails : winBidDetails){
            if(auctionDetails.getAuctionItem().itemID == itemID){
                winningDetails = auctionDetails;
                break;
            }
        }

        //close the auction by removing the item and the user ID from the auction
        auctionItemsById.remove(itemID, userID);
        winBidDetails.remove(winningDetails);

        //System.out.println("Last bet price " + winningDetails.getLastBetPrice());
        //System.out.println("Highest bid " + winningDetails.getAuctionItem().highestBid);

        //check the last bet price
        if(winningDetails.getAuctionItem().highestBid < winningDetails.getLastBetPrice()) {
            System.out.println("There's no winner for this auction.");
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
        WinningDetails winningDetails = winBidDetails.get(itemID - 1);
        AuctionItem auctionItem = winningDetails.getAuctionItem();

        //check if price is smaller than highest bid
        //a bid shouldn't be smaller than the highest current offer so we crash it
        if(auctionItem.highestBid >= price) {
            System.out.println("Bid is smaller than the highest offer at the moment!");
            return false;
        }

        //make price the highest bid
        winningDetails.getAuctionItem().highestBid = price;
        //set the person having the highest bid
        winningDetails.setWinningID(userID);

        return true;
    }

    private AuctionCloseInfo createCloseInfo(String winningEmail, int winningPrice) {
        AuctionCloseInfo auctionCloseInfo = new AuctionCloseInfo();
        auctionCloseInfo.winningEmail = winningEmail;
        auctionCloseInfo.winningPrice = winningPrice;

        return auctionCloseInfo;
        
    }
}