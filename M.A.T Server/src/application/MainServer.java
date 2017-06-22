package application;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ocsf.server.*;
import java.util.Date;



/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Samer kinaan
 * @version July 2017
 */
public class MainServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  private LogController logController;
  private Connection DBConn;
  private String assFilesDirPath = "C:\\M.A.T files\\assignments\\";
  private String subFilesDirPath = "C:\\M.A.T files\\submissions\\";
  private ArrayList<FileInfo> fileToBeSent = new ArrayList<>();
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public MainServer(int port) 
  {
	  super(port);
  }

  
  	//Instance methods ************************************************
  
  	/**
   	* This method handles any messages received from the client.
   	*
   	* @param msg The message received from the client.
   	* @param client The connection from which the message originated.
   	*/
  	public void handleMessageFromClient(Object msg, ConnectionToClient client){
  		
  		if(msg instanceof byte[]){
  			byte byteArray[] = (byte[])msg;
  			try {
				saveFile(byteArray, client);
			} catch (IOException e) {
				logController.showMsg("Error: unable to send message to client.");
				e.printStackTrace();
			}
  		}else if(msg instanceof HashMap<?, ?>){
		  	@SuppressWarnings("unchecked")
			HashMap<String, String> clientMsg = (HashMap<String, String>) msg;
		  
		  	// shows the received msg to the event log
		  	logController.showMsg("Message received: " + clientMsg.get("msgType") + " from " + client);
		
		  
		  	//check the msg type
		  	if(clientMsg.get("msgType").equals("Login")){
				login(clientMsg,client);
		  	}else if(clientMsg.get("msgType").equals("select")){
				selectQuery(clientMsg, client);
			}else if(clientMsg.get("msgType").equals("update")){
				updateQuery(clientMsg, client);
			}else if(clientMsg.get("msgType").equals("delete")){
				updateQuery(clientMsg, client);
			}else if(clientMsg.get("msgType").equals("insert")){
				updateQuery(clientMsg, client);
			}else if(clientMsg.get("msgType").equals("fileInfo")){
				try {
					saveFileInfo(clientMsg, client);
				} catch (IOException e) {
					logController.showMsg("Error: unable to send message to client.");
					e.printStackTrace();
				}
			}else if(clientMsg.get("msgType").equals("getFile")){
				getFile(clientMsg, client);
			}
  		}
  	}

  	/**
  	 * sends the requested file to the client
  	 * 
  	 * @param clientMsg	The message received from the client.
  	 * @param client The connection from which the message originated.
  	 */
  	private void getFile(HashMap<String, String> clientMsg, ConnectionToClient client){
  		Path path = Paths.get(clientMsg.get("filePath"));
  		try {
			byte[] data = Files.readAllBytes(path);
			client.sendToClient(data);
			logController.showMsg("File was sent to client: " + client);
		} catch (IOException e) {
			logController.showMsg("Failed to send the file to client: " + client);
			e.printStackTrace();
		}
  	}
  	
  	/**
  	 * saves all the info of the file that will be sent to the server later
  	 * 
  	 * @param clientMsg The message received from the client.
  	 * @param client The connection from which the message originated.
  	 * @throws IOException
  	 */
  	private void saveFileInfo(HashMap<String, String> clientMsg, ConnectionToClient client) throws IOException {
  		if(clientMsg.get("dir").equals("assignment")){
  			fileToBeSent.add(new FileInfo(1, clientMsg.get("fileName"), client));
			client.sendToClient(true);
			logController.showMsg("File info was saved.");
  		}else if(clientMsg.get("dir").equals("submission")){
  			fileToBeSent.add(new FileInfo(2, clientMsg.get("fileName"), client));
			client.sendToClient(true);
			logController.showMsg("File info was saved.");
  		}else{
  			client.sendToClient(false);
			logController.showMsg("Error: failed to save file info");
  		}
	}
  	
  	
  	/**
  	 * save the sent file in the server hard drive
  	 * 
  	 * @param byteArray The byte array containing all the file data
  	 * @param client The connection from which the message originated.
  	 * @throws IOException
  	 */
  	private void saveFile(byte byteArray[], ConnectionToClient client) throws IOException{
  		logController.showMsg("bytes[] recieved from: " + client);
  		FileInfo info = null;
  		
  		for(int i = 0; i < fileToBeSent.size(); i++){
  			if(fileToBeSent.get(i).getClient().equals(client))
  				info = fileToBeSent.get(i);
  		}
  		
  		if(info != null){
			FileOutputStream stream;
			String path;
			if(info.getDir() == 1)
				path = assFilesDirPath;
			else
				path = subFilesDirPath;
			
			try {
				stream = new FileOutputStream(path + info.getFileName());
				stream.write(byteArray);
				stream.close();
				logController.showMsg("File was saved successfully.");
				client.sendToClient(path + info.getFileName());
			} catch (IOException e) {
				logController.showMsg("Error: unable to save file.");
				client.sendToClient(null);
				e.printStackTrace();
			}
  		}else{
  			client.sendToClient(null);
  			logController.showMsg("Error: unable to find file info.");
  		}
  	}
  
  	
  	/**
  	 * login request from client.
  	 * 
  	 * @return true if accepted, false else
  	 * 
  	 * @param clientMsg The message received from the client.
  	 * @param client The connection from which the message originated.
  	 */
  private void login(HashMap<String, String> clientMsg, ConnectionToClient client) {
	String id, password, school;
	Statement stmt;
	HashMap<String, String> serverMsg = new HashMap<String, String>();
	
	
	id = clientMsg.get("id");
	password = clientMsg.get("passwrd");
	school = clientMsg.get("schoolId");
	
	
	
	String query = "select * from users where ID='" + id + "';";
	
	//checks if selected school MAT
	if(school.equals("MAT")){
		try {
			stmt = DBConn.createStatement();
			ResultSet result = stmt.executeQuery(query);
			
			if(result.next()){
				//if user found
				if(!result.getBoolean(7)){
					//if user is not blocked
					if(result.getString(3).equals(password)){
						//if password is vlaid
						if(!result.getBoolean(5)){
							//if user is not logged in
							serverMsg.put("Valid", "true");
							serverMsg.put("Type", result.getString(4));
							serverMsg.put("Name", result.getString(2));
							stmt.executeUpdate("UPDATE users SET isLogin = 1, numoftries = 0 WHERE id = '" + id + "';");
						}else{
							//user is already logged in
							serverMsg.put("Valid", "false");
							serverMsg.put("ErrMsg", "User already loged in.");
						}
					}else{
						// user password is wrong
						int tryNum = result.getInt(6);
						if(tryNum >= 2){
							//user entered wrong password 3 times
							stmt.executeUpdate("UPDATE users SET isBlocked = 1, lastLogin = now() WHERE id = '" + id + "';");
							serverMsg.put("Valid", "false");
							serverMsg.put("ErrMsg", "User is blocked, try agian in 30 minutes.");
						}else{
							serverMsg.put("Valid", "false");
							serverMsg.put("ErrMsg", "Password or ID is incorrect.");
						}
						//increment number of wrong password in DB
						stmt.executeUpdate("UPDATE users SET numoftries = numoftries + 1 WHERE id = '" + id + "';");
					}
				}else{
					//user was blocked
					Date date;
					Timestamp timestamp = result.getTimestamp(8);
					date = new java.util.Date(timestamp.getTime());
					Date now = new Date();
					long diff = now.getTime() - date.getTime();
					if(diff > 1000*60*30){
						//if 30 minutes has passed reset block
						stmt.executeUpdate("UPDATE users SET isBlocked = 0, numoftries = 0 WHERE id = '" + id + "';");
						if(result.getString(3).equals(password)){
							//user password is valid, gives the client permeation to log in
							serverMsg.put("Valid", "true");
							serverMsg.put("Type", result.getString(4));
							serverMsg.put("Name", result.getString(2));
							stmt.executeUpdate("UPDATE users SET isLogin = 1 WHERE id = '" + id + "';");
						}else{
							//user password is wrong
							stmt.executeUpdate("UPDATE users SET numoftries = numoftries + 1 WHERE id = '" + id + "';");
							serverMsg.put("Valid", "false");
							serverMsg.put("ErrMsg", "Password or ID is incorrect.");
						}
					}else{
						//user is still blocked
						serverMsg.put("Valid", "false");
						serverMsg.put("ErrMsg", "User is blocked, try agian in " + (30 - (diff / (1000 * 60))) + " minutes.");
					}
				}
			}else{
				//user id was not found
				serverMsg.put("Valid", "false");
				serverMsg.put("ErrMsg", "Password or ID is incorrect.");
			}
		} catch (Exception e) {
			logController.showMsg("ERROR: server could not execute the query");
			e.printStackTrace();
		}
	}else{
		//school is not found
		serverMsg.put("Valid", "false");
		serverMsg.put("ErrMsg", "School is not found.");
	}
	try{
	client.sendToClient(serverMsg);
	logController.showMsg("Message sent to client: " + client);
	} catch (Exception e) {
		logController.showMsg("ERROR: server failed to send message to client");
		e.printStackTrace();
	}
  }
  
  /**
   * @return array list of the data requested by the client 
   * @param clientMsg The message received from the client.
   * @param client The connection from which the message originated.
   */
  private void selectQuery(HashMap<String, String> clientMsg, ConnectionToClient client){
	  Statement stmt;
	  
	  ArrayList<String> arrayList = new ArrayList<String>();
	 
	  //execute the query and translate the result to array list
	  try {
			stmt = DBConn.createStatement();
			ResultSet result = stmt.executeQuery(clientMsg.get("query"));
			
			
			/*Counting the number of columns*/
		    int numberOfColumns = result.getMetaData().getColumnCount();
		    
			    /*Converting resaultSet into arraylist*/
				while (result.next()) {              
				        int i = 1;
				        while(i <= numberOfColumns) {
				            arrayList.add(result.getString(i++));
				        }
				}
		} catch (Exception e) {
			logController.showMsg("ERROR: server could not execute the query");
			e.printStackTrace();
		}
	  
	  // return the result to client
	  try {		  
		  client.sendToClient(arrayList);
		  logController.showMsg("Message sent to client: " + client);
	  } catch (IOException e) {
		  logController.showMsg("ERROR: server failed to send message to client");
		  e.printStackTrace();
	  }
  }
  
  /**
   * @return the number of rows affected 
   * 
   * @param clientMsg The message received from the client.
   * @param client The connection from which the message originated.
   */
  private void updateQuery(HashMap<String, String> clientMsg, ConnectionToClient client){
	  Statement stmt;
	  int result = 0;
	  //execute the query and return the number of effected rows to client
	  try {
  		stmt = DBConn.createStatement();
  		result = stmt.executeUpdate(clientMsg.get("query"));
		} catch (Exception e) {
			logController.showMsg("ERROR: server could not execute the query");
			e.printStackTrace();
		}
	  
	  try {		  
		  client.sendToClient(result);
		  logController.showMsg("Message sent to client: " + client);
	  } catch (IOException e) {
		  logController.showMsg("ERROR: server failed to send message to client");
		  e.printStackTrace();
	  }
  }

    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
	  logController.showMsg("Server listening for connection on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    logController.showMsg
      ("Server has stopped listening for connections.");
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server connects to a client.
   */
  @Override
  protected void clientConnected(ConnectionToClient client) {
	  logController.showMsg("Client: " + client + " has connected");
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server disconnects from a client.
   */
  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
	  logController.showMsg("Client: " + client + " has been disconnected");
  }
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   * 
   * @param user User of the DB
   * @param password Password for the DB
   * @param sqlPort	The port that mysql is listening on
   * @throws IOException
   */
  public void setServerCon(String user, String password, int sqlPort) throws IOException
  {
    //open log events controller
  	openLogEventGUI();
  	setFilesDir();
    try 
	{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    } catch (Exception ex) {/* handle the error*/}
 
  	
  	//connect to DB
    try 
    {
    	String driverPath = "jdbc:mysql://localhost:" + sqlPort + "/mat";
        DBConn = DriverManager.getConnection(driverPath,user,password);
        logController.showMsg("SQL connection succeed");
    }catch (SQLException ex) 
	    {/* handle any errors*/
    	logController.showMsg("SQL connection failed");
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
        }
    
    try 
    {
    	this.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      logController.showMsg("ERROR - Could not listen for clients!");
    }
  }
  
  /**
   * open the log event GUI
   */
  private void openLogEventGUI(){
	//open log events controller
	  	try {
	  		Stage primaryStage = new Stage();
	  		primaryStage.setTitle("MainServer log system");
	  		primaryStage.getIcons().add(new Image("/server_earth.png"));
	  	  	FXMLLoader loader = new FXMLLoader();
	  	  	Pane root;
	  		root = loader.load(getClass().getResource("LogController.fxml").openStream());
	  	  	
	  	  	Scene scene = new Scene(root);			
	  	  	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	  	  	
	  	  	logController = loader.getController();
	  	  	logController.setIp(getPort());
	  	  	primaryStage.setScene(scene);	
	  	  	primaryStage.setResizable(false);
	  	  	primaryStage.show();
	  	} catch (IOException e) {
	  		JOptionPane.showMessageDialog(null, 
					  "Failed to open log view!", "ERROR", JOptionPane.ERROR_MESSAGE);
	  		e.printStackTrace();
	  	}
  }
  
  /**
   * sets the directory that the file will be saved in 
   */
  private void setFilesDir(){
	  File assFiles = new File(assFilesDirPath);
	  File subFiles = new File(subFilesDirPath);
      if (!assFiles.exists()) {
          if (assFiles.mkdirs() && subFiles.mkdir()) {
        	  logController.showMsg("M.A.T files directory was created.");
          } else {
        	  logController.showMsg("Failed to create M.A.T files directory!");
        	  logController.showMsg("Server will shutdown in 5 seconds!!");
        	  try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				
			}
        	  System.exit(0);
          }
      }else{
    	  logController.showMsg("M.A.T files directory already exists.");
      }
  }
}
//End of MainServer class
