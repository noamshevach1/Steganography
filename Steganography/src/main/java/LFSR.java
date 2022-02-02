/* Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class creates pseudo random number.
 * */
public class LFSR {

	private int seed, tap, headMark; 
	private int[] tapOptions = {0x1, 0x3, 0x3, 0x3, 0x5, 0x3, 0x3, 0x1D, 0x11, 0x9,
			0x5, 0x107, 0x27, 0x1007, 0x3, 0x100B, 0x9, 0x81,
			0x27, 0x9, 0x5, 0x3, 0x21, 0x087};
	
	public LFSR(int seed, int bitLength) {
		this.seed = seed;
		this.headMark = 1;
		
		headMark <<= (bitLength -1);
		this.tap = tapOptions[bitLength - 1];
	}
	
	/*
	 * This function generates k-th bit length pseudo random number.
	 * */
	public int generate(int k) {
		int inputBit = xorBetweenTaps();
		int result = 0;
		
		for(int i = 0; i < k; i++) {
			seed >>= 1;
			if(inputBit == 1)
				seed ^= headMark;
			result <<= 1;
			result = result ^ inputBit;
			inputBit = xorBetweenTaps();
		}
		return result;	
	}
	
	/* 
	 * This function creates new bit from XORing the values in the locations of 1's bits in tap.
	 * */
	private int xorBetweenTaps() {
		int onlyTaps = seed & tap;
		byte counter = 0;
		for(int i = 0; i < 24; i++) {
			if((onlyTaps & 0x01) == 1)
				counter++;
			onlyTaps >>= 1;
		}
		if(counter % 2 == 0) 
			return 0;
		return 0x01;
	}
	
}
