package application;

import ocsf.server.ConnectionToClient;

public class FileInfo {
	private int dir = 0;
	private String fileName;
	private ConnectionToClient client;
	
	public FileInfo(int dir, String fileName, ConnectionToClient client) {
		this.dir = dir;
		this.fileName = fileName;
		this.client = client;
	}
	
	public int getDir() {
		return dir;
	}
	public String getFileName() {
		return fileName;
	}
	
	public ConnectionToClient getClient() {
		return client;
	}
	
	
}
