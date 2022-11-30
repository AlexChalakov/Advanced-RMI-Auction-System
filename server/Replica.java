public class Replica {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java Client n");
            return;
        }

        int n = Integer.parseInt(args[0]);
    }
}
