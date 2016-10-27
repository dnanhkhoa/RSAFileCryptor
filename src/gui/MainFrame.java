package gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import algorithm.RSA;
import core.Processor;
import structure.ProgressInfo;

public class MainFrame extends JFrame implements Observer {

    private static final long serialVersionUID = 1L;
    private JPanel            mainPane;

    private JTextField        txtKeyFile;
    private JTextField        txtPrivateKey;
    private JTextField        txtPublicKey;

    private JTextField        txtInputFile;
    private JTextField        txtOutputFile;

    private JButton           btnGenerate;
    private JButton           btnDo;

    private JProgressBar      pbProcessInfo;
    private JLabel            lblTimeLeftInfo;

    private Processor         processor;
    private boolean           isRunning;

    private RSA               rsa;

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
        setBounds(100, 100, 500, 350);

        mainPane = new JPanel();
        mainPane.setFont(new Font("Tahoma", Font.PLAIN, 14));
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        mainPane.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
        }, new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC,
                RowSpec.decode("default:grow"), FormSpecs.PARAGRAPH_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
        }));

        JPanel keyPane = new JPanel();
        mainPane.add(keyPane, "2, 2, fill, fill");
        keyPane.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(30dlu;pref)"), FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
        }, new RowSpec[] {
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:min(5dlu;default):grow"),
        }));

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
        filePane.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
        }, new RowSpec[] {
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
        }));

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
        infoPane.setLayout(new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(60dlu;pref)"),
        }, new RowSpec[] {
                FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
        }));

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

    }

    public void handleDoButton() {
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

}
