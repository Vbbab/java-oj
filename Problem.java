import java.util.*;

import org.json.JSONObject;

import java.io.File;

// Represents a Problem which contains TestCases
// Change Strings to TestCases
public class Problem {
    private int id;
    private ArrayList<TestCase> tc = new ArrayList<TestCase>();
    private int timeLimit;

    public Problem(int probID) throws Exception {
        id = probID;
        // for(int i = 0; i<cases.size(); i++){
        // tc.add(cases.get(i));
        // }
        File directoryPath = new File("testcase/" + id);
        JSONObject problemConfig = Constants.loadJSON(directoryPath.getPath() + "/conf.json");
        timeLimit = problemConfig.getInt("tl");

        int numTC = problemConfig.getInt("tc");
        for (int i = 1; i <= numTC; i++) {
            tc.add(new TestCase(id, i));
        }
    }

    public int getID() {
        return id;
    }

    public int getNumTC() {
        // # of testcases within testcase folder
        return tc.size();
    }

    public TestCase getTestCase(int i) {
        return tc.get(i);
    }

    public int getTimeLimit() {
        return timeLimit;
    }
    /*
     * public static void main(String[] args){
     * //Problem list = new Problem(45, ["a","b","c","d"]);
     * ArrayList<String> list = new ArrayList<String>();
     * list.add("a");
     * list.add("b");
     * list.add("c");
     * list.add("d");
     * Problem prob = new Problem(987654, list);
     * System.out.println(prob.getID());
     * System.out.println(prob.getNumTC());
     * System.out.println(prob.getTestCase(0));
     * }
     */
}
