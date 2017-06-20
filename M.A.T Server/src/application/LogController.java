package application;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import com.sun.media.jfxmediaimpl.platform.Platform;

import javafx.concurrent.Task;
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
     * sets the ip and port number on the log screen
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
    	System.exit(0);
    }
    
    /**
     * shows a message on the log controller
     * @param msg message to be displayed
     */
    public void showMsg(final String msg){
    	counter++;
    	final long currTime = System.currentTimeMillis();
    	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	
    	Task<String> task = new Task<String>() {
            @Override
            public String call() {
                return "[" + sdf.format(currTime) + "] " + msg + "\n" ; // value to be processed in onSucceeded
            }
        };
        task.setOnSucceeded(e -> logTxtArea.appendText(task.getValue()));
        Thread t = new Thread(task);
        t.start();
    	
    	if(counter > 100){
    		try(FileWriter fw = new FileWriter("C:\\M.A.T files\\Log file.txt", true);
				    BufferedWriter bw = new BufferedWriter(fw);
				    PrintWriter out = new PrintWriter(bw))
				{
				    out.println(logTxtArea.getText());
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
