import java.io.*;
import java.util.*;
import java.util.concurrent.*; // Time limits stuff

import org.json.JSONException;

/**
 * Core class for Judger
 * Utility class only 
 */
public class Judger {
    private Judger() {}

    /* Own private methods, undocumented, just for use in code */
    private static String getCompileCommand(String lang) throws JSONException {
        return Constants.CONFIG.getJSONObject(lang).getString("compile");
    }

    private static String getRunCommand(String lang) throws JSONException {
        return Constants.CONFIG.getJSONObject(lang).getString("run");
    }

    /**
     * Runs the given binary, passing input from {@code in}. <br><br>
     * code)
     * 
     * @param file  [COMPILED] file path (in {@code tmp/bin})
     * @param time  [ms]
     * 
     * @return      output string from process
     * @throws Exception  One of either {@link TimeoutException} or {@link IOException}, or other exceptions depending on system errors
     */
    public static String run(String file, String in, String lang, int time) throws Exception {
        File fileObj = new File(Constants.BIN_DIR + file);
        if (!fileObj.isFile()) return "[file doesn't exist!]";
        String filePath = fileObj.getAbsolutePath();
        
        String command = "";
        try {
            command = getRunCommand(lang).replace("%f", filePath);
        } catch(JSONException jerr) {
            throw new Exception("[lang unsupported]");
        }
        Process p = Runtime.getRuntime().exec(command);
        OutputStream stdin = p.getOutputStream();
        InputStream stdout = p.getInputStream();
        stdin.write(in.getBytes());
        stdin.flush();

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
            result = f.get(time, TimeUnit.MILLISECONDS);
        } catch(TimeoutException tle) {
            e.shutdownNow();
            // In some cases when TLE occurs the I/O streams are hung and we can't close them.
            // That's fine, just kill the process itself:
            p.destroy();
            return "[TLE]";
        }
        // Cleanup
        e.shutdownNow();
        stdout.close();
        stdin.close();
        p.destroy();

        return new String(result);
    }

    public static String compile(String file, String lang) throws Exception {
        File fileObj = new File(Constants.SRC_DIR + file);
        if (!fileObj.isFile()) throw new FileNotFoundException("Source file doesn't exist");
        String filePath = fileObj.getAbsolutePath();

        // Compiling is a lot simpler:
        final String command;
        try {
            command = getCompileCommand(lang).replace("%f", filePath);
        } catch(JSONException e) {
            throw new Exception("[lang not supported]");
        }

        // We might get a TLE during compilation. So, we'll *still* need multithreading to handle it:

        Callable<Void> compileTask = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Runtime.getRuntime().exec(command).waitFor();
                return null;
            }
        };

        ExecutorService e = Executors.newFixedThreadPool(2);
        Future<Void> future = e.submit(compileTask);
        try {
            future.get(Constants.MAX_COMPILE_TIME, TimeUnit.MILLISECONDS);
        } catch (TimeoutException tle) {
            throw new Exception("[tle compilation]");
        }

        return file;
    }
}
