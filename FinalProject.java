import java.util.*;

/**
 * Main driver class
 */
public class FinalProject {

    public static void main(String[] args) {
        /* Load the config file */
        try {
            Constants.initConfig();
        } catch (Exception e) {
            System.out.println("[-] " + e.getClass().getCanonicalName() + ": Can't init config, check " + Constants.CFG_PATH + ".");
            return;
        }

        System.out.println("[OK] Loaded!");

        Scanner s = new Scanner(System.in);
        
        System.out.print("File: ");
        String file = s.nextLine();

        System.out.print("Lang: ");
        String lang = s.nextLine();

        System.out.print("Time limit [ms]: ");
        int tl = s.nextInt();
        s.nextLine(); // '\n' is left in buffer

        System.out.println("In: ");
        String input = "";
        while (s.hasNextLine()) input += (s.nextLine() + "\n");
        s.close();

        System.out.println("==========");
        System.out.println("Out:");
        try {
            System.out.println(Judger.run(file, input, lang, tl));
        } catch(Exception e) {
            System.out.println("[-] " + e.toString());
        }
    }

}