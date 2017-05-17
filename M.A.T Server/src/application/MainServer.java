package application;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ocsf.server.*;



/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class MainServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  LogController logController;
  Connection DBConn;
  
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
	  
	  
  	HashMap<String, String> clientMsg = (HashMap<String, String>) msg;
  
  	// shows the received msg to the event log
  	logController.showMsg("Message received: " + clientMsg.get("msgType") + " from " + client);

  
  	//check the msg type
	if(clientMsg.get("msgType").equals("select")){
		selectQuery(clientMsg, client);
	}else if(clientMsg.get("msgType").equals("update")){
		updateQuery(clientMsg, client);
	}else if(clientMsg.get("msgType").equals("Login")){
		login(clientMsg,client);
	}
  }
  
  private void login(HashMap<String, String> clientMsg, ConnectionToClient client) {
	String id, password,school;
	Statement stmt;
	HashMap<String, String> serverMsg = new HashMap<String, String>();
	
	
	id = clientMsg.get("id");
	password = clientMsg.get("passwrd");
	school = clientMsg.get(clientMsg.get("schoolId"));
	
	
	
	String query = "use "+school+ ";" + "select Name, Type, isLogin from users where ID='" + id + "' AND Password='"+password+"';";
	
	try {
		stmt = DBConn.createStatement();
		ResultSet result = stmt.executeQuery(query);
		if(result.next()){
			if(!result.getBoolean(3)){
				serverMsg.put("Valid", "true");
				serverMsg.put("Type", result.getString(2));
				serverMsg.put("Name", result.getString(1));
				
			}else{
				serverMsg.put("Valid", "false");
				serverMsg.put("ErrMsg", "User allready loged in.");
			}
		}else{
			serverMsg.put("Valid", "false");
			serverMsg.put("ErrMsg", "Password or ID is incorrect.");
		}
		client.sendToClient(serverMsg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}
  
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
			//e.printStackTrace();
		}
	  
	  // return the result to client
	  try {		  
		  client.sendToClient(arrayList);
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		  System.out.println("\nCould not sent message to client.");
	  }
  }
  
  private void updateQuery(HashMap<String, String> clientMsg, ConnectionToClient client){
	  Statement stmt;
	  //execute the query and return the number of effected rows to client
	  try {
  		stmt = DBConn.createStatement();
  		int result = stmt.executeUpdate(clientMsg.get("query"));
			client.sendToClient(result);
		} catch (Exception e) {
			logController.showMsg("ERROR: server could not execute the query");
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
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
 * @throws IOException 
   */
  public void setServerCon(String user, String password, String portStr) throws IOException
  {
    //open log events controller
  	openLogEventGUI();
    
    try 
	{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    } catch (Exception ex) {/* handle the error*/}
 
  	
  	//connect to DB
    try 
    {
        DBConn = DriverManager.getConnection("jdbc:mysql://localhost/mat",user,password);
        logController.showMsg("SQL connection succeed");
    }catch (SQLException ex) 
	    {/* handle any errors*/
        logController.showMsg("SQLException: " + ex.getMessage());
        logController.showMsg("SQLState: " + ex.getSQLState());
        logController.showMsg("VendorError: " + ex.getErrorCode());
        }

    try
    {
    }
    catch(Throwable t)
    {
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
  
  private void openLogEventGUI(){
	//open log events controller
	  	try {
	  		Stage primaryStage = new Stage();
	  		primaryStage.setTitle("EchoServer log system");
	  		primaryStage.getIcons().add(new Image("/server_earth.png"));
	  	  	FXMLLoader loader = new FXMLLoader();
	  	  	Pane root;
	  		root = loader.load(getClass().getResource("LogController.fxml").openStream());
	  	  	
	  	  	Scene scene = new Scene(root);			
	  	  	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	  	  	
	  	  	logController = loader.getController();
	  	  	primaryStage.setScene(scene);	
	  	  	primaryStage.show();
	  	} catch (IOException e) {
	  		// TODO Auto-generated catch block
	  		e.printStackTrace();
	  	}
  }
  
}
//End of EchoServer class
