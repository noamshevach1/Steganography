/*
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class responsible for writing and reading from file.
 * 
 * */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class FileAccess {
	
	private static String saltFile = "src//main//resources//res1.txt" ;

	/*
	 * This function writes the imagePath and the salt as hex string to the file 'saltFile'.
	 * */
	public static void writeSaltToFile(byte[] image, byte[] salt) throws IOException, NoSuchAlgorithmException  {
		
		BufferedWriter myWriter = new BufferedWriter(new FileWriter(saltFile, true));
		myWriter.write(Crypt.sha256(image) + " " +  Crypt.hex(salt, false) );
		myWriter.close();	
	}
	
	/*
	 * This function returns the salt value of a given imagePath.
	 * */
	public static byte[] readSaltFromFile(byte[] image) throws FileNotFoundException, UnsupportedEncodingException, NoSuchAlgorithmException {
    	File pathAndSalt = new File(saltFile);
        Scanner myReader = new Scanner(pathAndSalt);
        String hashedImage = Crypt.sha256(image);
        while (myReader.hasNextLine()) {
          String input = myReader.nextLine();
          String[] s = input.split(" ");
          if(s[0].equals(hashedImage)) {
        	  myReader.close();     
        	  return hexStringToByteArray(s[1]);
          }
        }
        myReader.close();
	    return "0".getBytes(); // change
	}
	
	/*
	 * This function converts hex string to byte array.
	 * */
	private static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
