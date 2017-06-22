package application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


/**
 * controller for the server log
 * @author Samer Kinaan
 *
 */
public class LogController {

    @FXML // fx:id="logTxtArea"
    private TextArea logTxtArea; // Value injected by FXMLLoader

    @FXML // fx:id="exitBtn"
    private Button exitBtn; // Value injected by FXMLLoader
    
    @FXML // fx:id="serverIp"
    private Label serverIp; // Value injected by FXMLLoader
    
    @FXML // fx:id="portLbl"
    private Label portLbl; // Value injected by FXMLLoader

    /**
     * counts the number of rows in the text field
     */
    private int counter = 0;
   
    /**
     * sets the IP and port number on the log screen
     * @param portNum 	port number that the server is listening on
     */
    public void setIp(int portNum){
    	try {
			serverIp.setText("Server ip: " + Inet4Address.getLocalHost().getHostAddress());
			portLbl.setText("Port: " + portNum);
		} catch (UnknownHostException e) {
			logTxtArea.appendText("Failed to set IP and port fialds");
			e.printStackTrace();
		}
    }

    /**
     * closes the log controller
     * @param event
     */
    @FXML
    void exit(ActionEvent event) {
    	try(FileWriter fw = new FileWriter("C:\\M.A.T files\\Log file.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				String textAreaText = logTxtArea.getText().replaceAll("\n", System.getProperty("line.separator"));
				out.println(textAreaText);
			} catch (IOException e) {
			    e.printStackTrace();
			}
    	System.exit(0);
    }
    
    /**
     * shows a message on the log controller
     * @param msg message to be displayed
     */
    public void showMsg(final String msg){
    	counter++;
    	long currTime = System.currentTimeMillis();
    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        Platform.runLater(()->logTxtArea.appendText("[" + sdf.format(currTime) + "] " + msg + "\n"));
        
    	
    	if(counter > 100){
    		try(FileWriter fw = new FileWriter("C:\\M.A.T files\\Log file.txt", true);
				    BufferedWriter bw = new BufferedWriter(fw);
				    PrintWriter out = new PrintWriter(bw))
				{
    				String textAreaText = logTxtArea.getText().replaceAll("\n", System.getProperty("line.separator"));
    				out.println(textAreaText);
				    logTxtArea.clear();
				    counter = 0;
				    logTxtArea.appendText("Data was exported to log file.\n");
				} catch (IOException e) {
					logTxtArea.appendText("Failed to export to log file.\n");
					counter = 0;
				    e.printStackTrace();
				}
    	}
    }

}
