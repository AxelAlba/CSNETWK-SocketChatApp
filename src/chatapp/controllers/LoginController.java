package chatapp.controllers;

import chatapp.Main;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    TextField fIPAddress, fPort, fUsername;

    @FXML
    private void onLoginClick() throws Exception {
        if (isValidLogin()) {
            String username = fUsername.getText();
            String host = fIPAddress.getText();
            int port = Integer.parseInt(fPort.getText());

            Server.setPort(port);
            new Thread(new Server()).start();

            Client client = new Client(username, host, port);
            client.initialize();

            Main.changeScene("views/matching.fxml");
        } else {
//            TODO: Add error messages for invalid field/s
        }
    }

    private boolean isValidIP(String ipString) {
        String regex = "(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(ipString).matches() || ipString.equalsIgnoreCase("localhost");
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
