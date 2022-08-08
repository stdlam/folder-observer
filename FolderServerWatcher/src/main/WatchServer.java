package main;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.List;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;

public class WatchServer {

	private JFrame frame;
	private JLabel lblIP;
	private JLabel lblPort;
	private JLabel lblLogcat;
	private JButton btnStop;
	private JList<String> listStatus;
	JList<String> listClient;
	private DefaultListModel<String> statusModel = new DefaultListModel<String>();
	private DefaultListModel<String> clientModel = new DefaultListModel<String>();
	
	//static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int PORT = 9876;
    
    private Thread connectingThread = null;
    private Runnable watchingRunnalbe = new Runnable() {

		@Override
		public void run() {
			//create the socket server object
	        try {
				server = new ServerSocket(PORT);
				//keep listens indefinitely until receives 'exit' call or program terminates
		        while (true) {
		            statusModel.addElement("Waiting for the client request");
		            //creating socket and waiting for client connection
		            Socket socket = server.accept();
		            //read from socket to ObjectInputStream object
		            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		            //convert ObjectInputStream object to String
		            String message = (String) ois.readObject();
		            statusModel.addElement("Client " + message + " asked for joining.");
		            ClientData cData = new ClientData("", message);
		            clientModel.addElement(cData.toString());
		            //create ObjectOutputStream object
		            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		            //write object to Socket
		            oos.writeObject("Hello, you're accepted.");
		            statusModel.addElement("Client " + message + " was accepted.");
		            //close resources
		            ois.close();
		            oos.close();
		            socket.close();
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
	}
	
	private void suspendConnecting() {
		connectingThread = new Thread(watchingRunnalbe);
		connectingThread.start();
	}
	
	private void stopConnection() {
		connectingThread.interrupt();
		//close the ServerSocket object
        try {
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
		
		lblLogcat = new JLabel("Logcat");
		lblLogcat.setBounds(6, 352, 66, 16);
		frame.getContentPane().add(lblLogcat);
		
		btnStop = new JButton("Stop Server");
		btnStop.setBounds(69, 102, 117, 29);
		frame.getContentPane().add(btnStop);
		
		listStatus = new JList<String>(statusModel);
		listStatus.setVisibleRowCount(8);
		listStatus.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(listStatus);
		listScroller.setBounds(6, 380, 678, 203);
		frame.getContentPane().add(listScroller);
		
		listClient = new JList<String>(clientModel);
		listClient.setVisibleRowCount(8);
		listClient.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane clientScroller = new JScrollPane(listClient);
		clientScroller.setBounds(349, 48, 335, 320);
		frame.getContentPane().add(clientScroller);
		
		textField = new JTextField();
		textField.setBounds(456, 16, 228, 26);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(339, 16, 117, 29);
		frame.getContentPane().add(btnSearch);
		
		JButton btnDirChange = new JButton("Change Folder");
		btnDirChange.setBounds(228, 339, 117, 29);
		frame.getContentPane().add(btnDirChange);
	}
}
