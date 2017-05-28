package application;
	
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;


public class MainServerGUI extends Application implements Initializable {
	
	@FXML // fx:id="userTxtFld"
	private TextField userTxtFld = new TextField(); // Value injected by FXMLLoader

    @FXML // fx:id="portTxtFld"
    private TextField portTxtFld; // Value injected by FXMLLoader

    @FXML // fx:id="pswrdTxtFld"
    private PasswordField pswrdTxtFld; // Value injected by FXMLLoader

    @FXML // fx:id="logBtn"
    private Button logBtn; // Value injected by FXMLLoader

    @FXML // fx:id="cnclBtn"
    private Button cnclBtn; // Value injected by FXMLLoader
    
    @FXML // fx:id="errorMsgLbl"
    private Label errorMsgLbl; // Value injected by FXMLLoader
    
    @FXML // fx:id="sqlPortTxt"
    private TextField sqlPortTxt; // Value injected by FXMLLoader

    @FXML
    void cancel(ActionEvent event) {
    	System.exit(0);
    }

    @FXML
    void login(ActionEvent event) {
    	String userName, password, port, sqlPort;
    	
    	
    	userName = userTxtFld.getText();
    	password = pswrdTxtFld.getText();
    	port = portTxtFld.getText();
    	sqlPort = sqlPortTxt.getText();
    	 	
    	
		try {			
			MainServer mainServer = new MainServer(Integer.parseInt(port));
			mainServer.setServerCon(userName, password, Integer.parseInt(sqlPort));
			((Node)event.getSource()).getScene().getWindow().hide(); //hiding login window		
		} catch (NumberFormatException | IOException e) {
			errorMsgLbl.setText("Syntex error! Please enter an intger in ports");
			e.printStackTrace();
		}

    	
    }
   
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setTitle("Log in to server");
		primaryStage.getIcons().add(new Image("/server_earth.png"));
		primaryStage.setScene(scene);
    	primaryStage.show();
	}
	
	private void setValues(){
		//initialize the login values for 
		userTxtFld.setText("root");
		pswrdTxtFld.setText("12345");
		portTxtFld.setText("5555");
		sqlPortTxt.setText("3306");
	}
	
	public static void main(String[] args) {
		launch(args);
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setValues();
	}
}
