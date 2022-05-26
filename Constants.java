import java.io.*;
import java.util.*;
import org.json.*;

/**
  * Constants (e.g. paths) used throughout the code.
 */
public final class Constants {
  private Constants() {}

  /* PATHS */
  public static final String TEMP_DIR = "tmp/";
  public static final String SRC_DIR = TEMP_DIR + "src/";
  public static final String BIN_DIR = TEMP_DIR + "bin/";
  public static final String PROBLEMS_DIR = "problems/";
  public static final String CFG_PATH = "conf.json";

  /* TIME LIMIT */
  public static final int MAX_COMPILE_TIME = 5000;

  public static JSONObject CONFIG;

  /**
   * Helper method. Reads file at {@code path} relative to current dir (root of program).
   * @param path  Path relative to the program root dir.
   * @return      File contents.
   */
  public static final String readFile(String path) throws FileNotFoundException, IOException {
    File f = new File(path);
    if (!f.isFile()) throw new FileNotFoundException("File doesn't exist: " + path);
    Scanner s = new Scanner(f);
    // This method guarantees all line-breaks (and entire file content) will be preserved as-is. It also avoids loops.
    s.useDelimiter("\\Z");
    String contents = s.next();
    s.close();
    return contents;
  }

  /**
   * The following function initializes the {@code CONFIG} JSONObject. <br><br>
   * 
   * Call it in {@code main()} before attempting to read any configs.
   * 
   * @throws Exception  Could be file I/O related errors or {@code org.json} errors.
   */
  public static final void initConfig() throws Exception {
    CONFIG = loadJSON(CFG_PATH);
  }

  /**
   * Loads a specific JSON file into a {@link JSONObject}.
   * @param path  path to file, absolute or relative
   * @return      JSON object
   * @throws Exception
   */
  public static final JSONObject loadJSON(String path) throws Exception {
    String json = readFile(path);
    return new JSONObject(json);
  }
  
}