package application;

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

    @FXML
    void exit(ActionEvent event) {
    	System.exit(0);
    }
    
    public void showMsg(String msg){
    	long currTime = System.currentTimeMillis();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	logTxtArea.appendText("[" + sdf.format(currTime) + "] " + msg + "\n");
    }

}
