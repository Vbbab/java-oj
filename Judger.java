import java.io.*;
import java.util.concurrent.*; // Multithreading, also takes care of TLE stuff

import org.json.JSONException;

/**
 * Core class for Judger
 * Utility class only 
 */
public class Judger {
    private Judger() {} /* Static utility class */

    /**
     * Singleton class that can be used to get the state of the judger while
     * it is working.
     */
    public static class State {
        private State() {}

        private String state, result;


        /* Methods for Judger to use only */
        private void setState(String s) {
            state = s;
        }
        private void setResult(String s) {
            result = s;
        }

        public String getState() { return state; }
        public String getResult() { return result; }
    }

    public static State state = new State();

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
            throw tle;
        } catch (Exception other) {
            e.shutdownNow();
            p.destroy();
            throw other;
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
            e.shutdownNow();
            throw new Exception("tle compile");
        } catch (Exception other) {
            e.shutdownNow();
            throw other;
        }

        return file;
    }

    /**
     * Judge a problem. <br><br>
     * Note that <strong>all</strong> of the testcases will be judged right now, even if one
     * or more results in TLE or an exception (USACO-like behavior). Might add a config option
     * to turn this off. <br><br>
     * Note that this function doesn't return; however, it does modify the singleton {@code Judger.state}, which can be used
     * to obtain the results as well as progress info while the judger is working.
     * 
     * @param p  The {@link Problem} being judged.
     */
    public static void judge(Problem p, String srcPath, String lang) throws Exception {
        String bin = compile(srcPath, lang);
        String result = "";
        int id = p.getID(),
            tcount = p.getNumTC();

        for (int i = 0; i < tcount; i++) {
            TestCase t = p.getTestCase(i);
            try {
                String oPut = run(bin, t.in(), lang, 1000).stripLeading().stripTrailing();
                if (oPut.equals(t.out())) result += "*";
                else result += "x";
            } catch (TimeoutException tle) {
                result += "t";
            }
            state.setState(id + " " + (i + 1) + "/" + tcount);
        }
        state.setResult(result);
        state.setState("done");
    }
}
