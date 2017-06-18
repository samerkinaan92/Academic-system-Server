package application;

import ocsf.server.ConnectionToClient;

/**
 * class for saving file info to be sent
 * 
 * @author Samer kinaan
 *
 */
public class FileInfo {
	/**
	 * directory number that the file will be saved in
	 */
	private int dir = 0;
	
	/**
	 * file name
	 */
	private String fileName;
	
	/**
	 * client id
	 */
	private ConnectionToClient client;
	
	/**
	 * Constructor for building FileInfo 
	 * @param dir	directory number that the file will be saved in
	 * @param fileName 	file name
	 * @param client	client id
	 */
	public FileInfo(int dir, String fileName, ConnectionToClient client) {
		this.dir = dir;
		this.fileName = fileName;
		this.client = client;
	}
	
	/**
	 * gets directory number
	 * @return directory number
	 */
	public int getDir() {
		return dir;
	}
	
	/**
	 * 
	 * @return	file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * 
	 * @return	connection id
	 */
	public ConnectionToClient getClient() {
		return client;
	}
	
	
}
