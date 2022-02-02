/*
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class deciding the strength of a givien password.
 * */
public class PasswordStrength {

	private String password;
	private int passwordScore;
	private int uppercaseLetters, lowercaseLetters,      //password characteristic
				numbers, symbols, middleNumbersOrSymbols, 
				requirements, repeatCharacters,
				consecutiveUppercase, consecutiveLowercase, consecutiveNumbers,
				sequentialLetters, sequentialNumbers, sequentialSymbols, spaces;
	private boolean isRequirements = false, numbersOnly = false, lettersOnly = false;
	
	public PasswordStrength(String password) {
		setPassword(password);
	}
	
	public PasswordStrength() {
		setPassword("a");
	}
	
 	private void initializeCharacteristic() {
		this.passwordScore = 0;
		this.uppercaseLetters = 0;
		this.lowercaseLetters = 0;
		this.numbers = 0;
		this.symbols = 0;
		this.middleNumbersOrSymbols = 0;
		this.requirements = 0;
		this.isRequirements = false;
		this.numbersOnly = false;
		this.lettersOnly = false;
		this.repeatCharacters = 0;
		this.consecutiveLowercase = 0;
		this.consecutiveNumbers = 0;
		this.consecutiveUppercase = 0;
		this.sequentialLetters = 0;
		this.sequentialNumbers = 0;
		this.sequentialSymbols = 0;
		this.spaces = 0;
	}
	
 	
 	/*
 	 * This function calculate the characteristics of the given password (except sequential characters).
 	 * */
	private void calculateCharacteristic() {
		boolean[] characters  = new boolean[127];
		CharacterType lastCharType = CharacterType.NONE;
		for(int i = 0; i < password.length(); i++) {
			if(password.charAt(i) == ' ') {
				spaces++;
				continue;
			}
			if(characters[(int)password.charAt(i)])
				this.repeatCharacters += 1;
			if(Character.isUpperCase(password.charAt(i))) {
				uppercaseLetters++;
				if(lastCharType == CharacterType.UPPERCASE)
					this.consecutiveUppercase++;
				lastCharType = CharacterType.UPPERCASE;
			}
			else {
				if(Character.isLowerCase(password.charAt(i))) {
					lowercaseLetters++;
					if(lastCharType == CharacterType.LOWERCASE)
						this.consecutiveLowercase++;
					lastCharType = CharacterType.LOWERCASE;	
				}
				else {
					if(Character.isDigit(password.charAt(i))) {
						numbers++;
						if(i > spaces && i != (password.length()-1))
							middleNumbersOrSymbols++;
						if(lastCharType == CharacterType.NUMBER)
							this.consecutiveNumbers++;
						lastCharType = CharacterType.NUMBER;
					}else {
						lastCharType = CharacterType.SYMBOL;
						symbols++;
						if(i > spaces && i != (password.length()-1))
							middleNumbersOrSymbols++;
						
					}
				}
			}
			characters[(int)password.charAt(i)] = true;
		}
		checkRequirements();
	}
	
	/*
	 * This function checks if there is sequential characters.
	 * */
	private void sequentialCharacters() {
		int lastCharAscii = -1, ascSequence = 0, decSequence = 0;
		char lastChar = ' ';
		String specialChars = "!@#$%^&*()";
		for(int i = 0; i < password.length(); i++) {
			if(password.charAt(i) == lastCharAscii + 1 && decSequence == 0) {
				if(Character.isLetter(password.charAt(i)) && Character.isLetter(lastCharAscii)) {
					ascSequence++;
					if(ascSequence > 1)
						this.sequentialLetters++;
				}
				else {
					if(Character.isDigit(password.charAt(i)) && Character.isDigit(lastCharAscii)) {
						ascSequence++;
						if(ascSequence > 1)
							this.sequentialNumbers++;
					}
				}
			}
			else {
				if(password.charAt(i) == lastCharAscii - 1 && ascSequence == 0) {
					if(Character.isLetter(password.charAt(i)) && Character.isLetter(lastCharAscii)) {
						decSequence++;
						if(decSequence > 1)
							this.sequentialLetters++;
					}
					if(Character.isDigit(password.charAt(i)) && Character.isDigit(lastCharAscii)) {
						decSequence++;
						if(decSequence > 1)
							this.sequentialNumbers++;
					}
				}
				else {
					if(specialChars.contains("" + lastChar + password.charAt(i))) {
						ascSequence++;
						if(ascSequence > 1)
							this.sequentialSymbols++;
					}
					else
						if(specialChars.contains("" + password.charAt(i) + lastChar)) {
							decSequence++;
							if(decSequence > 1)
								this.sequentialSymbols++;
						}
						else {
							ascSequence = 0;
							decSequence = 0;
						}
				}
			}
			lastCharAscii = password.charAt(i);
			lastChar = password.charAt(i);
		}
	}
	
	/*
	 * This function checks if the requirements are met and update the variables isRequirement and requirements. 
	 * */
	private void checkRequirements() {
		if(this.uppercaseLetters != 0)
			this.requirements++;
		if(this.lowercaseLetters != 0)
			this.requirements++;
		if(this.numbers != 0)
			this.requirements++;
		if(this.symbols != 0)
			this.requirements++;
		if(this.password.length() >= 8 && this.requirements >= 3) {
			this.isRequirements = true;
			this.requirements++;
		}
		else
			this.isRequirements = false;
	}
	
	/*
	 * This function returns the given score of the password.
	 * */
	public int getScore() {
		if(this.passwordScore > 99)
			return 100;
		if(this.passwordScore < 0)
			return 0;
		return this.passwordScore;
	}
	
	/*
	 * This function gets new password and caculates the score of it.
	 * */
	public void setPassword(String password) {
		this.password = password;
		initializeCharacteristic();
		calculateCharacteristic();
		sequentialCharacters();
		calculateScore();
	}
	
	/*
	 * This function calculates the password's score.
	 * */
	private void calculateScore() {
		passwordScore += (password.length() * 4);
		if(this.uppercaseLetters > 0)
			passwordScore += (password.length() - this.uppercaseLetters) * 2;
		if(this.lowercaseLetters > 0)
			passwordScore += (password.length() - this.lowercaseLetters) * 2;
		passwordScore += (this.numbers * 4);
		passwordScore += (this.symbols * 6);
		passwordScore += (this.middleNumbersOrSymbols * 2);
		if(this.isRequirements)
			this.passwordScore += (this.requirements * 2);
		
		if(this.numbers == (this.password.length() - spaces)) {
			this.passwordScore -= this.password.length();
			this.numbersOnly = true;
		}else
			this.numbersOnly = false;
		if((this.uppercaseLetters + this.lowercaseLetters) == (this.password.length() - spaces)) {
			this.passwordScore -= this.password.length();
			this.lettersOnly = true;
		}else
			this.lettersOnly = false;
		this.passwordScore -= this.repeatCharacters;
		this.passwordScore -= (this.consecutiveLowercase * 2);
		this.passwordScore -= (this.consecutiveNumbers * 2);
		this.passwordScore -= (this.consecutiveUppercase * 2);
		this.passwordScore -= (this.sequentialLetters * 3);
		this.passwordScore -= (this.sequentialNumbers * 3);
		this.passwordScore -= (this.sequentialSymbols * 3);
	}
	
	
	/*
	 * This function return count column for UI display.
	 * */
	public String[] getCountColumn() {
		String[] count = {password.length()+ "", uppercaseLetters+ "",lowercaseLetters + "",
				numbers + "", symbols + "", this.middleNumbersOrSymbols + "", this.requirements + "",
				this.lettersOnly? "" + this.password.length():"0", 
						this.numbersOnly? "" + this.password.length():"0", this.repeatCharacters + "",
						this.consecutiveUppercase + "", this.consecutiveLowercase + "",
						this.consecutiveNumbers + "", this.sequentialLetters + "", this.sequentialNumbers + "",
						this.sequentialSymbols + ""};
		return count;
	}
	
	/*
	 * This function return bonus column for UI display.
	 * */
	public String[] getBonusColumn() {
		String[] bonus = {password.length() > 0 ? "+" + (password.length() * 4): "0", 
				(password.length() - this.uppercaseLetters) > 0 && this.uppercaseLetters > 0 ? "+" + ((password.length() - this.uppercaseLetters) * 2): "0",
				(password.length() - this.lowercaseLetters) > 0 && this.lowercaseLetters > 0 ? "+" + ((password.length() - this.lowercaseLetters) * 2): "0", 
				this.numbers > 0 ? "+" + (this.numbers * 4): "0",
				this.symbols > 0 ? "+" + (this.symbols * 6): "0",
				this.middleNumbersOrSymbols > 0 ? "+" + (this.middleNumbersOrSymbols * 2): "0"
				, this.isRequirements? "+" + (this.requirements * 2) : "0"
				, this.lettersOnly? "-" + this.password.length(): "0"
				, this.numbersOnly? "-" + this.password.length(): "0"
				, this.repeatCharacters > 0 ? "-" + this.repeatCharacters : "0"
				, this.consecutiveUppercase > 0 ? "-" + (this.consecutiveUppercase * 2): "0"
				, this.consecutiveLowercase > 0 ? "-" + (this.consecutiveLowercase * 2): "0"
				, this.consecutiveNumbers > 0 ? "-" + (this.consecutiveNumbers * 2): "0"
				, this.sequentialLetters > 0 ? "-" + (this.sequentialLetters * 3): "0"
				, this.sequentialNumbers > 0 ? "-" + (this.sequentialNumbers * 3): "0"
				, this.sequentialSymbols > 0 ? "-" + (this.sequentialSymbols * 3): "0"};
		return bonus;
	}
	
	/*
	 * This function return status column for UI display.
	 * */
	public String[] getStatusColumn() {
		String[] status = {this.password.length() > 8 ? "OK": "FAIL", this.uppercaseLetters > 0 ? "OK":"FAIL",
				this.lowercaseLetters > 0 ? "OK":"FAIL", this.numbers > 0 ? "OK":"FAIL",
				this.symbols > 0 ? "OK":"FAIL", this.middleNumbersOrSymbols > 0 ? "OK":"FAIL",
				this.isRequirements ? "OK":"FAIL", this.lettersOnly ? "FAIL":"OK", this.numbersOnly? "FAIL":"OK",
				this.repeatCharacters > 0 ? "FAIL":"OK", this.consecutiveUppercase > 0 ? "FAIL":"OK",
				this.consecutiveLowercase > 0 ? "FAIL":"OK", this.consecutiveNumbers > 0 ? "FAIL":"OK",
				this.sequentialLetters > 0 ? "FAIL":"OK", this.sequentialNumbers > 0 ? "FAIL":"OK",
				this.sequentialSymbols > 0 ? "FAIL": "OK"};
		return status;
	}

	/*
	 * This function returns the value of 'isRequirements' that indicates if the requirements are met.
	 * */
	public boolean isMeetTheRequirements() {
		return this.isRequirements;
	}
}
