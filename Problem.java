import java.util.*;


public class Problem{
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
     public static void main(String[] args){
        //Problem list = new Problem(45, ["a","b","c","d"]);
        

     }
}

/*public class CountingProblem extends Problem {
    public CountingProblem() {
        super(3, new ArrayList());
    }
}*/