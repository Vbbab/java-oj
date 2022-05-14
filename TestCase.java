
import java.io.IOException;
import java.lang.String;


public class TestCase {
   
   private String input;
   private String output;

   public TestCase(int p, String t) throws IOException {
      if(t.equals("in")){
         input = Constants.readFile("testcase/"+ p + "/" + t+".txt");
      }
      else if(t.equals("out")){
         output = Constants.readFile("testcase/"+ p + "/" + t+".txt");
      }
      //input = Constants.readFile("testcase/"+ p + "/" + t+".txt");
      //System.out.println(input);   
   }
   public String getInput() {
      return input;
   }
   public String getOutput() {
      return output;
   }
   public static void main(String args[]) throws IOException{
      TestCase obj = new TestCase(1,"in");  
      TestCase obj2 = new TestCase(1, "out");    
      //System.out.println(obj.getInput());
      //System.out.println();   
      //System.out.println(obj2.getOutput());
   }

}