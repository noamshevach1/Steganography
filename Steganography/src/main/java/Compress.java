/* 
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class are responsible for compress and decompress messages.
 * */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import Exceptions.CompressException;
import Exceptions.DecompressException;
import Exceptions.InflaterException;


public class Compress {
	
	/*
	 * This function compress the byte array data via Deflater and returns it.
	 * */
	public static byte[] compress(byte[] data) 
			throws CompressException{
		byte[] output;
	    try {
			Deflater deflater = new Deflater();  
		    deflater.setInput(data);  
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);   

		    deflater.finish();  
		    byte[] buffer = new byte[1024];   
		    while (!deflater.finished()) { 
		    	int count = deflater.deflate(buffer); // returns the generated code... index
		    	outputStream.write(buffer, 0, count);
		    }
			outputStream.close();
		    output = outputStream.toByteArray();
		} 
	    catch (IOException e) { throw new CompressException(); }

	    return output;
	}
	
	/*
	 * This function decompress the byte array that is given and return the decompress result.
	 * */
	public static byte[] decompress(byte[] data) 
			throws DecompressException, InflaterException {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(data);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			byte[] buffer = new byte[1024];
			while(!inflater.finished()) {
				int count = inflater.inflate(buffer);
				if(count == 0)
					throw new InflaterException();
				outputStream.write(buffer, 0, count);
			}
			
			outputStream.close();
			byte[] output = outputStream.toByteArray();
			
			return output;
		} 
		catch (DataFormatException e) {throw new DecompressException();} 
		catch (IOException e) {throw new DecompressException(); }
	}
	
	/*
	 * This function returns a string that indicates the effectiveness of the compression on the given message.
	 * */
	public static String checkFeasibility(byte[] message) throws CompressException {
		int originalLength = message.length;
		int compressedLength = Compress.compress(message).length;
		
		if(compressedLength > originalLength) {
			return "Attention! \nCompressed message is larger than the original.\nOriginal:" + originalLength +" bytes\nCompressed:"+ compressedLength + " bytes \nIt is recommended to compress only large messages due to compression header.";
		}
		return "Original: " + originalLength + " bytes \nCompressed:" + compressedLength + " bytes";
		
	}
	  
}
