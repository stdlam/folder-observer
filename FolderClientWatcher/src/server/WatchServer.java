package server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.List;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import main.Action;
import main.ActionData;
import main.FileTreeModel;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JTable;

public class WatchServer {

	private JFrame frame;
	private JLabel lblIP;
	private JLabel lblPort;
	private JLabel lblLogcat;
	private JButton btnStop;
	JList<String> listClient;
	private DefaultListModel<String> statusModel = new DefaultListModel<String>();
	private DefaultListModel<String> clientModel = new DefaultListModel<String>();
	private Hashtable<String, Socket> clientHolder = new Hashtable<String, Socket>();
	private Hashtable<String, FileTreeModel> folderHolder = new Hashtable<String, FileTreeModel>();
	private DefaultTableModel tableModel = new DefaultTableModel(0, 0);
	private TableRowSorter<TableModel> rowSorter;
	private static final String LOGCAT_PATH = "server_logcat.txt";
	
	Socket socket;
	//static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int PORT = 9876;
    
    private Thread connectingThread = null;
    
    private Runnable socketRunnable = new Runnable() {

		@Override
		public void run() {
			//create the socket server object
	        try {
				server = new ServerSocket(PORT);
				
		        while (true) {
		            statusModel.addElement("Waiting for the client request");
		            //creating socket and waiting for client connection
		            socket = server.accept();
		           
		            //read from socket to ObjectInputStream object
		            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		            //convert ObjectInputStream object to String
		            ActionData actionData = (ActionData) ois.readObject();
		            //String[] msg = messages.split(",");
		            String createAt = actionData.getCreateAt();
		            String action = actionData.getAction();
		            String clientIP = actionData.getClientIP();
		            String message = actionData.getMessage();
		            System.out.println(message + ", action=" + action);
		            
		            switch (action) {
						case Action.LOGIN: {
							folderHolder.put(clientIP, actionData.getFolderTree());
							holdClient(clientIP, socket);
			            	addRowLog(createAt, action, message);
			            	writeLog(LOGCAT_PATH, actionData.toString(), true);
			            	// ClientData cData = new ClientData(message, clientIP);
				            clientModel.addElement(clientIP);
				          //create ObjectOutputStream object
				            ObjectOutputStream oos = new ObjectOutputStream(clientHolder.get(clientIP).getOutputStream());
				            //write object to Socket
				            oos.writeObject("Hello, you're accepted.");
				            oos.close();
				            break;
						}
						case Action.LOGOUT: {
			            	addRowLog(createAt, action, message);
			            	writeLog(LOGCAT_PATH, actionData.toString(), true);
			            	ClientData cData = new ClientData(message, clientIP);
			            	
				            for (int i = 0; i < clientModel.getSize(); i++) {
				            	if (clientModel.get(i).contains(clientIP)) {
									clientModel.remove(i);
									break;
								}
				            }
				            
				            logout(clientIP);
				            break;
						}
						case Action.FOLDER_TREE: {
							
							break;
						}
						default: {
							addRowLog(createAt, action, message);
							break;
						}
					}
		            
		            //close resources
		            ois.close();
		            //socket.close();
		        }
		    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
			
		}
    	
    };
    private JTextField textField;
    private JTable tableLog;
    private JTextField textFieldLogcatFilter;
    
    private void showTree(String ip) {
    	JTree tree = new JTree(folderHolder.get(ip));
    	// The JTree can get big, so allow it to scroll.
	    JScrollPane scrollpane = new JScrollPane(tree);
	    
	    // Display it all in a window and make the window appear
	    JFrame frame = new JFrame("Folder Chooser");
	    frame.getContentPane().add(scrollpane, "Center");
	    frame.setSize(400,600);
	    frame.setVisible(true);
    }
    
    private void holdClient(String ip, Socket client) {
    	clientHolder.remove(ip);
		clientHolder.put(ip, client);
    }
    
    private void logout(String ip) {
    	try {
			clientHolder.get(ip).close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
        clientHolder.remove(ip);
    }
    
    private void initLogTable() {
		String[] header = new String[] { "No.", "Time", "Action", "Description"};
		tableModel.setColumnIdentifiers(header);
		tableLog.setModel(tableModel);
		rowSorter = new TableRowSorter<>(tableLog.getModel());
		tableLog.setRowSorter(rowSorter);
	}
    
    private void addRowLog(String createAt, String action, String message) {
		int rowCount = tableModel.getRowCount();
		tableModel.addRow(new Object[] { String.valueOf(++rowCount), createAt, action, message });
	}
    
    private void writeLog(String filePath, String line, boolean isAppend) {
		try {
			FileWriter fw = new FileWriter(filePath, isAppend);
			 
			fw.write(line);
			fw.write("\n");
			fw.close();
			
			System.out.println("wrote data to " + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void setupFilterEvents() {
		textFieldLogcatFilter.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				String text = textFieldLogcatFilter.getText();
				if (text.trim().length() == 0) {
					rowSorter.setRowFilter(null);
				} else {
					rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				String text = textFieldLogcatFilter.getText();
				if (text.trim().length() == 0) {
					rowSorter.setRowFilter(null);
				} else {
					rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
				}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				
			}
		});
	}
    
    private void generateServerConnectionInfo() {
    	InetAddress idd;
		try {
			idd = InetAddress.getLocalHost();
			String ip = idd.getHostAddress();
			lblIP.setText(ip);
			lblPort.setText("" + PORT);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void handleClickEvents() {
    	btnStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stopConnection();
				
			}
    		
    	});
    }
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WatchServer window = new WatchServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WatchServer() {
		initialize();
		generateServerConnectionInfo();
		suspendConnecting();
		handleClickEvents();
		initLogTable();
		setupFilterEvents();
	}
	
	private void suspendConnecting() {
		connectingThread = new Thread(socketRunnable);
		connectingThread.start();
	}
	
	private void stopConnection() {
		connectingThread.interrupt();
		//close the ServerSocket object
        try {
        	socket.close();
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {	
		frame = new JFrame();
		frame.setBounds(100, 100, 690, 613);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblIntro = new JLabel("Use this port, IP to connect your client...");
		lblIntro.setBounds(16, 17, 284, 25);
		frame.getContentPane().add(lblIntro);
		
		JLabel lblNewLabel = new JLabel("IP");
		lblNewLabel.setBounds(59, 54, 61, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPortText = new JLabel("PORT");
		lblPortText.setBounds(59, 74, 61, 16);
		frame.getContentPane().add(lblPortText);
		
		lblIP = new JLabel("123456789");
		lblIP.setBackground(new Color(255, 255, 0));
		lblIP.setBounds(121, 54, 166, 16);
		frame.getContentPane().add(lblIP);
		
		lblPort = new JLabel("123456789");
		lblPort.setBounds(121, 74, 166, 16);
		frame.getContentPane().add(lblPort);
		
		lblLogcat = new JLabel("Logcat Filter");
		lblLogcat.setBounds(6, 352, 101, 16);
		frame.getContentPane().add(lblLogcat);
		
		btnStop = new JButton("Stop Server");
		btnStop.setBounds(69, 102, 117, 29);
		frame.getContentPane().add(btnStop);
		
		tableLog = new JTable();
		JScrollPane listScroller = new JScrollPane(tableLog);
		listScroller.setBounds(6, 380, 678, 203);
		frame.getContentPane().add(listScroller);
		
		listClient = new JList<String>(clientModel);
		listClient.setVisibleRowCount(8);
		listClient.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane clientScroller = new JScrollPane(listClient);
		clientScroller.setBounds(349, 48, 335, 278);
		frame.getContentPane().add(clientScroller);
		
		textField = new JTextField();
		textField.setBounds(456, 16, 228, 26);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(339, 16, 117, 29);
		frame.getContentPane().add(btnSearch);
		
		JButton btnDirChange = new JButton("Change Folder");
		btnDirChange.setEnabled(false);
		btnDirChange.setBounds(567, 328, 117, 29);
		frame.getContentPane().add(btnDirChange);
		
		textFieldLogcatFilter = new JTextField();
		textFieldLogcatFilter.setBounds(100, 347, 251, 26);
		frame.getContentPane().add(textFieldLogcatFilter);
		textFieldLogcatFilter.setColumns(10);
	}
}
