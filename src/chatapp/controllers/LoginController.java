package chatapp.controllers;

import chatapp.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    TextField fIPAddress, fPort, fUsername;

    @FXML
    Button btnLogin;

    @FXML
    HBox hbUsername, hbIP, hbPort;

    @FXML
    public void login() throws Exception {
        String username = fUsername.getText();
        String ip = fIPAddress.getText();
        String port = fPort.getText();

        if (isValidLogin()) {
            Main.changeScene("views/chat.fxml");
        } else {
            if (!isValidUsername(username)) {
                createErrorMessage(hbUsername, "  Please enter a username.");
            }

            if (!isValidIP(ip)) {
                createErrorMessage(hbIP, "  Please enter a valid IP Address.");
            }

            if (!isValidPort(port)) {
                createErrorMessage(hbPort, "  Please enter a valid port number.");
            }
        }
    }

    private void createErrorMessage(HBox parent, String message) {
        Label err = new Label(message);
        err.getStyleClass().addAll("error", "label", "italic");
        parent.getChildren().add(err);
    }

    private boolean isValidUsername(String name) {
        return name.length() > 0;
    }

    private boolean isValidIP(String ipString) {
        String regex = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(ipString).matches() || ipString.equalsIgnoreCase("localhost");
    }

    private boolean isValidPort(String portString) {
        if (portString.length() == 0)
            return false;

        int port = Integer.parseInt(portString);
        return port >= 0 && port <= 65535;
    }

    private boolean isValidLogin() {
        if (fIPAddress.getText().equals("") || fPort.getText().equals("")) {
            return false;
        }

        String username = fUsername.getText();
        String ip = fIPAddress.getText();
        String port = fPort.getText();

        return isValidUsername(username) && isValidIP(ip) && isValidPort(port);
    }
}
