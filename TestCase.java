import java.io.File;
import java.io.IOException;
import java.util.Scanner;




public class TestCase {
   public static void main(String args[]) throws IOException {
      //Creating a File object for directory
      File directoryPath = new File("testcase/1.txt");
      //List of all files and directories
      //testcase/p/t.in
      File filesList[] = directoryPath.listFiles();
     // System.out.println("List of files and directories in the specified directory:");
     // for(File file : filesList) {
     //    System.out.println("File name: "+file.getName());
        
     // }
     Scanner sc = new Scanner(directoryPath);
     String contents = "";
     while (sc.hasNextLine()){
         contents += sc.nextLine() + "\n";
     }
     System.out.println(contents);
   }
}