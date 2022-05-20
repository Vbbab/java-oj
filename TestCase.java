
import java.io.IOException;
import java.lang.String;

public class TestCase {

   private String input;
   private String output;

   public TestCase(int p, int t) throws IOException {
      input = Constants.readFile("testcase/" + p + "/" + t + ".in") + "\n";
      output = Constants.readFile("testcase/" + p + "/" + t + ".out");
      // input = Constants.readFile("testcase/"+ p + "/" + t+".txt");
      // System.out.println(input);
   }

   public String in() {
      return input;
   }

   public String out() {
      return output;
   }

   public static void main(String args[]) throws IOException {
      TestCase obj = new TestCase(1, 1);
      //TestCase obj2 = new TestCase(1, 2);
      //System.out.println(obj.getInput());
      // System.out.println();
      //System.out.println(obj.getOutput());
   }

}