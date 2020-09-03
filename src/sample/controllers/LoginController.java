package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import sample.Main;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    TextField fIPAddress, fPort;

    @FXML
    private void onLoginClick() throws Exception {
        if (isValidLogin()) {
//            Main.changeScene("views/matching.fxml");
            Main.changeScene("views/chat.fxml");
        } else {
//            TODO: Make error messages
        }
    }

    private boolean isValidIP(String ipString) {
        String regex = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(ipString).matches();
    }

    private boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    private boolean isValidLogin() {
        if (fIPAddress.getText().equals("") || fPort.getText().equals("")) {
            return false;
        }

        String ip = fIPAddress.getText();
        int port = Integer.parseInt(fPort.getText());

        return isValidIP(ip) && isValidPort(port);
    }
}
