public class Replica {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Client n");
            return;
        }

        int n = Integer.parseInt(args[0]);
    }

    public NewUserInfo newUser(String email) {
        return null;
    }

    public byte[] challenge(int userID) {
        return null;
    }

    public boolean authenticate(int userID, byte[] signature) {
        return false;
    }

    public AuctionItem getSpec(int itemID) {
        return null;
    }

    public int newAuction(int userID, AuctionSaleItem item) {
        return 0;
    }

    public AuctionItem[] listItems() {
        return null;
    }

    public AuctionCloseInfo closeAuction(int userID, int itemID) {
        return null;
    }

    public boolean bid(int userID, int itemID, int price) {
        return false;
    }

    public int getPrimaryReplicaID() {
        return 0;
    }
}
