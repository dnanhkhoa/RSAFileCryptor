package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Observer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import algorithm.RSA;
import exception.ExceptionInfo;
import structure.ObservableModel;
import structure.ProgressInfo;

public final class Processor {

	private final static String	  SIGNATURE	= "DKENC";
	private int					  BLOCK_SIZE;
	private int					  ENCRYPTED_BLOCK_SIZE;

	private final ProgressInfo	  progressInfo;
	private final ObservableModel observable;

	private final RSA			  rsa;

	public Processor() throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.progressInfo = new ProgressInfo();
		this.observable = new ObservableModel();
		rsa = new RSA();
		BLOCK_SIZE = rsa.getValidBlockSize();
		ENCRYPTED_BLOCK_SIZE = rsa.getValidEncryptedBlockSize();
	}

	public void registerObserver(Observer observer) {
		this.observable.addObserver(observer);
	}

	protected void encryptFile(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Key key)
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		dataOutputStream.write(SIGNATURE.getBytes());
		dataOutputStream.writeInt(ENCRYPTED_BLOCK_SIZE);
		dataOutputStream.writeLong(progressInfo.getTotal());

		long startTime = System.nanoTime();

		byte[] data = new byte[BLOCK_SIZE];
		int bytesRead;
		while ((bytesRead = dataInputStream.read(data)) == BLOCK_SIZE) {
			dataOutputStream.write(rsa.encrypt(data, key));

			progressInfo.setSecondLeft((System.nanoTime() - startTime)
					* (progressInfo.getTotal() - progressInfo.getCurrent()) / 1000000000);
			progressInfo.setCurrent(progressInfo.getCurrent() + 1);

			observable.setChanged();
			observable.notifyObservers(progressInfo);

			startTime = System.nanoTime();
		}
		if (bytesRead > 0) {
			dataOutputStream.write(rsa.encrypt(Arrays.copyOf(data, bytesRead), key));

			progressInfo.setSecondLeft(0);
			progressInfo.setCurrent(progressInfo.getTotal());

			observable.setChanged();
			observable.notifyObservers(progressInfo);
		}
	}

	protected void decryptFile(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Key key)
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		long startTime = System.nanoTime();

		byte[] data = new byte[ENCRYPTED_BLOCK_SIZE];
		
		while (dataInputStream.read(data) == ENCRYPTED_BLOCK_SIZE) {
			dataOutputStream.write(rsa.decrypt(data, key));

			progressInfo.setSecondLeft((System.nanoTime() - startTime)
					* (progressInfo.getTotal() - progressInfo.getCurrent()) / 1000000000);
			progressInfo.setCurrent(progressInfo.getCurrent() + 1);

			observable.setChanged();
			observable.notifyObservers(progressInfo);

			startTime = System.nanoTime();
		}
	}

	public void process(File inFile, File outFile, File keyFile) throws Exception {
		boolean isEncryptedFile = false;
		long numParts = 0;
		int skipLength = 0;

		try (InputStream inputStream = new FileInputStream(inFile)) {
			try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
				byte[] signatureData = new byte[SIGNATURE.length()];

				if (dataInputStream.read(signatureData) == SIGNATURE.length()
						&& SIGNATURE.equals(new String(signatureData))
						&& dataInputStream.readInt() == ENCRYPTED_BLOCK_SIZE) {
					numParts = dataInputStream.readLong();
					skipLength = SIGNATURE.length() + Integer.BYTES + Long.BYTES;
					isEncryptedFile = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Key key = null;

		try (FileInputStream priFileIS = new FileInputStream(keyFile)) {
			try (ObjectInputStream priOIS = new ObjectInputStream(priFileIS)) {
				key = (Key) priOIS.readObject();
			}
		} catch (Exception e) {
			throw new ExceptionInfo("Key is invalid!");
		}

		try (InputStream inputStream = new FileInputStream(inFile)) {
			try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
				try (OutputStream outputStream = new FileOutputStream(outFile)) {
					try (DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
						if (isEncryptedFile) {
							progressInfo.reset();
							progressInfo.setTotal(numParts);
							observable.setChanged();
							observable.notifyObservers(progressInfo);
							
							dataInputStream.skip(skipLength);
							decryptFile(dataInputStream, dataOutputStream, key);
						} else {
							progressInfo.reset();
							progressInfo.setTotal((inFile.length() - 1) / BLOCK_SIZE + 1);
							observable.setChanged();
							observable.notifyObservers(progressInfo);
							
							encryptFile(dataInputStream, dataOutputStream, key);
						}
					}
				} catch (Exception e) {
					if (outFile.exists()) {
						outFile.delete();
					}
					throw e;
				}
			}
		}
	}

	public boolean isEncryptedFile(File inFile) {
		try (InputStream inputStream = new FileInputStream(inFile)) {
			try (DataInputStream dataInputStream = new DataInputStream(inputStream)) {
				byte[] signatureData = new byte[SIGNATURE.length()];
				if (dataInputStream.read(signatureData) == SIGNATURE.length()
						&& SIGNATURE.equals(new String(signatureData))) {
					return dataInputStream.readInt() == ENCRYPTED_BLOCK_SIZE;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
