import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class FrontEnd implements Auction {
    private Auction stub;
    private ArrayList<Auction> servers;

    @Override
    public NewUserInfo newUser(String email) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.newUser(email);
    }

    @Override
    public byte[] challenge(int userID) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.challenge(userID);
    }

    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.authenticate(userID, signature);
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.getSpec(itemID);
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.newAuction(userID, item);
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.listItems();
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.closeAuction(userID, itemID);
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.bid(userID,itemID,price);
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        Replica replica = getPrimaryReplica();
        return replica.getPrimaryReplicaID();
    }

    private Replica getPrimaryReplica() {
        servers = getServerList();
        Replica r = new Replica();
        for (int i = 0; i < servers.size(); i++) {
            Auction server = servers.get(i);
            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                stub = server;
                registry.rebind("Auction", stub);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    private ArrayList<Auction> getServerList() {
        ArrayList<Auction> serverList = new ArrayList<Auction>();
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            for(String name : registry.list()){
                try {
                    Auction stub = (Auction) registry.lookup(name);
                    serverList.add(stub);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverList;
    }
    
}
