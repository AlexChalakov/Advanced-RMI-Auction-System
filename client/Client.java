import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client{
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Client n");
            return;
        }

        try {
            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);
            //AuctionItem result = server.getSpec(n);

            try (Scanner scanner = new Scanner(System.in)) {
                while(true){
                    System.out.println("This is a client program, able to do the following things: \n" +
                    " 1. Create a new user. \n" + 
                    " 2. Create an auction. \n" +
                    " 3. List items. \n" + 
                    " 4. List a single auction item. \n" +
                    " 5. Close an auction. \n" + 
                    " 6. Bid on a auction. \n" + 
                    " 7. Exit. \n" +
                    " Enter the correpsonding key to the operation you want to be executed");
                    switch(scanner.nextInt()) {
                        case 1:
                            System.out.println("Please enter your email address:");
                            int result1 = server.newUser(scanner.nextLine());
                            if(result1 == -1){
                                System.out.println("Error! User has not been created succesfully!");
                            }
                            System.out.println("User ID is: " + result1);
                            break;

                        case 2:
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                            AuctionSaleItem auctionSaleItem = new AuctionSaleItem();

                            System.out.println("Please enter your user ID.");
                            Integer userId = bufferedReader.read();
                            System.out.println("Please enter the item's name.");
                            auctionSaleItem.name = bufferedReader.readLine();
                            System.out.println("Please enter the item's description.");
                            auctionSaleItem.description = bufferedReader.readLine();
                            System.out.println("Please enter the item's reserve price.");
                            auctionSaleItem.reservePrice = Integer.parseInt(bufferedReader.readLine());

                            int result2 = server.newAuction(userId, auctionSaleItem);
                            if(result2 == -1){
                                System.out.println("Error! Invalid user...");
                            } else {
                                System.out.println("New auction created, item ID is " + result2);
                            }
                            break;
                        case 3: 
                            AuctionItem[] result3 = server.listItems();
                            for(int i = 0; i < result3.length; i++){
                                System.out.println("Auction's item ID is " + result3[i].itemID);
                                System.out.println("Auction's item name is " + result3[i].name);
                                System.out.println("Auction's item description is " + result3[i].description);
                                System.out.println("Auction's item highest bid is " + result3[i].highestBid);
                            }
                            break;
                        case 4:
                            System.out.println("Enter an Auction ID:");
                            AuctionItem result4 = server.getSpec(scanner.nextInt());
                            System.out.println("Item Name - " + result4.name);
                            System.out.println("Item ID - " + result4.itemID);
                            System.out.println("Item Description - " + result4.description);
                            System.out.println("Item's highest bid - " + result4.highestBid);
                            break;
                        case 5:
                            BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(System.in));
                            System.out.println("Enter the owner ID: ");
                            int userOwner = bufferedReader2.read();
                            System.out.println("Enter the auction ID: ");
                            int auctionID = bufferedReader2.read();
                            AuctionCloseInfo result5 = server.closeAuction(userOwner, auctionID);
                            if(result5 == null){
                                System.out.println("Error! Auction does not exist!");
                            } else {
                                System.out.println("Winning email: " + result5.winningEmail);
                                System.out.println("Winning price: " + result5.winningPrice);
                            }
                            break;
                        case 6:
                            BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(System.in));
                            System.out.println("Enter the user ID: ");
                            int userID = bufferedReader3.read();
                            System.out.println("Enter the auction ID: ");
                            int auctionId = bufferedReader3.read();
                            System.out.println("Enter the bidding price: ");
                            int price = bufferedReader3.read();
                            Boolean result6 = server.bid(userID, auctionId, price);
                            if(result6){
                                System.out.println("Bid is placed successfully!");
                            } else {
                                System.out.println("Error! Bid is not accepted!");
                            }
                            break;
                        case 7:
                            System.out.println("Press any key to exit.");
                            scanner.nextLine();
                            Runtime.getRuntime().exec("clear");
                            break;
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}
