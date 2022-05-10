import java.io.*;
import java.util.*;
import java.util.concurrent.*; // Time limits stuff

/**
 * Core class for Judger
 * Utility class only 
 */
public class Judger {
    private Judger() {}

    /**
     * Runs the given binary, passing input from {@code in}. <br><br>
     * 
     * [TODO] add lang parameter, read conf? (Languages like Python require specific commands to exec code)
     * 
     * @param file  input file path (in tmp/bin)
     * @param in    input string
     * @return      output string from process
     * @throws Exception  One of either {@link TimeoutException} or {@link IOException}, or other exceptions depending on system errors
     */
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

        result = f.get(Constants.MAX_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        
        // Cleanup
        e.shutdownNow();
        stdout.close();
        stdin.close();
        p.destroy();

        return new String(result);
    }
}
