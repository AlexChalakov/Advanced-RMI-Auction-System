public class WinningDetails {
    int winningID;
    AuctionItem auctionItem;
    int lastBetPrice;

    public WinningDetails(int winningID, AuctionItem auctionItem, int lastBetPrice) {
        this.winningID = winningID;
        this.auctionItem = auctionItem;
        this.lastBetPrice = lastBetPrice;
    }

    public WinningDetails(Integer winningID) {
        this.winningID = winningID;
    }

    public int getWinningID() {
        return winningID;
    }

    public AuctionItem getAuctionItem() {
        return auctionItem;
    }

    public double getLastBetPrice() {
        return lastBetPrice;
    }

    public void setWinningID(int winningID) {
        this.winningID = winningID;
    }

    public void setAuctionItem(AuctionItem auctionItem) {
        this.auctionItem = auctionItem;
    }

    public void setLastBetPrice(int lastBetPrice) {
        this.lastBetPrice = lastBetPrice;
    }
}
