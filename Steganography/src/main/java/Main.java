import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		StegToolForm steg = new StegToolForm();
		
		/*byte[] plainText = {0x32, (byte) 0x43, (byte) 0xf6, (byte) 0xa8,
				(byte) 0x88, 0x5a, 0x30, (byte) 0x8d, 
				(byte) 0x31, 0x31, (byte) 0x98, (byte) 0xa2,
				(byte) 0xe0, (byte) 0x37, (byte) 0x07, 0x34};
		byte[] key = {0x2b, 0x7e, (byte) 0x15, 0x16,
				0x28, (byte) 0xae, (byte) 0xd2,(byte) 0xa6, 
				(byte) 0xab, (byte) 0xf7, 0x15, (byte) 0x88,
				0x09, (byte) 0xcf, (byte) 0x4f, 0x3c};
		byte[] IV = Crypt.getRandomNonce(Crypt.IV_LENGTH_BYTE);

		byte[] encrypted = Crypt.encryptUsingAES(plainText, key, IV);
		System.out.println(Crypt.hex(encrypted, false));*/	

		/*Crypt.seKeySize(AES.AES256);
		byte[] plainText = "It should be longer".getBytes("UTF-8");
		System.out.println("Plain text: " + Crypt.hex(plainText, false));
		byte[] key = "jds8aD9%%(098f751234567812345678".getBytes("UTF-8");
		byte[] IV = "ABCDEFGHIJKLMNOP1234567812345678".getBytes("UTF-8");
		byte[] encrypted = Crypt.encryptUsingAES(plainText, key, IV);
		System.out.println("Encrypted text:" + Crypt.hex(encrypted, false));*/
		
		//byte[] compressedData = Compress.compress(encrypted);
		//System.out.println(Crypt.hex(compressedData, false));
		
		/*LFSR rnd = new LFSR( 0x6a6473);
		for(int i = 0; i < 100000; i++)
			System.out.println(rnd.generate(16));*/
		
		//byte[] plainTextInBytes = Crypt.decryptUsingAES(encrypted, key, IV);
		//System.out.println(new String(plainTextInBytes, StandardCharsets.UTF_8));
		
		
		/*SteganographyHelper.embedText(plainText, "C:\\Users\\noams\\Desktop\\husky.bmp", key, IV);
		try {
			SteganographyHelper.extractText("C:\\Users\\noams\\eclipse-workspace\\Stenganography\\huskyWithMessage.bmp", key, IV);
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
}
