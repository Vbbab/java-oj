import java.io.*;
import java.util.*;
import java.util.Map.*;
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

        private int numJobsComplete = 0;
        private String result = "";

        /* Methods for Judger to use only */
        private void incState() { numJobsComplete++; }
        private void setResult(String r) { result = r; }

        public int getState() { return numJobsComplete; }
        public String getResult() { return result; }
    }

    public static State state = new State();

    /**
     * Represents a single testcase runner job. Returns a pair (or, in java, a map Entry)
     * that contains a boolean (whether not the program TLE'd), and a String (the result if no TLE).
     */
    private static class RunnerJob implements Callable<Entry<Boolean, String>> {
        private String f, i, l;
        private int timeLimit;

        public RunnerJob(String file, String in, String lang, int time) {
            f = file;
            i = in;
            l = lang;
            timeLimit = time;
        }

        @Override
        public Entry<Boolean, String> call() {
            Entry<Boolean, String> output;
            try {
                output = Map.entry(true, Judger.run(f, i, l, timeLimit));
            } catch (Exception e) {
                output = Map.entry(false, "");
            }
            Judger.state.incState();
            return output;
        }
    }

    /* Own private methods, undocumented, just for use in code */
    private static String getCompileCommand(String lang) throws JSONException {
        return Constants.CONFIG.getJSONObject(lang).getString("compile");
    }

    private static String getRunCommand(String lang) throws JSONException {
        return Constants.CONFIG.getJSONObject(lang).getString("run");
    }

    private static int getTimeFactor(String lang) throws JSONException {
        return Constants.CONFIG.getJSONObject(lang).getInt("tf");
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
        if (!fileObj.isFile()) throw new FileNotFoundException("Source file doesn't exist: no " + fileObj.getAbsolutePath());
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
        String bin;
        try {
            bin = compile(srcPath, lang);
        } catch(Exception compErr) {
            state.setResult("[tle compilation]");
            throw compErr;
        }

        int tcount = p.getNumTC();

        // Set up a thread for each testcase (potentially a bad idea in the future but it's probably a good one for now)
        ExecutorService jobPool = Executors.newFixedThreadPool(tcount + 1);
        ArrayList<Future<Entry<Boolean, String>>> jobs = new ArrayList<Future<Entry<Boolean, String>>>(tcount);
        for(int i = 0; i < tcount; i++) {
            TestCase t = p.getTestCase(i);
            jobs.add(jobPool.submit(new RunnerJob(bin, t.in(), lang, p.getTimeLimit() * getTimeFactor(lang))));
        }
        while (state.getState() != tcount) {
            // Do something. For some reason,
            // maybe since it's weird coding practice
            // if I don't put a line here the program just freezes.
            // perhaps it's because this loop gets optimized out by the compiler...
            System.out.print("");
        }
        String result = "";
        for(int i = 0; i < jobs.size(); i++) {
            Entry<Boolean, String> status = jobs.get(i).get();
            if (status.getKey()) {
                if (status.getValue().stripTrailing().stripLeading().equals(p.getTestCase(i).out())) result += "*";
                else result += "x";
            } else result += "t";
        }
        jobPool.shutdownNow();
        state.setResult(result);
    }
}
