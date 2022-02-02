/*
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class is responsible for encryption and decryption.
 * */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

public class Crypt {
	
    public static int BLOCK_SIZE = 16;
    private static int ROUNDS = 9, ROWS_IN_BLOCK = 4, COLS_IN_BLOCK = 4, KEY_SIZE_BYTES = 16;
    private static byte[] sbox, rsbox;
    private static final int[] Rcon = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a};
    
    public static byte[] getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        // iterationCount = 65536
        KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_SIZE_BYTES * 8);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret.getEncoded();
    }
    
    /*
     * Returns a random 16 byte value.
     * */
    public static byte[] getRandomNonce() {
        byte[] nonce = new byte[BLOCK_SIZE];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }
    
    
    /*
     * This function changes KEY_SIZE_BYTES and ROUNDS according to the chosen key size.
     * */
    public static void setKeySize(AES type) {
    	if(type == AES.AES128) {
    		ROUNDS = 9;
    		KEY_SIZE_BYTES = 16;
    	}
    	if(type == AES.AES192) {
    		ROUNDS = 11;
    		KEY_SIZE_BYTES = 24;
    	}
    	if(type == AES.AES256) {
    		ROUNDS = 13;
    		KEY_SIZE_BYTES = 32;
    	}
    }
    
    /*
     * This function return the chosen key size.
     * */
    public static int getKeySize() {
    	return KEY_SIZE_BYTES;
    }
    
    /*
     * This function handles the CBC mode on the encryption phase.
     * 
     * */
	public static byte[] encryptUsingAES(byte[] plainText, byte[] key, byte[] IV) {
		byte[] padPlainText = padding(plainText);
		createSBOX();
		byte[] state = new byte[BLOCK_SIZE];  //plain text block
		byte[] extendKey = extendKey(key);
		byte[] encryptedData = new byte[padPlainText.length];
		byte[] encryptedBlock = new byte[BLOCK_SIZE];
		
		copySourceToDest(padPlainText, state, 0, 0);
		xorOperationBetweenBlocks(state, IV); 
		encryptBlock(state, extendKey);
		copySourceToDest(state, encryptedBlock, 0, 0);
		copySourceToDest(state, encryptedData, 0,0);
		
		int blocks = (int) Math.ceil(padPlainText.length / state.length);
		
		for(int i = 1; i < blocks; i++) {
			copySourceToDest(padPlainText, state, state.length * i,0); // source, dest, offset
			xorOperationBetweenBlocks(state, encryptedBlock);  // result in state
			encryptBlock(state, extendKey);
			copySourceToDest(state, encryptedBlock, 0, 0);
			copySourceToDest(state, encryptedData, 0, state.length * i);
		}
		return encryptedData;
	}
	
	
	/*
	 * This function prints the byte array as hex string.
	 * If isMatrixRepresentation is set to true then the output will be in the structure of matrix.
	 * 
	 * */
    public static String hex(byte[] bytes , boolean isMatrixRepresentation) {
    	if(isMatrixRepresentation)
    		bytes = invertMatrix(bytes);
        StringBuilder result = new StringBuilder();
        int counter = 0;
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
            counter++;
            if(isMatrixRepresentation && counter % COLS_IN_BLOCK == 0)
            	result.append("\n");
        	if(!isMatrixRepresentation && counter % BLOCK_SIZE == 0)
        		result.append("\n");
        	//if(!isMatrixRepresentation && counter % COLS_IN_BLOCK == 0)
        	//	result.append(" ");
        }
    	if(isMatrixRepresentation)
    		bytes = invertMatrix(bytes);
        return result.toString();
    }
    
    
    /*
     * This function invert the matrix (state byte array)
     * I use to think that the byte array is going throw column[0], column[1], etc...
     * In order to access the matrix in rows order it is useful to invert the matrix. (especially for shiftRows phase)
     * */
    private static byte[] invertMatrix(byte[] bytes) { 	
    	bytes = switchBetween(bytes, 1, 4);
    	bytes = switchBetween(bytes, 2, 8);
    	bytes = switchBetween(bytes, 3, 12);
    	bytes = switchBetween(bytes, 7, 13);
    	bytes = switchBetween(bytes, 11, 14);
    	bytes = switchBetween(bytes, 9, 6);
    	return bytes;
    }
    
    private static byte[] switchBetween(byte[] bytes, int a, int b) {
    	byte temp;
    	temp = bytes[a];
    	bytes[a] = bytes[b];
    	bytes[b] = temp;
    	return bytes;
    }
	
    
    /*
     * This function is responsible for encryption of one block.
     * */
	private static byte[] encryptBlock(byte[] state, byte[] extendKey ) {
		xorOperationBetweenBlocks(state, getRoundKey(extendKey, 0)); //addRoundKey
		//System.out.println(hex(state, true));
		for(int i = 0; i < ROUNDS; i++) {
			subBytes(state);
			//System.out.println(hex(state, true));
			shiftRows(state);
			//System.out.println(hex(state, true));
			mixColumns(state);
			//System.out.println(hex(state, true));
			xorOperationBetweenBlocks(state, getRoundKey(extendKey, i + 1)); //addRoundKey
			//System.out.println(hex(state, true));
		}
		subBytes(state);
		//System.out.println(hex(state, true));
		shiftRows(state);
		//System.out.println(hex(state, true));
		xorOperationBetweenBlocks(state, getRoundKey(extendKey, ROUNDS + 1)); //addRoundKey
		//System.out.println(hex(state, true));
		return state;
	}
	
	/*
	 * This function handles decryption in CBC mode.
	 * */
	public static byte[] decryptUsingAES(byte[] cipherText, byte[] key, byte[] IV) {
		createSBOX();
		byte[] state = new byte[BLOCK_SIZE];  //plain text block
		byte[] extendKey = extendKey(key);
		byte[] plainText = new byte[cipherText.length];
		byte[] lastCipherTextBlock = new byte [BLOCK_SIZE];
		copySourceToDest(cipherText, state, 0,0); //source, dest, offset
		copySourceToDest(cipherText, lastCipherTextBlock, 0,0);
		decryptBlock(state, extendKey);
		xorOperationBetweenBlocks(state, IV);
		copySourceToDest(state, plainText, 0,0);
		
		for(int i = 1; i < cipherText.length; i++) {
			copySourceToDest(cipherText, state, state.length * i,0); // source, dest, offset
			decryptBlock(state, extendKey);
			xorOperationBetweenBlocks(state, lastCipherTextBlock);  // result in state
			copySourceToDest(cipherText, lastCipherTextBlock, state.length * i,0);
			copySourceToDest(state, plainText, 0, state.length * i);
		}
		return removePadding(plainText);
	}
	
	/*
	 * This function remove the padding of the original message.
	 * */
	private static byte[] removePadding(byte[] plainText) {
		int paddingLength = plainText[plainText.length - 1];
		byte[] result = new byte[plainText.length - paddingLength];
		copySourceToDest(plainText, result, 0, 0);
		return result;
	}
	
	
	/*
	 * This function is responsible for decryption of one block.
	 * */
	private static byte[] decryptBlock(byte[] state, byte[] extendKey) {
		xorOperationBetweenBlocks(state, getRoundKey(extendKey, ROUNDS + 1)); //addRoundKey
		//System.out.println(hex(state, true));
		for(int i = ROUNDS; i > 0; i--) {
			iShiftRows(state);
			//System.out.println(hex(state, true));
			iSubBytes(state);
			//System.out.println(hex(state, true));
			xorOperationBetweenBlocks(state, getRoundKey(extendKey, i)); //addRoundKey
			//System.out.println(hex(state, true));
			iMixColumns(state);
			//System.out.println(hex(state, true));
		}
		iShiftRows(state);
		//System.out.println(hex(state, true));
		iSubBytes(state);
		//System.out.println(hex(state, true));
		xorOperationBetweenBlocks(state, getRoundKey(extendKey, 0)); //addRoundKey
		//System.out.println(hex(state, true));
		return state;
	}
	
	/*
	 * This function returns round key according to the value 'round', from 'extendKey' array.
	 * */
	public static byte[] getRoundKey(byte[] extendKey, int round) {
		byte[] key = new byte[BLOCK_SIZE];
		for(int i = 0 ; i < BLOCK_SIZE; i++) 
			key[i] = extendKey[(round * BLOCK_SIZE) + i];
		return key;
	}
	
	/*
	 * This function XOR between state and key arrays.
	 * */
	private static void xorOperationBetweenBlocks(byte[] state, byte[] key) {
		for(int i = 0; i < BLOCK_SIZE; i++)
			state[i] = (byte) ((state[i] & 0xFF) ^ (key[i] & 0xFF));
	}
	
	/*
	 * This function executes the subBytes phase for encryption.
	 * */
	private static void subBytes(byte[] state) {
		for(int i = 0; i < state.length; i++) 
			state[i] = sbox[state[i] & 0xFF];
	}
	
	
	/*
	 * This function executes the subBytes phase for decryption.
	 * */
	private static void iSubBytes(byte[] state) {
		for(int i = 0; i < state.length; i++) {
			state[i] = rsbox[state[i] & 0xFF];
		}
	}
	
	
	/*
	 * This function is responsible for shiftRows phase for encryption.
	 * */
	private static void shiftRows(byte[] state) {
		byte tmp;
		state = invertMatrix(state);
		for(int i = 0; i < ROWS_IN_BLOCK; i++) {
			for(int j = 0; j < i; j++) {
				tmp = state[i * COLS_IN_BLOCK];
				for(int k = 0; k < (COLS_IN_BLOCK - 1); k++) 
					state[(i * COLS_IN_BLOCK) + k] = state[(i * COLS_IN_BLOCK)+ k + 1];
				state[(i * COLS_IN_BLOCK) + (COLS_IN_BLOCK - 1)] = tmp;
			}
		}
		state = invertMatrix(state);
	}
	
	/*
	 * This function is responsible for shiftRows phase for decryption.
	 * */
	private static void iShiftRows(byte[] state) {
		byte tmp;
		state = invertMatrix(state);
		for(int i = 0; i < ROWS_IN_BLOCK; i++) {
			for(int j = 0; j < i; j++) {
				tmp = state[(COLS_IN_BLOCK - 1) + (i * COLS_IN_BLOCK)];
				for(int k = (COLS_IN_BLOCK - 1); k > 0; k--)
					state[(i * COLS_IN_BLOCK) + k] = state[(i * COLS_IN_BLOCK) + k -1];
				state[(i * COLS_IN_BLOCK)] = tmp;
			}
		}
		state = invertMatrix(state);
	}
	
	/*
	 * This function multiply A and B
	 * */
	private static byte Gmul(byte a, byte b) {
		byte p = 0; 
		boolean high;
		for (int i = 0; i < 8; i++) {
			if((b & 1) != 0)
				p ^= a;
			high =  (a & 0x80) != 0;
			a <<= 1;
			if(high)
				a ^= 0x1b;
			b >>= 1;
		}
		return p;
	}
	
	/*
	 * This function is responsible for mixColumns phase for encryption.
	 * */
	private static void mixColumns(byte[] state) {
		byte[] column = new byte[ROWS_IN_BLOCK];
		byte[] temp = new byte[ROWS_IN_BLOCK];
		for(int i = 0; i < COLS_IN_BLOCK; i++) {
			for(int j = 0; j < ROWS_IN_BLOCK; j++) 
				column[j] = state[(i * ROWS_IN_BLOCK) + j];
			for(int r = 0; r < ROWS_IN_BLOCK; r++) 
				temp[r] = column[r];
			column[0] = (byte) (Gmul(temp[0], (byte) 0x02) ^ Gmul(temp[1], (byte)0x03) ^ Gmul(temp[2], (byte) 0x01) ^ Gmul(temp[3], (byte)0x01));
			column[1] = (byte) (Gmul(temp[0], (byte) 0x01) ^ Gmul(temp[1], (byte)0x02) ^ Gmul(temp[2], (byte) 0x03) ^ Gmul(temp[3], (byte)0x01));
			column[2] = (byte) (Gmul(temp[0], (byte) 0x01) ^ Gmul(temp[1], (byte)0x01) ^ Gmul(temp[2], (byte) 0x02) ^ Gmul(temp[3], (byte)0x03));
			column[3] = (byte) (Gmul(temp[0], (byte) 0x03) ^ Gmul(temp[1], (byte)0x01) ^ Gmul(temp[2], (byte) 0x01) ^ Gmul(temp[3], (byte)0x02));
			for(int j = 0; j < ROWS_IN_BLOCK; j++)
				state[(i * ROWS_IN_BLOCK) + j] = column[j];
		}
	}
	
	/*
	 * This function is responsible for mixColumns phase for decryption.
	 * */
	private static void iMixColumns(byte[] state) {
		byte[] column = new byte[ROWS_IN_BLOCK];
		byte[] temp = new byte[ROWS_IN_BLOCK];
		for(int i = 0; i < COLS_IN_BLOCK; i++) {
			for(int j = 0; j < ROWS_IN_BLOCK; j++) 
				column[j] = state[(i * ROWS_IN_BLOCK) + j];
			for(int r = 0; r < ROWS_IN_BLOCK; r++) 
				temp[r] = column[r];
			column[0] = (byte) (Gmul(temp[0], (byte) 0x0E) ^ Gmul(temp[3], (byte)0x09) ^ Gmul(temp[2], (byte) 0x0D) ^ Gmul(temp[1], (byte)0x0B));
			column[1] = (byte) (Gmul(temp[1], (byte) 0x0E) ^ Gmul(temp[0], (byte)0x09) ^ Gmul(temp[3], (byte) 0x0D) ^ Gmul(temp[2], (byte)0x0B));
			column[2] = (byte) (Gmul(temp[2], (byte) 0x0E) ^ Gmul(temp[1], (byte)0x09) ^ Gmul(temp[0], (byte) 0x0D) ^ Gmul(temp[3], (byte)0x0B));
			column[3] = (byte) (Gmul(temp[3], (byte) 0x0E) ^ Gmul(temp[2], (byte)0x09) ^ Gmul(temp[1], (byte) 0x0D) ^ Gmul(temp[0], (byte)0x0B));
			for(int j = 0; j < ROWS_IN_BLOCK; j++)
				state[(i * ROWS_IN_BLOCK) + j] = column[j];
		}
	}
	
	
	/*
	 * This function responsible for extending the given key.
	 * */
    public static byte[] extendKey(byte[] key) {
        byte[] extendKey = new byte[BLOCK_SIZE * (ROUNDS + 2)];
        byte[] tmp = new byte[ROWS_IN_BLOCK];
        copyKeyToExtendedKey(key, extendKey);
        for (int i = (KEY_SIZE_BYTES / ROWS_IN_BLOCK); i < ((ROUNDS + 2) * COLS_IN_BLOCK); i++) {
            extendKeyCopyToTmp(tmp, extendKey, i);
            if (i % (KEY_SIZE_BYTES / ROWS_IN_BLOCK) == 0) {
                rotWord(tmp);
                subBytes(tmp);
                tmp[0] = (byte) ((tmp[0] & 0xFF) ^ (Rcon[(i / (KEY_SIZE_BYTES / ROWS_IN_BLOCK)) - 1]) );    // XOR between tmp (transformed w_(i-1) ) and Rcon[ i / 4]
            }else {
            	if(KEY_SIZE_BYTES == 32 && (i % 4 == 0))
            		subBytes(tmp);
            }
            tmpXorExtendKey(tmp, extendKey, i * ROWS_IN_BLOCK);                     // XOR between tmp ( w_(i - 1) ) and w_(i - 4)
        }
 
        return extendKey;
    }
    
    /*
     * This function copy the key to the extendedKey array to index 0-15
     * */
    private static void copyKeyToExtendedKey(byte[] key, byte[] extendedKey) {
    	for (int i = 0; i < KEY_SIZE_BYTES; i++) 
    		extendedKey[i] = key[i];
    }
    
    
    /*
     * This function rotates 4 byte array.
     * */
	private static void rotWord(byte[] state) {
		byte tmp = state[0];
		for(int i = 0; i < ROWS_IN_BLOCK - 1; i++) 
			state[i] = state[i + 1];
		state[(ROWS_IN_BLOCK - 1)] = tmp;
	}
 
	/*
	 * This function is doing XOR between the last column (w_(j-1))  and the column (w_(j-4))
	 * */
    private static void tmpXorExtendKey(byte[] tmp, byte[] extendKey, int currentIdx) {  // XOR between w_(j-1) = tmp     to   w_(j-4)
        int counter = currentIdx;
    	for (int i = 0; i < ROWS_IN_BLOCK; i++) {
            extendKey[counter++] = (byte) ((tmp[i] & 0xFF) ^ (extendKey[currentIdx - KEY_SIZE_BYTES + i] & 0xFF));
        }
    }
 
    /*
     * This function copy the next column from extendKey to tmp.
     * */
    private static void extendKeyCopyToTmp(byte[] tmp, byte[] extendKey, int j) {   //copy w_(j-1) to tmp
        int extKeyCurIdx = j * ROWS_IN_BLOCK;
    	for (int i = 0; i < ROWS_IN_BLOCK; i++) {
            tmp[i] = extendKey[extKeyCurIdx - ROWS_IN_BLOCK + i];
        }
 
    }
    
    /*
     * Gets byte array and returns hashed string (using SHA 256)
     * */
	public static String sha256(byte[] image) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(image);
		return new String(Hex.encode(hash));
	}
	
    /*
     * This function is responsible for padding the original message.
     * */
	private static byte[] padding(byte[] plainText) {
		//padding according to PKCS5 - https://www.cryptosys.net/pki/manpki/pki_paddingschemes.html
		int blockRemainder = plainText.length % BLOCK_SIZE;
		int numberOfBlocks = (plainText.length / BLOCK_SIZE) + 1;
		int paddingLength = BLOCK_SIZE - blockRemainder;
		byte[] padPlainText = new byte[numberOfBlocks * BLOCK_SIZE];
		copySourceToDest(plainText, padPlainText, 0,0);
		for(int i = 1; i <= paddingLength; i++) 
			padPlainText[(numberOfBlocks * BLOCK_SIZE) - i] = (byte)(paddingLength);
		return padPlainText;
	}
	
	/*
	 * This function copy the source array from 'srcIdx' index to the destination array starting at 'dstIdx'.
	 * */
	private static void copySourceToDest(byte[] source, byte[] dest, int srcIdx, int dstIdx) {
		int length = 0;
		if((source.length - srcIdx) < (dest.length - dstIdx) )
			length = source.length - srcIdx;
		else
			length = dest.length - dstIdx;
		
		for(int i = dstIdx; i < dstIdx + length; i++ ) 
			dest[i]= source[srcIdx++];
			
	}
	
	/*
	 * This function creates the SBOX table.
	 * */
    private static void createSBOX() {   
		int[] initTable = new int[256];
		initTable[0] = 1;
		for (int i = 1; i < 255; ++i) {
		   initTable[i] = (initTable[i - 1] << 1) ^ initTable[i - 1];
		   if ((initTable[i] & 0x100) > 255) {
		       initTable[i] ^= 0x11B;
		   }
		}
		
		int[] arcTable= new int[256];
		for (int i = 0; i < 255; i++) {
		   arcTable[initTable[i]] = i;
		}
		
		int[] s = new int[256];
		for (int i = 1; i < 256; i++) {
		   // k = g^a
		   int k = arcTable[i];
		   k = 255 - k;
		   k = k % 255;
		   s[i] = initTable[k];
		}
		   
		sbox = new byte[256];
		for (int i = 0; i < s.length; i++) {
		   sbox[i] = byteTransformation(s[i], 0x63);
		}
		
		rsbox = new byte[256];
		int row, col, result;
		for (int i = 1; i < rsbox.length; i++) {
		   row = (sbox[i] & 0xFF) >> 4;
	   	   col = (sbox[i]& 0xFF) & 0x0f;
	   	   result =   ((i / 16) << 4) ^ (i % 16);
	   	rsbox[row * 16 + col] = (byte) result;
		}
		
    }

	private static byte byteTransformation(int a, int x) {
		int[] tmp = new int[8];
		for (int i = 0; i < 8; i++) {
		   tmp[i] = (((a >> i) & 0x1) ^
		           ((a >> ((i + 4) % 8)) & 0x1) ^
		           ((a >> ((i + 5) % 8)) & 0x1) ^
		           ((a >> ((i + 6) % 8)) & 0x1) ^
		           ((a >> ((i + 7) % 8)) & 0x1) ^
		           ((x >> i) & 0x1)) << i;
		}
		return (byte) Arrays.stream(tmp).sum();
	}
	
}
