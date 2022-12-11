import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.*;
import java.util.*;

/**
 * Replica is basically a server. There should be many replicas that are able to execute the basic functions,
 * while also maintaining communication with each other.
 */
public class Replica implements ReplicaComms{

    private AuctionItem[] aItem;
    private NewUserInfo newUserInfo;

    private List<AuctionDetails> winBidDetails = new ArrayList<>();
    private HashMap<Integer, String> users = new HashMap<>();
    private HashMap<Integer, Integer> auctionItemsById = new HashMap<>();

    public Replica(){
        super();
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Replica n");
            return;
        }

        try {
            //creating a server replica with the id of the argument cast in terminal
            int replicaId = Integer.parseInt(args[0]);
            Replica replica = new Replica();
            
            //creating the server itself in the registry
            String name = "Replica" + replicaId;
            Auction stub = (Auction) UnicastRemoteObject.exportObject(replica, 0);
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(name, stub);

            System.out.println("Replica ready");
        } catch (RemoteException e) {
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
    public NewUserInfo newUser(String email) throws RemoteException {
        //check if the user exists
        if(!users.containsValue(email)){
            users.put(users.size() + 1, email);
            System.out.println(users);

            newUserInfo = new NewUserInfo();

            KeyPairGenerator keyPG;
            try {
                keyPG = KeyPairGenerator.getInstance("RSA");
                keyPG.initialize(2048);
                KeyPair keyPair = keyPG.generateKeyPair();
                PublicKey publKey = keyPair.getPublic();
                PrivateKey prvKey = keyPair.getPrivate();

                newUserInfo.userID = users.size();
                newUserInfo.publicKey = publKey.getEncoded();
                newUserInfo.privateKey = prvKey.getEncoded();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                updateAll();
            } catch (Exception e) {
                System.out.println("Update failed.");
                //e.printStackTrace();
            }
            return newUserInfo;
        } else {
            return null;
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
        try {
            updateAll();
        } catch (Exception e) {
            System.out.println("Update failed.");
            //e.printStackTrace();
        }
        return auctionItem.itemID;
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException{
        AuctionItem[] auctionItem = new AuctionItem[auctionItemsById.size()];
        if(auctionItem.length == 0){
            System.out.println("There are no open auctions at the moment! Replica is Alive.");
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

        try {
            updateAll();
        } catch (Exception e) {
            System.out.println("Update failed.");
            e.printStackTrace();
        }
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

        try {
            updateAll();
        } catch (Exception e) {
            System.out.println("Update failed.");
            e.printStackTrace();
        }
        return true;
    }

    private AuctionCloseInfo createCloseInfo(String winningEmail, int winningPrice) {
        AuctionCloseInfo auctionCloseInfo = new AuctionCloseInfo();
        auctionCloseInfo.winningEmail = winningEmail;
        auctionCloseInfo.winningPrice = winningPrice;

        return auctionCloseInfo;
    }

    @Override
    public byte[] challenge(int userID) throws RemoteException {
        return null;
    }

    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        return true;
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction frontEnd = (Auction) registry.lookup("FrontEnd");
            return frontEnd.getPrimaryReplicaID();
        } catch (NotBoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update method that is called at the end of every function that is copied from the server.
     * It goes through the registry, and updates all the replica except the primary one with the info.
     * Basically it does the data transfer so the flow of information is complete even if one of the replicas is destroyed.
     */
    @Override
    public void updateAll() throws RemoteException{
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            for(String name : registry.list()){
                if(!(name.equals("Replica"+getPrimaryReplicaID())) &&name.contains("Replica")){
                    try {
                        ReplicaComms repComms = (ReplicaComms) registry.lookup(name);
                        repComms.setAuctions(auctionItemsById);
                        repComms.setBidDetails(winBidDetails);
                        repComms.setUsers(users);
                        System.out.println("Data is transferred.");
                    } catch (Exception e) {
                        System.out.println("Unable to transfer some of the data!");
                    }
                } 
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper methods called in the updateAll()
     */
    @Override
    public void setBidDetails(List<AuctionDetails> detailsRep) throws RemoteException{
        winBidDetails = new ArrayList<AuctionDetails>(detailsRep);
    }

    @Override
    public void setUsers(HashMap<Integer, String> usersRep) throws RemoteException{
        users = new HashMap<Integer, String>(usersRep);
    }

    @Override
    public void setAuctions(HashMap<Integer, Integer> auctionsRep) throws RemoteException{
        auctionItemsById = new HashMap<Integer, Integer>(auctionsRep);
    }
}
