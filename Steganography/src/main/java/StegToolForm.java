/*
 * Creator: Noam Shevach
 * Date: 8.7.2021
 * 
 * This class display the main window of the app and responsible for 
 * the connection between the user requests to the logic of the app.
 * */

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import Exceptions.CompressException;
import Exceptions.DecompressException;
import Exceptions.InflaterException;
import Exceptions.MessageNotFound;
import Exceptions.OutputStreamException;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class StegToolForm extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextArea txtMessage;
	private JTextField txtPassword, txtIV;
	private JCheckBox chckbxHideMessage,chckbxSeek, chckbxCompress, chckbxDecompress;
	private JButton btnSelectImage, btnDone, btnDetails, btnSaveImage, btnClean ;
	private JLabel lblBeforeImage, lblAfterImage, lblKeySize, lblInitialVector, lblrequestPassword, lblTextLength;
	private JRadioButton rdbtnAES128, rdbtnAES192, rdbtnAES256;
	private JProgressBar bar;
	
	private JTextField txtIV_d, txtPassword_d;
	private JLabel lblInitialVector_d, lblKeySize_d, lblRequestPassword_d;
	private JRadioButton rdbtnAES128_d, rdbtnAES192_d, rdbtnAES256_d;
	
	private PasswordStrength suggestedPassword;
	private PasswordStrengthForm passwordStrengthForm;	
	private String imagePath = null;
	private byte[] imageWithMessage;
	private byte[] salt;

	public StegToolForm() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		this.setTitle("Steganography Tool");
		this.setResizable(false);
		getContentPane().setLayout(null);
		this.suggestedPassword = new PasswordStrength();
		this.passwordStrengthForm = new PasswordStrengthForm(suggestedPassword);
		
		imagesComponents();
		hideMessageComponents();
		passwordComponents();
		encryptionPreferences();
		compress();
		seekMessageComponents();
		cleanFields();

		doneButton();

		this.setSize(1120,870);
		this.setVisible(true);
	}
	
	/*
	 * This function display the encryption related components of the window.
	 * Event Listeners are also included.
	 * */
	private void encryptionPreferences() {
		lblKeySize = new JLabel(" My preffered key size is:");
		lblKeySize.setEnabled(false);
		lblKeySize.setBounds(54, 732, 158, 20);
		lblKeySize.setFocusable(false);
		getContentPane().add(lblKeySize);
		
		rdbtnAES128 = new JRadioButton("CBC_AES_128");
		rdbtnAES128.setSelected(true);
		rdbtnAES128.addActionListener(new ActionListener() {		
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES128.isSelected())
					Crypt.setKeySize(AES.AES128);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES128.setEnabled(false);
		rdbtnAES128.setBounds(227, 732, 109, 20);
		rdbtnAES128.setFocusable(false);
		getContentPane().add(rdbtnAES128);
		
		rdbtnAES192 = new JRadioButton("CBC_AES_192");
		rdbtnAES192.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES192.isSelected())
					Crypt.setKeySize(AES.AES192);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES192.setEnabled(false);
		rdbtnAES192.setBounds(334, 732, 109, 20);
		rdbtnAES192.setFocusable(false);
		getContentPane().add(rdbtnAES192);
		
		rdbtnAES256 = new JRadioButton("CBC_AES_256");
		rdbtnAES256.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES256.isSelected())
					Crypt.setKeySize(AES.AES256);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES256.setEnabled(false);
		rdbtnAES256.setBounds(441, 732, 109, 23);
		rdbtnAES256.setFocusable(false);
		getContentPane().add(rdbtnAES256);
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnAES128);
		group.add(rdbtnAES192);
		group.add(rdbtnAES256);
		
		lblInitialVector = new JLabel(" Initialization Vector:");
		lblInitialVector.setEnabled(false);
		lblInitialVector.setBounds(54, 761, 123, 20);
		getContentPane().add(lblInitialVector);
		
		txtIV = new JTextField();
		txtIV.setEnabled(false);
		txtIV.setBackground(new Color(208, 208, 208));
		txtIV.setBounds(179, 761, 200, 20);
		txtIV.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}

			public void removeUpdate(DocumentEvent e) {

				if(!isFormCompleted())
					btnDone.setEnabled(false);
				else
					btnDone.setEnabled(true);
			}

			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub			
			}
			
		});
		getContentPane().add(txtIV);
		
		lblInitialVector_d = new JLabel(" Initialization Vector:");
		lblInitialVector_d.setEnabled(false);
		lblInitialVector_d.setBounds(584, 627, 123, 20);
		getContentPane().add(lblInitialVector_d);
		
		txtIV_d = new JTextField();
		txtIV_d.setEnabled(false);
		txtIV_d.setBackground(new Color(208, 208, 208));
		txtIV_d.setBounds(748, 628, 200, 20);
		txtIV_d.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}

			public void removeUpdate(DocumentEvent e) {
				if(!isFormCompleted())
					btnDone.setEnabled(false);
				else
					btnDone.setEnabled(true);
			}

			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub			
			}
			
		});
		getContentPane().add(txtIV_d);
		
		lblKeySize_d = new JLabel(" My preffered key size is:");
		lblKeySize_d.setFocusable(false);
		lblKeySize_d.setEnabled(false);
		lblKeySize_d.setBounds(584, 654, 158, 20);
		getContentPane().add(lblKeySize_d);
		
		rdbtnAES128_d = new JRadioButton("CBC_AES_128");
		rdbtnAES128_d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES128_d.isSelected())
					Crypt.setKeySize(AES.AES128);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES128_d.setSelected(true);
		rdbtnAES128_d.setFocusable(false);
		rdbtnAES128_d.setEnabled(false);
		rdbtnAES128_d.setBounds(757, 654, 109, 20);
		getContentPane().add(rdbtnAES128_d);
		
		rdbtnAES192_d = new JRadioButton("CBC_AES_192");
		rdbtnAES192_d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES192_d.isSelected())
					Crypt.setKeySize(AES.AES192);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES192_d.setFocusable(false);
		rdbtnAES192_d.setEnabled(false);
		rdbtnAES192_d.setBounds(864, 654, 109, 20);
		getContentPane().add(rdbtnAES192_d);
		
		rdbtnAES256_d = new JRadioButton("CBC_AES_256");
		rdbtnAES256_d.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(rdbtnAES256_d.isSelected())
					Crypt.setKeySize(AES.AES256);
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}
		});
		rdbtnAES256_d.setFocusable(false);
		rdbtnAES256_d.setEnabled(false);
		rdbtnAES256_d.setBounds(971, 654, 109, 23);
		getContentPane().add(rdbtnAES256_d);
		
		ButtonGroup group2 = new ButtonGroup();
		group2.add(rdbtnAES128_d);
		group2.add(rdbtnAES192_d);
		group2.add(rdbtnAES256_d);
	}
	
	/*
	 * This function display the compression related components of the window.
	 * Event Listeners are also included.
	 * */
	private void compress() {
		chckbxCompress = new JCheckBox("I would like to compress the message.");
		chckbxCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxCompress.isSelected()) {
					try {
						String message = Compress.checkFeasibility(txtMessage.getText().getBytes());
						JOptionPane.showMessageDialog(null, message, "Attention", JOptionPane.INFORMATION_MESSAGE);
						
						if(txtMessage.getText().length() > 0) {
							int imageLength = calSecretMessageMaxLength();
							int compressedLength = Compress.compress(txtMessage.getText().getBytes("UTF-8")).length;
							lblTextLength.setText(compressedLength +" / " + imageLength);
						}
					} 
					catch (CompressException e1) { JOptionPane.showMessageDialog(null, "Compression failed", "Error", JOptionPane.ERROR_MESSAGE); } 
					catch (UnsupportedEncodingException e1) {JOptionPane.showMessageDialog(null, "Compression failed", "Error", JOptionPane.ERROR_MESSAGE); } 
				}
			}
		});
		chckbxCompress.setEnabled(false);
		chckbxCompress.setBounds(50, 794, 267, 20);
		chckbxCompress.setFocusable(false);
		getContentPane().add(chckbxCompress);
		
		chckbxDecompress = new JCheckBox("I would like to decompress the message.");
		chckbxDecompress.setFocusable(false);
		chckbxDecompress.setEnabled(false);
		chckbxDecompress.setBounds(580, 684, 267, 20);
		getContentPane().add(chckbxDecompress);
	}
	
	
	/*
	 * This function display the image related components of the window.
	 * Event Listeners are also included.
	 * */
	private void imagesComponents() {
		// image titles
		JLabel lblBeforeImageTitle = new JLabel();
		lblBeforeImageTitle.setText("Image before hiding information");
		lblBeforeImageTitle.setBounds(202, 24, 200, 20);
		
		JLabel lblAfterImageTitle = new JLabel();
		lblAfterImageTitle.setText("Image after hiding information");
		lblAfterImageTitle.setBounds(754, 24, 188, 20);
		
		getContentPane().add(lblBeforeImageTitle);
		getContentPane().add(lblAfterImageTitle);
		
		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
		
		// image frames
		lblBeforeImage = new JLabel();
		lblBeforeImage.setBorder(border);
		lblBeforeImage.setBounds(50, 50, 500, 500);
		
		lblAfterImage = new JLabel();
		lblAfterImage.setBorder(border);
		lblAfterImage.setBounds(580, 50, 500, 500);
		
		getContentPane().add(lblBeforeImage);
		getContentPane().add(lblAfterImage);
		
		//select image button
		this.btnSelectImage = new JButton("Select Image");
		btnSelectImage.setEnabled(false);
		btnSelectImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSaveImage.setEnabled(false);
	        	JFileChooser fileChooser = new JFileChooser();
	        	int response = fileChooser.showOpenDialog(null);
	        	if(response == JFileChooser.APPROVE_OPTION) {
	        		Image image;
					try {
						image = ImageIO.read(new File(fileChooser.getSelectedFile().getAbsolutePath()));
						imagePath = fileChooser.getSelectedFile().getAbsolutePath();
		        		ImageIcon icon = new ImageIcon(image);	        
		        		if(chckbxHideMessage.isSelected()) {
		        			lblBeforeImage.setIcon(icon);
		        			int imageLength = calSecretMessageMaxLength();
					        lblTextLength.setText( "0 / " + imageLength);
		        		}
		        		else
		        			lblAfterImage.setIcon(icon);
						if(isFormCompleted())
							btnDone.setEnabled(true);
						else
							btnDone.setEnabled(false);
					} catch (IOException e1) { e1.printStackTrace(); }
	        	}
			}
		});
		btnSelectImage.setFocusable(false);
		btnSelectImage.setBounds(672, 784, 130, 30);
		
		getContentPane().add(btnSelectImage);
		
		//save image
		btnSaveImage = new JButton("Save Image");
		btnSaveImage.setEnabled(false);
		btnSaveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser();
				int response = fileChooser.showSaveDialog(null);
				
				if(response == JFileChooser.APPROVE_OPTION) {
		        	try {
						String selectedFile = fileChooser.getSelectedFile().getAbsolutePath() +".bmp";
						FileAccess.writeSaltToFile(imageWithMessage, salt);
				        OutputStream out = new FileOutputStream(selectedFile);
						out.write(imageWithMessage);
			        	out.close();
					} 
		        	catch (IOException e1) { 
		        		e1.printStackTrace();
		        		JOptionPane.showMessageDialog(null, "Saving image failed", "Error", JOptionPane.ERROR_MESSAGE);
		        	} catch (NoSuchAlgorithmException e1) {
		        		JOptionPane.showMessageDialog(null, "Hashing new image has failed", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				

			}
		});
		btnSaveImage.setBounds(812, 784, 130, 30);
		getContentPane().add(btnSaveImage);
	}
	
	/*
	 * This function calculates the maximum length of the secret message.
	 * */
	private int calSecretMessageMaxLength() {
		int bitLength = 0, temp = (lblBeforeImage.getIcon().getIconWidth() * lblBeforeImage.getIcon().getIconHeight() * 3);
		while(temp /2 > 0) {
			temp = temp /2;
			bitLength++;
		}
		return (int) ((int) (Math.pow(2, bitLength) / 8)* SteganographyHelper.secretMessageMaxCapacity - 1000);
	}
	
	/*
	 * This function enables the components that are related to message hiding.
	 * */
	private void enableHideMessageComponents() {
		chckbxCompress.setEnabled(true);
		txtMessage.setEnabled(true);
		txtMessage.setBackground(Color.WHITE);
		txtPassword.setEnabled(true);
		txtPassword.setBackground(Color.WHITE);
		txtIV.setEnabled(true);
		txtIV.setBackground(Color.WHITE);
		bar.setEnabled(true);
		lblrequestPassword.setEnabled(true);
		lblInitialVector.setEnabled(true);
		lblKeySize.setEnabled(true);
		rdbtnAES128.setEnabled(true);
		rdbtnAES192.setEnabled(true);
		rdbtnAES256.setEnabled(true);
	}
	
	/*
	 * This function disables the components that are related to message hiding.
	 * */
	private void disableHideMessageComponents() {
		chckbxHideMessage.setSelected(false);
		chckbxCompress.setEnabled(false);
		chckbxCompress.setSelected(false);
		txtMessage.setEnabled(false);
		txtMessage.setBackground(Color.decode("#D0D0D0"));
		txtMessage.setText("");
		txtPassword.setText("");
		txtPassword.setEnabled(false);
		txtPassword.setBackground(Color.decode("#D0D0D0"));
		txtIV.setEnabled(false);
		txtIV.setBackground(Color.decode("#D0D0D0"));
		txtIV.setText("");
		bar.setEnabled(false);
		btnDetails.setEnabled(false);
		rdbtnAES128.setEnabled(false);
		rdbtnAES128.setSelected(true);
		rdbtnAES192.setEnabled(false);
		rdbtnAES256.setEnabled(false);
		btnDone.setEnabled(false);
		lblKeySize.setEnabled(false);
		lblrequestPassword.setEnabled(false);
		lblInitialVector.setEnabled(false);
	}
	
	/*
	 * This function display the message hiding related components of the window.
	 * Event Listeners are also included.
	 * */
	private void hideMessageComponents() {
		this.chckbxHideMessage = new JCheckBox();
		chckbxHideMessage.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(chckbxHideMessage.isSelected()) {
					enableHideMessageComponents();
					disableSeekMessgeComponents();
					chckbxSeek.setSelected(false);
					btnSelectImage.setEnabled(true);
					lblBeforeImage.setIcon(null);
					lblAfterImage.setIcon(null);
				}
				else {
					disableHideMessageComponents();
					btnSelectImage.setEnabled(false);
				}
			}
		});
		chckbxHideMessage.setText("I would like to hide the text below:");
		chckbxHideMessage.setBounds(50, 570, 250, 20);
		chckbxHideMessage.setFocusable(false);
		


		this.txtMessage = new JTextArea();
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				if(lblBeforeImage.getIcon() == null) {
					txtMessage.setText("");
					JOptionPane.showMessageDialog(null, "Please select an image first" , "Attention!", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				int imageLength = calSecretMessageMaxLength();
		        int keyCode = evt.getKeyCode();
		        if(chckbxCompress.isSelected() == false) {
	                if(txtMessage.isEditable())
	                	lblTextLength.setText(txtMessage.getText().length() +" / " + imageLength);
	                
	                if(keyCode == KeyEvent.VK_BACK_SPACE && !txtMessage.isEditable()) {
	                    txtMessage.setEditable(true); 
	                    txtMessage.setText(txtMessage.getText().substring(0, imageLength - 1));
	                    lblTextLength.setText((imageLength - 1) + " / " + imageLength);
	                }
	                else {
		                if (txtMessage.getText().length() >= imageLength) 
		                	txtMessage.setEditable(false); 
	                }
		        }else {
		        	int compressedLength;
					try {
						compressedLength = Compress.compress(txtMessage.getText().getBytes("UTF-8")).length;
					} 
					catch (UnsupportedEncodingException e) { JOptionPane.showMessageDialog(null, "Faild to compress the message\\n Please uncheck compress option" , "Error!", JOptionPane.ERROR_MESSAGE); return; } 
					catch (CompressException e) {JOptionPane.showMessageDialog(null, "Faild to compress the message\n Please uncheck compress option" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
	                
					if(txtMessage.isEditable())
	                	lblTextLength.setText( compressedLength +" / " + imageLength);
	                
	                if(keyCode == KeyEvent.VK_BACK_SPACE && !txtMessage.isEditable()) {
	                    txtMessage.setEditable(true); 
	                    txtMessage.setText(txtMessage.getText().substring(0, imageLength - 1));
	                }
	                else {
		                if (compressedLength >= imageLength) 
		                	txtMessage.setEditable(false); 
	                }
		        }
			}
		});
		
		lblTextLength = new JLabel("0/1000");
		lblTextLength.setBounds(421, 570, 130, 14);
		getContentPane().add(lblTextLength);
		
		txtMessage.setEnabled(false);
		txtMessage.setBackground(Color.decode("#D0D0D0"));
		txtMessage.setWrapStyleWord(true);
		txtMessage.setLineWrap(true);
		txtMessage.getDocument().addDocumentListener(new DocumentListener() {
			
			public void insertUpdate(DocumentEvent e) {
				if(isFormCompleted())
					btnDone.setEnabled(true);
			}

			public void removeUpdate(DocumentEvent e) {
				if(!isFormCompleted())
					btnDone.setEnabled(false);
			}

			public void changedUpdate(DocumentEvent e) {
				
			}			
		});
		JScrollPane sp = new JScrollPane(txtMessage);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setBounds(50, 590, 500, 102);
		getContentPane().add(sp);
		getContentPane().add(chckbxHideMessage);
	}
	
	/*
	 * This function display the password related components of the window.
	 * Event Listeners are also included.
	 * */
	private void passwordComponents() {
		
		lblrequestPassword = new JLabel("Please type your password:");
		lblrequestPassword.setEnabled(false);
		lblrequestPassword.setBounds(55, 703, 200, 20);
		getContentPane().add(lblrequestPassword);
		
		this.btnDetails = new JButton("Details");
		btnDetails.setBounds(462, 703, 86, 20);
		btnDetails.setFocusable(false);
		btnDetails.setEnabled(false);
		btnDetails.addActionListener(new ActionListener() {

			//@Override
			public void actionPerformed(ActionEvent e) {
				passwordStrengthForm.setVisible(true);
			}
			
		});
		
		this.bar = new JProgressBar();
		bar.setValue(0);
		bar.setBounds(350, 703, 109, 20);
		bar.setStringPainted(true);
		
		this.txtPassword = new JTextField();
		txtPassword.setBounds(215, 703, 130, 20);
		txtPassword.setEnabled(false);
		txtPassword.setBackground(Color.decode("#D0D0D0"));
		txtPassword.getDocument().addDocumentListener(new DocumentListener() {

			//@Override
			public void insertUpdate(DocumentEvent e) {
				suggestedPassword.setPassword(txtPassword.getText());
				bar.setValue(suggestedPassword.getScore());
				passwordStrengthForm.updateRows();
				if(txtPassword.getText().length() > 0) {
					btnDetails.setEnabled(true);
					
				}
				if(isFormCompleted())
					btnDone.setEnabled(true);
			}

			//@Override
			public void removeUpdate(DocumentEvent e) {
				suggestedPassword.setPassword(txtPassword.getText());
				bar.setValue(suggestedPassword.getScore());
				passwordStrengthForm.updateRows();
				if(txtPassword.getText().length() == 0) {
					btnDetails.setEnabled(false);
					passwordStrengthForm.dispose();
				}
				if(isFormCompleted())
					btnDone.setEnabled(true);
				else
					btnDone.setEnabled(false);
			}

			//@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub			
			}
			
		});
		getContentPane().add(txtPassword);
		getContentPane().add(bar);
		getContentPane().add(btnDetails);
		
		lblRequestPassword_d = new JLabel("Please type your password:");
		lblRequestPassword_d.setEnabled(false);
		lblRequestPassword_d.setBounds(584, 597, 158, 20);
		getContentPane().add(lblRequestPassword_d);
		
		txtPassword_d = new JTextField();
		txtPassword_d.setEnabled(false);
		txtPassword_d.setBackground(new Color(208, 208, 208));
		txtPassword_d.setBounds(748, 597, 200, 20);
		txtPassword_d.getDocument().addDocumentListener(new DocumentListener() {

			//@Override
			public void insertUpdate(DocumentEvent e) {
				if(isFormCompleted())
					btnDone.setEnabled(true);
			}

			//@Override
			public void removeUpdate(DocumentEvent e) {
				if(!isFormCompleted())
					btnDone.setEnabled(false);
			}

			//@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub			
			}
			
		});
		getContentPane().add(txtPassword_d);
		
	}
	
	/*
	 * This function display the extract message related components of the window.
	 * Event Listeners are also included.
	 * */
	private void seekMessageComponents() {
		this.chckbxSeek = new JCheckBox();
		chckbxSeek.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(chckbxSeek.isSelected()) {
					disableHideMessageComponents();
					enableSeekMessageComponents();
					chckbxHideMessage.setSelected(false);
					btnSelectImage.setEnabled(true);
					lblBeforeImage.setIcon(null);
					lblAfterImage.setIcon(null);
				}
				else {
					disableSeekMessgeComponents();
					btnSelectImage.setEnabled(false);
				}
			}
		});
		chckbxSeek.setText("I would like to seek message in the imported image.");
		chckbxSeek.setBounds(580, 570, 340, 20);
		chckbxSeek.setFocusable(false);
		
		getContentPane().add(chckbxSeek);
	}
	
	/*
	 * This function enable the components that are related to message extraction.
	 * */
	private void enableSeekMessageComponents() {
		txtIV_d.setEnabled(true);
		txtIV_d.setBackground(Color.WHITE);
		txtPassword_d.setEnabled(true);
		txtPassword_d.setBackground(Color.WHITE);
		lblInitialVector_d.setEnabled(true);
		lblKeySize_d.setEnabled(true);
		lblRequestPassword_d.setEnabled(true);
		rdbtnAES128_d.setEnabled(true);
		rdbtnAES192_d.setEnabled(true);
		rdbtnAES256_d.setEnabled(true);
		chckbxDecompress.setEnabled(true);
	}
	
	/*
	 * This function disable the components that are related to message extraction.
	 * */
	private void disableSeekMessgeComponents() {
		chckbxSeek.setSelected(false);
		txtIV_d.setEnabled(false);
		txtIV_d.setText("");
		txtIV_d.setBackground(Color.decode("#D0D0D0"));
		txtPassword_d.setEnabled(false);
		txtPassword_d.setText("");
		txtPassword_d.setBackground(Color.decode("#D0D0D0"));
		lblInitialVector_d.setEnabled(false);
		lblKeySize_d.setEnabled(false);
		lblRequestPassword_d.setEnabled(false);
		rdbtnAES128_d.setEnabled(false);
		rdbtnAES128_d.setSelected(true);
		rdbtnAES192_d.setEnabled(false);
		rdbtnAES256_d.setEnabled(false);
		chckbxDecompress.setEnabled(false);
	}
	
	/*
	 * This function display done button and reacts to push event.
	 * */
	private void doneButton() {
		this.btnDone = new JButton("Done");
		btnDone.setFocusable(false);
		btnDone.setBounds(950, 784, 130, 30);
		btnDone.setEnabled(false);
		btnDone.addActionListener(new ActionListener() {

			//@Override
			public void actionPerformed(ActionEvent e) {
				if(chckbxSeek.isSelected()) 
					extractText();
					else if(chckbxHideMessage.isSelected()) 
							embedText();
			}
			
		});
		
		getContentPane().add(btnDone);
	}
	
	/*
	 * This fucntion is executed when the user pressed the done button and request to extract text from the image.
	 * */
	private void extractText() {

		char[] pass = txtPassword_d.getText().toCharArray();
		try {
			BufferedImage bi = ImageIO.read(new File(imagePath));
	        byte[] imageBytes = SteganographyHelper.imageToByteArray(bi, "bmp");
			salt = FileAccess.readSaltFromFile(imageBytes);
		} 
		catch (FileNotFoundException e) {JOptionPane.showMessageDialog(null, "Failed to read salt from file" , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (UnsupportedEncodingException e) {JOptionPane.showMessageDialog(null, "Failed to read salt from file" , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (IOException e) { JOptionPane.showMessageDialog(null, "Failed to read image" , "Error!", JOptionPane.ERROR_MESSAGE); return; } 
		catch (NoSuchAlgorithmException e) { JOptionPane.showMessageDialog(null, "Can't hash the image" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
		
		byte[] IV;
		try {
			IV = txtIV_d.getText().getBytes("UTF-8");
		} 
		catch (UnsupportedEncodingException e2) { JOptionPane.showMessageDialog(null, "Failed to convert String to byte[]" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
		
		byte[] key;
		try {
			key = Crypt.getAESKeyFromPassword(pass, salt);
		}
		catch (NoSuchAlgorithmException e1) { JOptionPane.showMessageDialog(null, "Failed to derieve key from password" , "Error!", JOptionPane.ERROR_MESSAGE);  return;} 
		catch (InvalidKeySpecException e1) { JOptionPane.showMessageDialog(null, "Failed to derieve key from password" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
		
		String message;
		try {
			message = SteganographyHelper.extractText(imagePath, key, IV, chckbxDecompress.isSelected());
			JOptionPane.showMessageDialog(null, "The content of your message is:\n" + message, "Secret Message was found!", JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (DecompressException e) {JOptionPane.showMessageDialog(null, "Failed to decompress the message" , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (IOException e) {JOptionPane.showMessageDialog(null, "Failed to read image" , "Error!", JOptionPane.ERROR_MESSAGE); return; } 
		catch (MessageNotFound e) {JOptionPane.showMessageDialog(null, "Secret message was not found" , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (InflaterException e) {JOptionPane.showMessageDialog(null, "Rare Inflater issue happened.\nPlease try to embed text again.\nFor more details see https://stackoverflow.com/questions/55366393/java-inflater-will-loop-infinitely-sometimes" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
	}
	
	/*
	 * This function is executed when the user pressed the done button and request to embed text in an image.
	 * */
	private void embedText() {
		byte[] IV, textToHide, key;
		try {
			textToHide = txtMessage.getText().getBytes("UTF-8");
			IV = txtIV.getText().getBytes("UTF-8");
		} 
		catch (UnsupportedEncodingException e2) { JOptionPane.showMessageDialog(null, "Failed to convert String to byte[]" , "Error!", JOptionPane.ERROR_MESSAGE); return;}
		
		try {
			salt = Crypt.getRandomNonce();
			char[] pass = txtPassword.getText().toCharArray();
			key = Crypt.getAESKeyFromPassword(pass, salt);
		} 
		catch (NoSuchAlgorithmException e1) { JOptionPane.showMessageDialog(null, "Failed to derieve key from password" , "Error!", JOptionPane.ERROR_MESSAGE);  return;} 
		catch (InvalidKeySpecException e1) { JOptionPane.showMessageDialog(null, "Failed to derieve key from password" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
		
		try {
			imageWithMessage = SteganographyHelper.embedText(textToHide, imagePath, key, IV ,chckbxCompress.isSelected());
			btnSaveImage.setEnabled(true);
		} 
		catch (IOException e1) { JOptionPane.showMessageDialog(null, "Failed to read image" , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (OutputStreamException e) {  JOptionPane.showMessageDialog(null, "ByteArrayOutputStream exception." , "Error!", JOptionPane.ERROR_MESSAGE); return;} 
		catch (CompressException e) {JOptionPane.showMessageDialog(null, "Faild to compress the message" , "Error!", JOptionPane.ERROR_MESSAGE); return; }
		
		//display image
		Image img;
		try {
			img = ImageIO.read(new ByteArrayInputStream(imageWithMessage));
    		ImageIcon icon = new ImageIcon(img);
    		lblAfterImage.setIcon(icon);
    		JOptionPane.showMessageDialog(null, "Your message was successfully hidden\nFirst hit sucess rate:" + SteganographyHelper.getFirstTryHitSuccessRate(), "Notification", JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (IOException e1)  { JOptionPane.showMessageDialog(null, "Failed to read image" , "Error!", JOptionPane.ERROR_MESSAGE); return;}
	}
	
	/*
	 * This function display the clear fields button and reacts to mouse press events.
	 * */
	private void cleanFields() {
		btnClean = new JButton("Clean fields");
		btnClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				disableHideMessageComponents();
				disableSeekMessgeComponents();
				lblBeforeImage.setIcon(null);
				lblAfterImage.setIcon(null);
				btnDone.setEnabled(false);
				btnSaveImage.setEnabled(false);
			}
		});
		btnClean.setFocusable(false);
		btnClean.setBounds(532, 784, 130, 30);
		getContentPane().add(btnClean);
	}
	
	/*
	 * This function checks if the form is filled correctly.
	 * */
	private boolean isFormCompleted() {
		if(chckbxHideMessage.isSelected()) {
			if(lblBeforeImage.getIcon() == null)
				return false;
			if(txtMessage.getText().length() == 0)
				return false;
			if(!suggestedPassword.isMeetTheRequirements())
				return false;
			if(txtIV.getText().length() == 16)
				return true;
		}
		if(chckbxSeek.isSelected()) {
			if(lblAfterImage.getIcon() == null)
				return false;
			if(txtPassword_d.getText().length() == 0)
				return false;
			if(txtIV_d.getText().length() == 16)
				return true;
		}
		return false;
	}
}
