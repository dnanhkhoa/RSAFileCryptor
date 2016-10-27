package core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Observer;

import javax.crypto.NoSuchPaddingException;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import algorithm.RSA;
import structure.ObservableModel;
import structure.ProgressInfo;

public final class Processor {

    private final static String   SIGNATURE = "DKENC";
    private int                   BLOCK_SIZE;

    private final ProgressInfo    progressInfo;
    private final ObservableModel observable;

    private final RSA             rsa;

    public Processor() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.progressInfo = new ProgressInfo();
        this.observable = new ObservableModel();
        rsa = new RSA();
        BLOCK_SIZE = rsa.getValidBlockSize();
    }

    public void registerObserver(Observer observer) {
        this.observable.addObserver(observer);
    }

    protected void encryptFile(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Key key) {

    }

    protected void decryptFile(DataInputStream dataInputStream, DataOutputStream dataOutputStream, Key key) {

    }

    public void process(File inFile, File outFile, Key key) {
        
    }
}
