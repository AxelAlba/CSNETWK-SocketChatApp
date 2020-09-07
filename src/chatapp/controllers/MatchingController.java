package chatapp.controllers;

import javafx.fxml.Initializable;
import chatapp.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class MatchingController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean matchFound = Server.getActiveClients().size() == 2;
        if (matchFound) {
            try {
                Main.changeScene("views/chat.fxml");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
