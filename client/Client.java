import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client{
    public static void main(String[] args) {

        /*if (args.length < 1) {
            System.out.println("Usage: java Client n");
            return;
        }*/

        try {
            //challenge = byte[]
            //Authenticate(byte[])
            //Notes about encryption
            //Sign a message with out private key - challenge
            //and verify the signature with the public key - authenticate

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
                            Scanner scanner1 = new Scanner(System.in);
                            System.out.println("Please enter your email address:");
                            String email = scanner1.nextLine();
                            NewUserInfo result1 = server.newUser(email);
                            if(result1 == null){
                                System.out.println("Error! User has not been created succesfully!");
                                break;
                            }
                            byte[] signedMessage = server.challenge(result1.userID);
                            boolean authenticated = server.authenticate(result1.userID, signedMessage);
                            System.out.println("User ID is: " + result1.userID);
                            System.out.println("Signed Message: " + signedMessage);
                            System.out.println("Authentication: " + authenticated);
                            break;

                        case 2:
                            AuctionSaleItem auctionSaleItem = new AuctionSaleItem();
                            Scanner scanner2 = new Scanner(System.in);

                            System.out.println("Please enter your user ID.");
                            Integer userId = scanner2.nextInt();
                            System.out.println("Please enter the item's name.");
                            auctionSaleItem.name = scanner2.next();
                            System.out.println("Please enter the item's description.");
                            auctionSaleItem.description = scanner2.next();
                            System.out.println("Please enter the item's reserve price.");
                            auctionSaleItem.reservePrice = Integer.parseInt(scanner2.next());

                            int result2 = server.newAuction(userId, auctionSaleItem);
                            if(result2 == -1){
                                System.out.println("Error! Invalid user...");
                            } else {
                                System.out.println("New auction created, item ID is " + result2);
                            }
                            break;
                        case 3: 
                            AuctionItem[] result3 = server.listItems();
                            if(result3.length == 0){
                                System.out.println("No available items to list.");
                            }
                            for(int i = 0; i < result3.length; i++){
                                System.out.println("Auction's item ID is " + result3[i].itemID);
                                System.out.println("Auction's item name is " + result3[i].name);
                                System.out.println("Auction's item description is " + result3[i].description);
                                System.out.println("Auction's item highest bid is " + result3[i].highestBid);
                            }
                            break;
                        case 4:
                            Scanner scanner4 = new Scanner(System.in);
                            System.out.println("Enter an Auction ID:");
                            AuctionItem result4 = server.getSpec(scanner4.nextInt());
                            System.out.println("Item Name - " + result4.name);
                            System.out.println("Item ID - " + result4.itemID);
                            System.out.println("Item Description - " + result4.description);
                            System.out.println("Item's highest bid - " + result4.highestBid);
                            break;
                        case 5:
                            Scanner scanner5 = new Scanner(System.in);
                            System.out.println("Enter the owner ID: ");
                            int userOwner = scanner5.nextInt();;
                            System.out.println("Enter the auction ID: ");
                            int auctionID = scanner5.nextInt();;
                            AuctionCloseInfo result5 = server.closeAuction(userOwner, auctionID);
                            if(result5 == null){
                                System.out.println("Error:Auction does not exist or user has no permission to close it!");
                            } else {
                                System.out.println("Winning email: " + result5.winningEmail);
                                System.out.println("Winning price: " + result5.winningPrice);
                            }
                            break;
                        case 6:
                            Scanner scanner6 = new Scanner(System.in);
                            System.out.println("Enter the user ID: ");
                            int userID = scanner6.nextInt();;
                            System.out.println("Enter the auction ID: ");
                            int auctionId = scanner6.nextInt();;
                            System.out.println("Enter the bidding price: ");
                            int price = scanner6.nextInt();;
                            Boolean result6 = server.bid(userID, auctionId, price);
                            if(result6){
                                System.out.println("Bid is placed successfully!");
                            } else {
                                System.out.println("Error! Bid is not accepted!");
                            }
                            break;
                        case 7:
                            Scanner scanner7 = new Scanner(System.in);
                            System.out.println("Press any key to exit.");
                            scanner7.next();
                            System.exit(0);
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
