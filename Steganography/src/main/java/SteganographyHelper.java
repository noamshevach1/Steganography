
/* Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class responsible for hiding text in image and extracting secret text from an image. 
 * */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;
import Exceptions.CompressException;
import Exceptions.DecompressException;
import Exceptions.InflaterException;
import Exceptions.MessageNotFound;
import Exceptions.OutputStreamException;

public class SteganographyHelper {
	
	public static double secretMessageMaxCapacity = 0.9;
	private static int hit, miss = 0;
	private static final int START_DELIMITER = 0x24242424,  END_DELIMITER = 0x23232323;
	
	/*
	 * This function embed textToHide in the image that can be located at imagePath.
	 * The message will be compressed if isCompressed is true.
	 * The message will be encrypted with 'key' and 'IV'.
	 * Start delimiter and end delimiter are concatenated to the start and the end of the message respectively.
	 * Embedding is done via hideText function.
	 * 
	 * */
	public static byte[] embedText(byte[] textToHide, String imagePath, byte[] key, byte[] IV, boolean isCompressed) 
			throws OutputStreamException, CompressException, IOException  {
		
		if(isCompressed) 
			textToHide = Compress.compress(textToHide);
		byte[] encrypted = Crypt.encryptUsingAES(textToHide, key, IV);
		BufferedImage bi = ImageIO.read(new File(imagePath));
        byte[] imageBytes = imageToByteArray(bi, "bmp");
        
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			outputStream.write("$$$$".getBytes());
			outputStream.write(encrypted);
			outputStream.write("####".getBytes()); // Delimiter
			byte[] messegeToHide = outputStream.toByteArray( );
			imageBytes = hideText(messegeToHide, imageBytes, getSeed(key));
			return imageBytes;
			
		} 
		catch (IOException e) { throw new OutputStreamException();}
	}
	
	/* 
	 * This function hide 'text' in 'image'.
	 * The index location are picked using psuedo number generator LFSR that gets the 'seed' value.
	 * The function returns the image with the secret message.
	 * */
	private static byte[] hideText(byte[] text, byte[] image, int seed) {
		int imageIndex = 0, textIndex = 0;	
		boolean[] availableIdx = new boolean[image.length];
		Random rand = new Random();
		byte lastByteVisited = 0;
		int imageBit, textBit;
		LFSR lfsr = new LFSR(seed, 24);
		int indexLength =(int) (Math.floor(  (Math.log(image.length) / Math.log(2))));
		hit = 0; miss = 0;
		
		while(textIndex < text.length) {
			for(int i = 0; i < 8; i++) {	
				do {
				imageIndex = lfsr.generate(indexLength);
				//if(!(textIndex == 0 && i == 0) && imageIndex > 1000)
				//	imageIndex = changeLeadingBits(imageIndex , lastByteVisited);	
				if(availableIdx[imageIndex])
					miss++;
				else
					hit++;
				} while(!((image.length - 1) > imageIndex && !availableIdx[imageIndex] && imageIndex > 1000) );
				availableIdx[imageIndex] = true;
				
				imageBit = ((image[imageIndex] & 0xFF) & 0x1);
				textBit = ((text[textIndex] & 0xFF) & 0x80) >> 7;
				if(imageBit != textBit) {
					if(rand.nextInt(1) == 1)
						image[imageIndex]++;
					else
						image[imageIndex]--;
				}
				text[textIndex] <<= 1;
				lastByteVisited = image[imageIndex];
			}
			textIndex++;
		}
		return image;
	}
	
	/*
	 * This function returns the success rate of pseudo random index on the first iteration.
	 * */
	public static double getFirstTryHitSuccessRate() {
		return (double)hit / (double)(hit + miss);
	}
		
	
	/*
	 * This function is searching for a secret message in the image that can be found on 'imagePath'.
	 * In case secret message was found the function returns it.
	 * Otherwise, throws an exception. 
	 * */
	public static String extractText(String imagePath, byte[] key, byte[] IV, boolean isCompressed) 
			throws DecompressException, IOException, MessageNotFound, InflaterException {
		
        BufferedImage bi = ImageIO.read(new File(imagePath));
        byte[] image = imageToByteArray(bi, "bmp");
        
		int imageIndex = 0, isDelimiter = 0, messageLength = 0;
		byte temp = 0, lastByteVisited = 0;
		boolean[] availableIdx = new boolean[image.length];
		boolean isFirstIdx = true;
		ByteBuffer hiddenMessage = ByteBuffer.allocate(image.length);
		LFSR lfsr = new LFSR(getSeed(key), 24);
		int indexLength =(int) (Math.floor(  (Math.log(image.length) / Math.log(2))));
		byte imageBit;
		
		while(true) {
			for(int i = 0; i < 8; i++) {
				do {
				imageIndex = lfsr.generate(indexLength);
				//if(!isFirstIdx)
				//	imageIndex = changeLeadingBits(imageIndex , lastByteVisited);	

				}while(!((image.length - 1) > imageIndex && !availableIdx[imageIndex] && imageIndex > 1000));
				availableIdx[imageIndex] = true;
				isFirstIdx = false;
				
				isDelimiter <<= 1;
				temp <<= 1;
				imageBit = (byte) (image[imageIndex] & 0x1);
				if( imageBit == 1) {
					isDelimiter ^= 0x1;
					temp ^= 0x01;
				}
				lastByteVisited = image[imageIndex];
			}
			hiddenMessage.put(temp);
			messageLength++;
			if(messageLength == 4 ) {
				if(isDelimiter != START_DELIMITER) 
					throw new MessageNotFound();
			}

			if(isDelimiter == END_DELIMITER) {
				byte[] content = Arrays.copyOfRange(hiddenMessage.array(), 4, messageLength - 4);
				byte[] plain = Crypt.decryptUsingAES(content, key, IV);
				if(isCompressed) 
					plain = Compress.decompress(plain);
				String result = new String(plain, StandardCharsets.UTF_8);
				System.out.println(result);
				return result;
			}
		}
	}
	
	/*
	 * This function calculate the seed value from the key that was given.
	 * */
	private static int getSeed(byte[] key) {
		byte[] seed = new byte[3];
		for(int i = 0; i < seed.length; i++)
			seed[i] = key[i];
		BigInteger bigI = new BigInteger(seed); 
		return bigI.intValue(); 
	}
	
	/*
	 * This function changes the first two bits of the variable 'rnd' according to the MSB and LSB of 'lastByteVisited'.
	 * */
	private static int changeLeadingBits(int rnd, byte lastByteVisited) {
		Random r = new Random();
		int newTwoBits = r.nextInt(4); 
		if(newTwoBits == 0)
			return rnd;
		//if((lastByteVisited & 0x80) != 0)
		//	newTwoBits ^= 0x1;
		//newTwoBits <<= 1;
		//if((lastByteVisited & 0x1) != 0)
		//	newTwoBits ^= 0x1;
		int temp = rnd,  counter = 0;
		System.out.println("before: " + rnd);
		while((temp & 0x80000000) == 0) {
			temp <<= 1;
			counter++;
		}
		if((30 - counter) > 0)
			newTwoBits <<= (30 - counter);
		int mask = ((int)(Math.pow(2, 30 - counter) - 1));
		rnd &= mask;
		rnd ^= newTwoBits;
		System.out.println("after: " + rnd);
		return rnd;
	}
	
	/*
	 * This function returns the image that in 'BufferedImage' as byte array
	 * */
    static byte[] imageToByteArray(BufferedImage bi, String format)
            throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ImageIO.write(bi, format, baos);
    	byte[] bytes = baos.toByteArray();
    	return bytes;
    }
}
