import java.io.*;
import java.util.*;
import java.util.concurrent.*; // Time limits stuff

/**
 * Core class for Judger
 * Utility class only 
 */
public class Judger {
    private Judger() {}

    public static String run(String file, String in) throws Exception {
        Process p = Runtime.getRuntime().exec(Constants.BIN_DIR + file);
        OutputStream stdin = p.getOutputStream();
        InputStream stdout = p.getInputStream();
        stdin.write(in.getBytes());

        Callable<byte[]> readTask = new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return stdout.readAllBytes();
            }
        };

        ExecutorService e = Executors.newFixedThreadPool(2); // Create new thread

        Future<byte[]> f = e.submit(readTask);

        byte[] result = {0x00};

        try {
            result = f.get(Constants.MAX_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } 
        catch(TimeoutException tException) {
            System.out.println("[TIMED OUT AFTER " + Constants.MAX_TIMEOUT_MILLIS / 1000 + " SECS]");
        }
        
        // Cleanup
        e.shutdownNow();
        stdout.close();
        stdin.close();
        p.destroy();

        return new String(result);
    }
}
