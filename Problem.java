import java.util.*;
import java.io.File;

 // Represents a Problem which contains TestCases
 // Change Strings to TestCases
public class Problem {
     private int id;
     private ArrayList<TestCase> tc = new ArrayList<TestCase>();

     public Problem(int probID) throws Exception {
        id = probID;
         //for(int i = 0; i<cases.size(); i++){
         //    tc.add(cases.get(i));       
         //}
         File directoryPath = new File("testcase/" + id);
         int size = directoryPath.listFiles().length;
         
         // size is the # of files within a certain a problem --> should be even 
        if (size % 2 != 0) throw new Exception("Incorrect testcase data");
        
        for(int i = 1; i <= size / 2; i++) {
            tc.add(new TestCase(id, i));
        }
    }
     public int getID(){
         return id;
     }
     public int getNumTC(){
         // # of problems within testcase folder
         return tc.size();
     }
     public TestCase getTestCase(int i){
         return tc.get(i);
     }
     /*public static void main(String[] args){
        //Problem list = new Problem(45, ["a","b","c","d"]);
        ArrayList<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        Problem prob = new Problem(987654, list);
        System.out.println(prob.getID());
        System.out.println(prob.getNumTC());
        System.out.println(prob.getTestCase(0));
     }
     */
}
