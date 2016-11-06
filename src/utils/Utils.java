package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

	public static String hashSHA256File(File file, int blockSize)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] buffer = new byte[blockSize];
		int bytesRead = 0;
		try (InputStream is = new FileInputStream(file)) {
			while ((bytesRead = is.read(buffer)) > 0) {
				md.update(buffer, 0, bytesRead);
			}
		}
		return byteToHex(md.digest());
	}

	public static String byteToHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			builder.append(Integer.toHexString((bytes[i] & 0xff) + 0x100).substring(1));
		}
		return builder.toString();
	}
}
