package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import algorithm.RSA;
import core.Processor;
import exception.ExceptionInfo;
import structure.ProgressInfo;
import utils.Utils;

public class MainFrame extends JFrame implements Observer {

	private static final long serialVersionUID = 1L;
	private JPanel			  mainPane;

	private JTextField		  txtKeyFile;
	private JTextField		  txtPrivateKey;
	private JTextField		  txtPublicKey;

	private JTextField		  txtInputFile;
	private JTextField		  txtOutputFile;

	private JButton			  btnGenerate;
	private JButton			  btnDo;

	private JProgressBar	  pbProcessInfo;
	private JLabel			  lblTimeLeftInfo;

	private JTextField		  txtInputFileSign;
	private JTextField		  txtSignFile;
	private JTextField		  txtKeyFileSign;

	private Processor		  processor;
	private boolean			  isRunning;

	private RSA				  rsa;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		// Initialize form
		setResizable(false);
		setTitle("RSA File Cryptor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 520, 500);

		mainPane = new JPanel();
		mainPane.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		mainPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.PARAGRAPH_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
						FormSpecs.RELATED_GAP_ROWSPEC, }));

		JPanel keyPane = new JPanel();
		mainPane.add(keyPane, "2, 2, fill, fill");
		keyPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(35dlu;pref)"), FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

		JLabel lblPrivateKey = new JLabel("Private key");
		lblPrivateKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		keyPane.add(lblPrivateKey, "1, 1, right, default");

		txtPrivateKey = new JTextField();
		txtPrivateKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPrivateKey.setEditable(false);
		txtPrivateKey.setColumns(10);
		keyPane.add(txtPrivateKey, "3, 1, 3, 1, fill, default");

		JButton btnPrivateKey = new JButton("...");
		btnPrivateKey.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handlePrivateKeyButton();
			}
		});
		btnPrivateKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPrivateKey.setFocusable(false);
		keyPane.add(btnPrivateKey, "7, 1");

		JLabel lblPublicKey = new JLabel("Public key");
		lblPublicKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		keyPane.add(lblPublicKey, "1, 3, right, default");

		txtPublicKey = new JTextField();
		txtPublicKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPublicKey.setEditable(false);
		txtPublicKey.setColumns(10);
		keyPane.add(txtPublicKey, "3, 3, 3, 1, fill, default");

		JButton btnPublicKey = new JButton("...");
		btnPublicKey.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handlePublicKeyButton();
			}
		});
		btnPublicKey.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPublicKey.setFocusable(false);
		keyPane.add(btnPublicKey, "7, 3");

		btnGenerate = new JButton("Generate");
		btnGenerate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnGenerate.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleGenerateButton();
			}
		});
		keyPane.add(btnGenerate, "5, 5, 3, 1");

		JPanel filePane = new JPanel();
		filePane.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainPane.add(filePane, "2, 4, fill, fill");
		filePane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblInputFile = new JLabel("Input file");
		lblInputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(lblInputFile, "1, 1, right, default");

		txtInputFile = new JTextField();
		txtInputFile.setEditable(false);
		txtInputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(txtInputFile, "3, 1, fill, default");
		txtInputFile.setColumns(10);

		JButton btnInputFile = new JButton("...");
		btnInputFile.setFocusable(false);
		btnInputFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleInputFileButton();
			}
		});

		btnInputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(btnInputFile, "5, 1");

		JLabel lblOutputFile = new JLabel("Output file");
		lblOutputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(lblOutputFile, "1, 3, right, default");

		txtOutputFile = new JTextField();
		txtOutputFile.setEditable(false);
		txtOutputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(txtOutputFile, "3, 3, fill, default");
		txtOutputFile.setColumns(10);

		JButton btnOutputFile = new JButton("...");
		btnOutputFile.setFocusable(false);
		btnOutputFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleOutputFileButton();
			}
		});

		btnOutputFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(btnOutputFile, "5, 3");

		JLabel lblKeyFile = new JLabel("Key file");
		lblKeyFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		filePane.add(lblKeyFile, "1, 5, right, default");

		txtKeyFile = new JTextField();
		txtKeyFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtKeyFile.setEditable(false);
		txtKeyFile.setColumns(10);
		filePane.add(txtKeyFile, "3, 5, fill, default");

		JButton btnKeyFile = new JButton("...");
		btnKeyFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleKeyFileButton();
			}
		});
		btnKeyFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnKeyFile.setFocusable(false);
		filePane.add(btnKeyFile, "5, 5");

		JPanel infoPane = new JPanel();
		infoPane.setFont(new Font("Tahoma", Font.PLAIN, 14));
		mainPane.add(infoPane, "2, 6, fill, fill");
		infoPane.setLayout(new FormLayout(
				new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(60dlu;pref)"), },
				new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, }));

		JLabel lblTimeLeft = new JLabel("Time left");
		lblTimeLeft.setFont(new Font("Tahoma", Font.PLAIN, 14));
		infoPane.add(lblTimeLeft, "1, 1");

		lblTimeLeftInfo = new JLabel("00:00:00");
		lblTimeLeftInfo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		infoPane.add(lblTimeLeftInfo, "3, 1");

		btnDo = new JButton("Encrypt");
		btnDo.setFocusable(false);
		btnDo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleDoButton();
			}
		});

		btnDo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		infoPane.add(btnDo, "5, 1, 1, 3");

		pbProcessInfo = new JProgressBar();
		pbProcessInfo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		infoPane.add(pbProcessInfo, "1, 3, 3, 1");

		final JLabel lblNewLabel = new JLabel("Digital signature");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.mainPane.add(lblNewLabel, "2, 8");

		final JPanel signPane = new JPanel();
		this.mainPane.add(signPane, "2, 10, fill, fill");
		signPane.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(60dlu;pref)"), FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(35dlu;pref)"), FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
						FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("fill:default:grow"), }));

		final JLabel lblInputFileSign = new JLabel("Input file");
		lblInputFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		signPane.add(lblInputFileSign, "1, 1, right, default");

		this.txtInputFileSign = new JTextField();
		this.txtInputFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.txtInputFileSign.setEditable(false);
		this.txtInputFileSign.setColumns(10);
		signPane.add(this.txtInputFileSign, "3, 1, 5, 1, fill, default");

		final JButton btnInputFileSign = new JButton("...");
		btnInputFileSign.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				handleBtnInputFileSignActionPerformed(arg0);
			}
		});
		btnInputFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnInputFileSign.setFocusable(false);
		signPane.add(btnInputFileSign, "9, 1");

		final JLabel lblSignatureFile = new JLabel("Signature file");
		lblSignatureFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		signPane.add(lblSignatureFile, "1, 3, right, default");

		this.txtSignFile = new JTextField();
		this.txtSignFile.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.txtSignFile.setEditable(false);
		this.txtSignFile.setColumns(10);
		signPane.add(this.txtSignFile, "3, 3, 5, 1, fill, default");

		final JButton btnSignFileSign = new JButton("...");
		btnSignFileSign.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleBtnSignFileSignActionPerformed(e);
			}
		});
		btnSignFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnSignFileSign.setFocusable(false);
		signPane.add(btnSignFileSign, "9, 3");

		final JLabel lblKeyFileSign = new JLabel("Key file");
		lblKeyFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		signPane.add(lblKeyFileSign, "1, 5, right, default");

		this.txtKeyFileSign = new JTextField();
		this.txtKeyFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.txtKeyFileSign.setEditable(false);
		this.txtKeyFileSign.setColumns(10);
		signPane.add(this.txtKeyFileSign, "3, 5, 5, 1, fill, default");

		final JButton btnKeyFileSign = new JButton("...");
		btnKeyFileSign.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleBtnKeyFileSignActionPerformed(e);
			}
		});
		btnKeyFileSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnKeyFileSign.setFocusable(false);
		signPane.add(btnKeyFileSign, "9, 5");

		final JButton btnSign = new JButton("Sign");
		btnSign.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleBtnSignActionPerformed(e);
			}
		});
		btnSign.setFont(new Font("Tahoma", Font.PLAIN, 14));
		signPane.add(btnSign, "5, 7");

		final JButton btnVerify = new JButton("Verify");
		btnVerify.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				handleBtnVerifyActionPerformed(e);
			}
		});
		btnVerify.setFont(new Font("Tahoma", Font.PLAIN, 14));
		signPane.add(btnVerify, "7, 7, 3, 1");

		// Initialize variables
		initialize();
	}

	public void initialize() {
		try {
			processor = new Processor();
			processor.registerObserver(this);
			rsa = new RSA();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handlePrivateKeyButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save private key file");
		fileChooser.setSelectedFile(new File("PrivateKey.pri"));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtPrivateKey.setText(fileChooser.getSelectedFile().toString());
		}
	}

	public void handlePublicKeyButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save public key file");
		fileChooser.setSelectedFile(new File("PublicKey.pub"));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtPublicKey.setText(fileChooser.getSelectedFile().toString());
		}
	}

	public void handleGenerateButton() {
		if (txtPrivateKey.getText().isEmpty() || txtPublicKey.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Some required fields have not been entered!");
		} else {
			File privateKeyFile = new File(txtPrivateKey.getText());
			File publicKeyFile = new File(txtPublicKey.getText());
			KeyPair keyPair = rsa.generateKeyPair();

			try (FileOutputStream priFileOS = new FileOutputStream(privateKeyFile)) {
				try (ObjectOutputStream priOOS = new ObjectOutputStream(priFileOS)) {
					priOOS.writeObject(keyPair.getPrivate());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try (FileOutputStream pubFileOS = new FileOutputStream(publicKeyFile)) {
				try (ObjectOutputStream pubOOS = new ObjectOutputStream(pubFileOS)) {
					pubOOS.writeObject(keyPair.getPublic());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			JOptionPane.showMessageDialog(this, "Done");
		}
	}

	public void handleInputFileButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose file");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			txtInputFile.setText(selectedFile.toString());

			if (processor.isEncryptedFile(selectedFile)) {
				btnDo.setText("Decrypt");
				txtOutputFile.setText(txtInputFile.getText().replace(".enc", "") + ".dec");
			} else {
				btnDo.setText("Encrypt");
				txtOutputFile.setText(txtInputFile.getText() + ".enc");
			}

			// Reset progress info
			clearProgressInfo();
		}
	}

	public void handleOutputFileButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save file");
		fileChooser.setSelectedFile(new File(txtOutputFile.getText()));
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtOutputFile.setText(fileChooser.getSelectedFile().toString());
		}
	}

	public void handleKeyFileButton() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose key file");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtKeyFile.setText(fileChooser.getSelectedFile().toString());
		}
	}

	public void handleDoButton() {
		if (isRunning) {
			JOptionPane.showMessageDialog(this, "Process is running!");
		} else {
			changeState(true);

			if (txtInputFile.getText().isEmpty() || txtOutputFile.getText().isEmpty()
					|| txtKeyFile.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Some required fields have not been entered!");
			} else {
				final Frame component = this;
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							processor.process(new File(txtInputFile.getText()), new File(txtOutputFile.getText()),
									new File(txtKeyFile.getText()));
							JOptionPane.showMessageDialog(component, "Done!");
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(component, e.getMessage());
						} finally {
							changeState(false);
						}
					}
				});
				thread.start();
			}
		}
	}

	public void changeState(boolean isRunning) {
		this.isRunning = isRunning;
		btnDo.setEnabled(!isRunning);
	}

	public void clearProgressInfo() {
		lblTimeLeftInfo.setText("00:00:00");
		pbProcessInfo.setValue(0);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof ProgressInfo) {
			ProgressInfo processInfo = (ProgressInfo) arg;
			lblTimeLeftInfo.setText(processInfo.getTimeLeft());
			pbProcessInfo.setValue(processInfo.getProgressValue());
		}
	}

	protected void handleBtnInputFileSignActionPerformed(ActionEvent arg0) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose file");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().toString();
			txtInputFileSign.setText(fileName);
			txtSignFile.setText(fileName + ".sig");
		}
	}

	protected void handleBtnSignFileSignActionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose signature file");
		fileChooser.setSelectedFile(new File(txtSignFile.getText()));
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtSignFile.setText(fileChooser.getSelectedFile().toString());
		}
	}

	protected void handleBtnKeyFileSignActionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose key file");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			txtKeyFileSign.setText(fileChooser.getSelectedFile().toString());
		}
	}

	protected void handleBtnSignActionPerformed(ActionEvent e) {
		if (txtInputFileSign.getText().isEmpty() || txtSignFile.getText().isEmpty()
				|| txtKeyFileSign.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Some required fields have not been entered!");
		} else {
			try {
				String hash = Utils.hashSHA256File(new File(txtInputFileSign.getText()), rsa.getValidEncryptedBlockSize());
				
				Key key = null;
				try (FileInputStream priFileIS = new FileInputStream(new File(txtKeyFileSign.getText()))) {
					try (ObjectInputStream priOIS = new ObjectInputStream(priFileIS)) {
						key = (Key) priOIS.readObject();		
					}
				} catch (Exception ex) {
					throw new ExceptionInfo("Key is invalid!");
				}
				
				try (FileOutputStream fileOutputStream = new FileOutputStream(new File(txtSignFile.getText()))) {
					fileOutputStream.write(rsa.encrypt(hash.getBytes(), key));
				}
				
				JOptionPane.showMessageDialog(this, "Done!");
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		}
	}

	protected void handleBtnVerifyActionPerformed(ActionEvent e) {
		if (txtInputFileSign.getText().isEmpty() || txtSignFile.getText().isEmpty()
				|| txtKeyFileSign.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Some required fields have not been entered!");
		} else {
			try {
				String hash = Utils.hashSHA256File(new File(txtInputFileSign.getText()), rsa.getValidEncryptedBlockSize());
				
				Key key = null;
				try (FileInputStream priFileIS = new FileInputStream(new File(txtKeyFileSign.getText()))) {
					try (ObjectInputStream priOIS = new ObjectInputStream(priFileIS)) {
						key = (Key) priOIS.readObject();		
					}
				} catch (Exception ex) {
					throw new ExceptionInfo("Key is invalid!");
				}
				
				String hashTemp = null;
				File signFile = new File(txtSignFile.getText());
				try (FileInputStream fileInputStream = new FileInputStream(signFile)) {
					byte[] buffer = new byte[(int) Math.min(signFile.length(), rsa.getValidEncryptedBlockSize())];
					if (fileInputStream.read(buffer) == buffer.length) {
						byte[] decrypted = rsa.decrypt(buffer, key);
						hashTemp = new String(decrypted);
					}
				}
				
				if (hash.contentEquals(hashTemp)) {
					JOptionPane.showMessageDialog(this, "Match!");
				} else {
					JOptionPane.showMessageDialog(this, "Not match!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		}
	}
}
