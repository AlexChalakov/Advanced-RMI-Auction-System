import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client{
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Client n");
            return;
        }

        int n = Integer.parseInt(args[0]);
        try {
            String name = "Auction";
            Registry registry = LocateRegistry.getRegistry("localhost");
            Auction server = (Auction) registry.lookup(name);

            AuctionItem result = server.getSpec(n);

            //decrypt result
            //byte[] keyInBytesFile = Files.readAllBytes(Paths.get("../keys/testKey.aes"));
            //SecretKey aesKey = new SecretKeySpec(keyInBytesFile,0,keyInBytesFile.length, "AES");

            //Cipher cipher;
            //cipher = Cipher.getInstance("AES");
            //cipher.init(Cipher.DECRYPT_MODE, aesKey);
            //AuctionItem item = (AuctionItem) result.getObject(cipher);
            //System.out.println(item);
            //System.out.println("result is " + item.itemID + "\n" + item.name
              //      + "\n" + item.description + "\n" + item.highestBid);
        }
        catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
}
