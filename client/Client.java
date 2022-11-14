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

            Scanner scanner = new Scanner(System.in);
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
                        int result1 = server.newUser(scanner.nextLine());
                        if(result1 == 0){
                            System.out.println("Error! User has not been created succesfully!");
                        }
                        System.out.println("User ID is: " + result1);
                        break;

                    case 2:
                        break;
                    case 3: 
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
                        break;
                    case 6:
                        break;
                    case 7:
                        System.out.println("Press any key to exit.");
                        scanner.nextLine();
                        Runtime.getRuntime().exec("clear");
                        break;
                }
            }
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}
