package chatapp.controllers;

import javafx.fxml.Initializable;
import chatapp.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class MatchingController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//    TODO: If match found, switch scene to chat
        boolean matchFound = false;
//        Server code to set matchFound to true
        if (matchFound) {
            try {
                Main.changeScene("views/chat.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
