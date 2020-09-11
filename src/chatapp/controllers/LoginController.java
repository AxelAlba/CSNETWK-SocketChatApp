package chatapp.controllers;

import chatapp.Constants;
import chatapp.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.ConnectException;
import java.util.regex.Pattern;

public class LoginController {

    @FXML
    TextField fIPAddress, fPort, fUsername;

    @FXML
    Button btnLogin;

    @FXML
    public void login() throws Exception {
        boolean portOpen = true;

        if (isValidLogin()) {
            int port = Integer.parseInt(fPort.getText());
            String username = fUsername.getText();
            String host = fIPAddress.getText();

            try {
                ChatController c = (ChatController) Main.changeScene("views/chat.fxml");
                ControllerRepo.setChatController(c);

                Client client = new Client(username, host, port);
                client.initialize();
            } catch (ConnectException e) {
                System.out.println("Connection refused: Server not started.");
                portOpen = false;
                // TODO: Error message for bad ip or closed port
            }
        } else {
//            TODO: Error messages for malformed inputs
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
        String ip = fIPAddress.getText();
        int port = Integer.parseInt(fPort.getText());

        return isValidIP(ip) && isValidPort(port);
    }


}
