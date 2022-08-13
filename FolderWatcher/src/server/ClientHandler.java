package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import main.ActionData;

public class ClientHandler extends Thread {
	ObjectOutputStream oos;
	ObjectInputStream ois;
	Socket socket;
	IClientAction clientAction;
	public ClientHandler(ObjectOutputStream oos, ObjectInputStream ois, Socket socket, IClientAction clientAction) {
		super();
		this.oos = oos;
		this.ois = ois;
		this.socket = socket;
		this.clientAction = clientAction;
	}
	
	@Override
	public void run() {
		
		try {
			while (true) {
				ActionData actionData = (ActionData) ois.readObject();
				clientAction.onAction(actionData);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	public void changeFolder(String path) {
		try {
			oos.writeObject(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void logout() {
		try {
			ois.close();
			oos.close();
			if (socket != null && socket.isConnected()) {
				socket.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
