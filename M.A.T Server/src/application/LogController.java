package application;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class LogController {

    @FXML // fx:id="logTxtArea"
    private TextArea logTxtArea; // Value injected by FXMLLoader

    @FXML // fx:id="exitBtn"
    private Button exitBtn; // Value injected by FXMLLoader
    
    @FXML // fx:id="serverIp"
    private Label serverIp; // Value injected by FXMLLoader
    
    @FXML // fx:id="portLbl"
    private Label portLbl; // Value injected by FXMLLoader
    
    private static int counter = 0;
    
    public void setIp(int portNum){
    	try {
			serverIp.setText("Server ip: " + Inet4Address.getLocalHost().getHostAddress());
			portLbl.setText("Port: " + portNum);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @FXML
    void exit(ActionEvent event) {
    	System.exit(0);
    }
    
    public void showMsg(String msg){
    	counter++;
    	long currTime = System.currentTimeMillis();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	logTxtArea.appendText("[" + sdf.format(currTime) + "] " + msg + "\n");
    	if(counter > 100){
    		try(FileWriter fw = new FileWriter("C:\\M.A.T files\\Log file.txt", true);
				    BufferedWriter bw = new BufferedWriter(fw);
				    PrintWriter out = new PrintWriter(bw))
				{
				    out.println(logTxtArea.getText());
				    logTxtArea.clear();
				    counter = 0;
				} catch (IOException e) {
				    //exception handling left as an exercise for the reader
				}
    	}
    }

}
