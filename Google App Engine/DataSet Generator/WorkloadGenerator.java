import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WorkloadGenerator {

  private static final String CHARACTER_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private int RAND_STR_LENGTH;
  
  /* This method generates random string */
  public String generateRandomString(int j){
	  
	/* Two bytes are occupied by the line separator after each line therefore 
	random string lengths are defined as 98 bytes and 22 bytes instead of 100 and 24 bytes */ 
	  
    if (j == 10){  
      RAND_STR_LENGTH = 22;  
    }
        
    else {	  
      RAND_STR_LENGTH = 98;                         
    }
    StringBuffer randString = new StringBuffer();
    for(int i = 0; i < RAND_STR_LENGTH; i++) {
      int number = getRandNumber(); //call random number generator function
      char ch = CHARACTER_LIST.charAt(number);
      randString.append(ch); // append characters at random numbers
    }

    return randString.toString();
     
  }


/* This method generates random numbers */
 
 private int getRandNumber() {
   int rInt = 0;
   Random randGenerator = new Random(); //create a new random number generator
   rInt = randGenerator.nextInt(CHARACTER_LIST.length());
   if (rInt - 1 == -1) {
        return rInt;
    } else {
        return rInt - 1;
    }
  } 

 public static void main(String a[]){
	
	try {
	
      int findex = -1; int size;
      System.out.println("Dataset is getting generated");
      /* Iterate through an array of various file sizes */
      for(int farray = 0; farray < 6; farray++) {
        findex++;
       
      /* Vary number of files as per the different file sizes */  
        if (findex == 4) {	  
          size = 10;
    	}
      
    	else if (findex == 5) {
          size = 1;
    	}  
    	else {	  
    	  size = 100;
    	}
    	
        for (int numfiles = 0; numfiles < size; numfiles++) {
    	  int k = 0;
    	  
    	  /* assign random alpha-numerical file names of 10 characters long */
    	  WorkloadGenerator msr = new WorkloadGenerator();	  
    	  String fname = msr.generateRandomString(k); 
    	  String Str2 = fname.substring(0,10);
	      File file = new File(Str2 + ".txt");	
	      
	      int[] fsize = new int[] {1,10,100,1024,10240,102400,0};
	      int y = 0; 
	 
	      for (y = 0; y < fsize[findex]; y ++) {
	        for ( int i = 0 ; i < 11 ; i++) {
	          String fcontent = msr.generateRandomString(i);  
              
	          /* Check if file already exists */
              if (!file.exists()) {
 	            file.createNewFile();
 	          }
             
             /* Append data to the file if it already exits using file writer */
 	         FileWriter fwriter = new FileWriter(file,true);
 	         BufferedWriter bwriter = new BufferedWriter(fwriter);
 	         bwriter.write(fcontent + System.getProperty("line.separator"));
 	         bwriter.close();
	         }
	        }
    	  }
	    } 
	   }
      catch(IOException e) {  
	    e.printStackTrace();
      }
    }
 }