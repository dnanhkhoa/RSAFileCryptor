package algorithm;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class RSA {

	private static final String	   ALGORITHM = "RSA";
	private static final int	   KEY_SIZE	 = 2048;
	private final KeyPairGenerator keyPairGenerator;
	private final Cipher		   cipher;

	public RSA() throws NoSuchAlgorithmException, NoSuchPaddingException {
		keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyPairGenerator.initialize(KEY_SIZE);
		cipher = Cipher.getInstance(ALGORITHM);
	}

	public KeyPair generateKeyPair() {
		return keyPairGenerator.genKeyPair();
	}

	public int getValidBlockSize() {
		return getValidEncryptedBlockSize() - 11; // 11 bytes paddings
	}

	public int getValidEncryptedBlockSize() {
		return KEY_SIZE / 8;
	}

	public byte[] encrypt(byte[] data, Key key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	public byte[] decrypt(byte[] data, Key key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}
}
