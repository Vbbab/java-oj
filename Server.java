import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import io.javalin.*;
import io.javalin.http.*;
import io.javalin.http.staticfiles.*;

/**
 * Server class, responsible for serving content & handling judger client requests
 */
public class Server {
    private Server() {}

    private static Javalin server;

    private static Map<Integer, Judger.State> jobQueue;
    private static int JOB_CTR = 0; // Generate a unique Job ID for each request

    private static class HandleFileTask implements Callable<Void> {
        private UploadedFile f;
        private Judger.State s;
        private int probID;
        private String l;
        public HandleFileTask(UploadedFile file, String lang, int prob, Judger.State state) {
            f = file;
            s = state;
            l = lang;
            probID = prob;
        }

        @Override
        public Void call() {
            // Save
            try {
                Files.copy(f.getContent(), new File(Constants.SRC_DIR + f.getFilename()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                Judger.judge(new Problem(probID), f.getFilename(), l, s);
            } catch (Exception e) {
                if (s.getResult().isEmpty()) s.setResult("E_SERVER");
            }
            return null;
        }
    }

    public static void serve(int port) {
        // Set up job queue
        jobQueue = new HashMap<Integer, Judger.State>();
        
        server = Javalin.create(config -> {
            config.addStaticFiles(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "public";
                staticFiles.location = Location.EXTERNAL;
                staticFiles.precompress = false;
            });
        }).start(port);


        /**
         * [POST]
         * Source code submit endpoint.
         * 
         * Requires:
         * - Single uploaded file (source file)
         * - lang: language
         * - prob: problem ID
         */
        server.post("/submit", ctx -> {
            List<UploadedFile> files = ctx.uploadedFiles();
            if (files.isEmpty()) {
                ctx.result("E_NO_FILE"); return;
            }
            int prob = 0;
            try {
                prob = Integer.parseInt(ctx.formParam("prob"));
            } catch (Exception ex) {
                ctx.result("E_INVALID_PROBLEM"); return;
            }
            String lang = ctx.formParam("lang");
            if (lang == null) {
                ctx.result("E_NO_LANG"); return;
            }
            
            UploadedFile srcFile = files.get(0);
            // Stop here. We'll hand it off to a thread.
            // First, set up a state...
            Judger.State state = new Judger.State();
            jobQueue.put(JOB_CTR, state);
            Executors.newFixedThreadPool(2).submit(new HandleFileTask(srcFile, lang, prob, state));
            ctx.result(Integer.toString(JOB_CTR++));
        });

        /**
         * Gets the status of a running job.
         * Params:
         * - job: value given by a call to [POST /submit] to request a new job
        */
        server.get("/status", ctx -> {
            int job = 0;
            try {
                job = Integer.parseInt(ctx.queryParam("job"));
            } catch (Exception ex) {
                // uhhh ok
                ctx.result(""); return;
            }

            Judger.State jobState = jobQueue.get(job);
            if (jobState == null) {
                ctx.result(""); return;
            }

            String result = jobState.getResult();
            int progress = jobState.getState();
            if (!(result.isEmpty())) ctx.result(result);
            else ctx.result(progress + " " + jobState.getTotal());
        });
    }
    

    public static void stop() {
        server.stop();
    }
}
