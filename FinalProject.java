import java.io.*;
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
            System.out.println("[-] " + e.getClass().getCanonicalName() + ": Can't init config, check "
                    + Constants.CFG_PATH + ".");
            return;
        }

        System.out.println("==== java judge thing ====");

        // Scanner s = new Scanner(System.in);

        // System.out.print("File: ");
        // String file = s.nextLine();

        // System.out.print("Lang: ");
        // String lang = s.nextLine();

        // System.out.print("Prob: ");
        // int prob = s.nextInt();
        // s.close();

        // try {
        //     Problem p = new Problem(prob);
        //     // jeez multithreading all over the place
        //     ExecutorService exec = Executors.newFixedThreadPool(2);
        //     Callable<Void> judgeTask = new Callable<Void>() {
        //         @Override
        //         public Void call() {
        //             try {
        //                 Judger.judge(p, file, lang);
        //             } catch (Exception e) {
        //                 System.out.println(e.getMessage());
        //             }
        //             return null;
        //         }
        //     };

        //     exec.submit(judgeTask);
        //     int currState = Judger.state.getState();
        //     while (currState != p.getNumTC() || Judger.state.getResult() == "") {
        //         System.out.print(prob + " " + currState + "/" + p.getNumTC() + " \r");
        //         currState = Judger.state.getState();
        //     }
        //     exec.shutdown();
        //     System.out.println(currState + " ");
        //     System.out.println("\nResult=============");
        //     System.out.println(Judger.state.getResult());
        // } catch (Exception e) {
        //     System.out.println(e.toString());
        // }

        Server.serve(8080);
        System.out.println("[OK] Server up on port 8080, type 'q' and hit enter to stop...");
        Scanner s = new Scanner(System.in);
        while(!(s.next().equals("q")));
        s.close();
        Server.stop();
        System.out.println("Server stopped.");

        System.out.print("Cleaning up... ");
        File binDir = new File(Constants.BIN_DIR);
        for (String binFile : binDir.list()) {
            File currFile = new File(binDir.getPath(), binFile);
            currFile.delete();
        }
        File srcDir = new File(Constants.SRC_DIR);
        for (String srcFile : srcDir.list()) {
            File currFile = new File(srcDir.getPath(), srcFile);
            currFile.delete();
        }
        System.out.println("[ok]");
        System.exit(0);
    }

    // Server.serve(8000);
    // System.out.println("Everything good! Serving at port 8080");
}
// }