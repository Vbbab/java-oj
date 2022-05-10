import java.util.*;

/**
// TODO: implement TestCase
 */
 // Represents a Problem which contains TestCases
 // Change Strings to TestCases
public class Problem {
     private int id;
     private ArrayList<String> tc = new ArrayList<String>();

     public Problem(int probID, ArrayList<String> cases){
        id = probID;
         //for(int i = 0; i<cases.size(); i++){
         //    tc.add(cases.get(i));       
         //}
        tc = cases;
    }
     public int getID(){
         return id;
     }
     public int getNumTC(){
         return tc.size();
     }
     public String getTestCase(int i){
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
