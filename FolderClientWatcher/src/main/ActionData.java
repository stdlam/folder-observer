package main;

public class ActionData {
	long timestamp;
	String action;
	String clientIP;
	String message;
	
	public ActionData(long timestamp, String action, String clientIP, String message) {
		super();
		this.timestamp = timestamp;
		this.action = action;
		this.clientIP = clientIP;
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getClientIP() {
		return clientIP;
	}
	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "timestamp=" + timestamp + "|action=" + action + "|clientIP=" + clientIP + "|message=" + message;
	}
	
	
}
