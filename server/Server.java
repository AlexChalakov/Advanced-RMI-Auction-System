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
    private List<AuctionDetails> winBidDetails = new ArrayList<AuctionDetails>();

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
        for(AuctionDetails auctionDetails: winBidDetails){
            if(auctionDetails.winningID == itemID){
                return auctionDetails.getAuctionItem();
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
            return -1;
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
    public synchronized int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
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
        // + stating this item has been created by this user
        auctionItemsById.put(auctionItem.itemID, userID);
        System.out.println(userID);
        System.out.println(auctionItem.itemID);
        System.out.println(auctionItem.highestBid);
        System.out.println("Item array is " + auctionItemsById.size());
        //putting userID and the item in the auction details
        AuctionDetails auctionDetails = new AuctionDetails(userID, auctionItem, auctionItem.highestBid);
        //putting it in the list of auctions
        winBidDetails.add(auctionDetails);

        return auctionItem.itemID;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        AuctionItem[] auctionItem = new AuctionItem[auctionItemsById.size()];
        if(auctionItem.length == 0){
            System.out.println("There are no open auctions at the moment!");
        }

        aItem = new AuctionItem[auctionItem.length];

        for(int i = 0; i < auctionItem.length; i++){
            aItem[i] = winBidDetails.get(i).auctionItem;
        }
        return aItem;
    }

    @Override
    public synchronized AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        //making sure only the owner of the auction can close it
        if(auctionItemsById.get(itemID) != userID){
            System.out.println("You have no authority to close the auction!");
            return null;
        }
        //check if auction exists
        if(auctionItemsById.get(itemID) == null){
            System.out.println("Auction does not exist!");
            return null;
        }

        //loop through the auction details (where items are put in auctions) 
        //for an item that's real and corresponding to the itemID in the arguments, match it and remove it
        AuctionDetails auctionDetails = null;
        for(AuctionDetails auctDetails : winBidDetails){
            if(auctDetails.getAuctionItem().itemID == itemID){
                auctionDetails = auctDetails;
                break;
            }
        }

        //close the auction by removing the item and the user ID from the auction
        auctionItemsById.remove(itemID, userID);
        winBidDetails.remove(auctionDetails);

        //System.out.println("Last bet price " + winningDetails.getLastBetPrice());
        //System.out.println("Highest bid " + winningDetails.getAuctionItem().highestBid);

        //check the last bet price
        if(auctionDetails.getAuctionItem().highestBid < auctionDetails.getLastBetPrice()) {
            System.out.println("There's no winner for this auction.");
            AuctionCloseInfo auctionCloseInfo = createCloseInfo(null, 0);
            return auctionCloseInfo;
        }

        //getting the auction close info - getting the email from the function and the highest bid from the winning details
        AuctionCloseInfo auctionCloseInfo = createCloseInfo(getEmail(auctionDetails.winningID), auctionDetails.getAuctionItem().highestBid);

        return auctionCloseInfo;
    }

    @Override
    public synchronized boolean bid(int userID, int itemID, int price) throws RemoteException {
        
        //get item from list of auction details by the itemID
        AuctionDetails auctionDetails = winBidDetails.get(itemID - 1);
        AuctionItem auctionItem = auctionDetails.getAuctionItem();

        //check if price is smaller than highest bid
        //a bid shouldn't be smaller than the highest current offer so we crash it
        if(auctionItem.highestBid >= price) {
            System.out.println("Bid is smaller or equal to the highest offer at the moment!");
            return false;
        }

        //make price the highest bid
        auctionDetails.getAuctionItem().highestBid = price;
        //set the person having the highest bid
        auctionDetails.setWinningID(userID);

        return true;
    }

    private AuctionCloseInfo createCloseInfo(String winningEmail, int winningPrice) {
        AuctionCloseInfo auctionCloseInfo = new AuctionCloseInfo();
        auctionCloseInfo.winningEmail = winningEmail;
        auctionCloseInfo.winningPrice = winningPrice;

        return auctionCloseInfo;
    }

    //Authentication
    @Override
    public byte[] challenge(int userID) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }
}