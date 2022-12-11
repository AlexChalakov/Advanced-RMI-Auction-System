import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * FrontEnd for our application. It serves as a replica manager,
 * checks if they are alive and chooses which one is going to do the work.
 * If the primary replica dies, it replaces it with a new one on its place and continues working.
 */
public class FrontEnd implements Auction {
    private int replicaID = 1;

    public static void main(String[] args) {
        try {
            FrontEnd frontEnd = new FrontEnd();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(frontEnd, 0);
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(name, stub);
            System.out.println("FrontEnd is alive!");
        } catch (IOException e) {
            System.out.println("FrontEnd is dead.");
            e.printStackTrace();
        }
    }

    @Override
    public NewUserInfo newUser(String email) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction rep = getPrimaryReplica();
            return rep.newUser(email);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] challenge(int userID) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.challenge(userID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.authenticate(userID, signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AuctionItem getSpec(int itemID) throws RemoteException {
        Auction replica = getPrimaryReplica();
        return replica.getSpec(itemID);    
    }

    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.newAuction(userID, item);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public AuctionItem[] listItems() throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.listItems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.closeAuction(userID, itemID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            Auction replica = getPrimaryReplica();
            return replica.bid(userID, itemID, price);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the id of the replica thats primary. It gets called in the Replica.java in the form of an if statement.
     */
    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        try {
            checkAliveOrReplace("Replica" + replicaID);
            return replicaID;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get Primary Replica and return it as a way to designate which one is doing the operations.
     * @return rep
     */
    private Auction getPrimaryReplica() {
        ArrayList<Auction> list = getServerList();
        System.out.println("Server list size "+ list.size());
        try {
            System.out.println("Going through Primary replicas!");
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction rep = (Auction) registry.lookup("Replica"+replicaID);
            return rep;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fault Detection - checks if the designated replica is alive, if not - it replaces it with a new one.
     * @param name String of the replica
     * @return true or false
     * @throws RemoteException
     */
    private boolean checkAliveOrReplace(String name) throws RemoteException{
        Registry registry = LocateRegistry.getRegistry("localhost");
        try {
            Auction rep = (Auction) registry.lookup(name);
            rep.listItems();
            System.out.println("Alive");
            return true;
        } catch (Exception e) {
            System.out.println("Dead");
            try {
                registry.unbind(name);
                
                for (int i = 0; i < registry.list().length; i++) {
                    if((registry.list()[i]!=(name))&&(registry.list()[i].contains("Replica")==true)){
                        replicaID = Integer.parseInt(registry.list()[i].split("a")[1]);
                    }
                }
            } catch (NotBoundException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Get active servers
     * @return
     */
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
