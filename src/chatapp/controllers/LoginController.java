package chatapp.controllers;

import chatapp.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    @FXML
    TextField fIPAddress, fPort, fUsername;

    @FXML
    Button btnLogin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnLogin.setOnMouseClicked(e -> {
            if (isValidLogin()) {
                String username = fUsername.getText();
                String host = fIPAddress.getText();
                int port = Integer.parseInt(fPort.getText());


//                Server server = Server.getInstance(port);
//                if (!server.isActive()) {
//                    Thread serverThread = new Thread(server);
//                    serverThread.start();
//                    server.restrictSpawn();
//                }

                new Thread(new Server(port)).start();

                Client client;
                try {
                    client = new Client(username, host, port);
                    client.initialize();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                try {
                    System.out.println("login");
                    Main.changeScene("views/chat.fxml");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
//            TODO: Add error messages for invalid field/s
            }
        });
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
