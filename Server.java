import java.util.*;
import java.io.*;
import io.javalin.*;
import io.javalin.http.*;
import io.javalin.http.staticfiles.*;

/**
 * Simple web server. Nothing special here...
 */
public class Server {
    private Server() {}

    private static Javalin server;

    public static void serve(int port) {
        server = Javalin.create(config -> {
            config.addStaticFiles(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "public";
                staticFiles.location = Location.EXTERNAL;
                staticFiles.precompress = false;
            });
        }).start(port);

        /**Basic API endpoint tho */
        server.post("/mysubmitter", ctx -> {
            ctx.result(ctx.formParam("test"));
        });
    }
    
}
