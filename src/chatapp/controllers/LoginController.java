package chatapp.controllers;

import chatapp.Main;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.ControllerInstance;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.net.ConnectException;
import java.util.regex.Pattern;

public class LoginController {

    @FXML
    TextField fIPAddress, fPort, fUsername;

    @FXML
    Label lblUsername, lblPort, lblIP;

    @FXML
    Button btnLogin;

    @FXML
    HBox hbUsername, hbIP, hbPort;

    @FXML
    public void login() throws Exception {
        boolean isPortOpen = true, isReconnecting = false;

        String username = fUsername.getText();
        String ip = fIPAddress.getText();
        String port = fPort.getText();

        if (isValidLogin()) {
            int portNum = Integer.parseInt(port);

            try {
                if (!ClientRepository.isFull()) {
                    Client client = new Client(username, ip, portNum);
                    ClientRepository.addClient(client.getUsername());
                    ClientRepository.setThisClient(client);
                    client.initialize();
                } else {
                    if (ClientRepository.containsClient(username)) {
                        Client client = new Client(username, ip, portNum);
                        client.initialize();

                        ChatController c = (ChatController) Main.changeScene("views/chat.fxml");
                        ControllerInstance.setChatController(c);

                        String otherName = ClientRepository.getClientList().get(0).equals(username) ?
                                ClientRepository.getClientList().get(1) :
                                ClientRepository.getClientList().get(0);

                        c.showChat(otherName);
                        isReconnecting = true;
                    } else {
                        createErrorMessage(hbUsername, lblUsername, "  Chat room is full.");
                    }
                }
            } catch (ConnectException e) {
                System.out.println("Connection refused: Server not started.");
                isPortOpen = false;
                createErrorMessage(hbIP, lblIP, "  Please check your IP");
                createErrorMessage(hbPort, lblPort, "  Please check if your port is open.");
            }

            if (isPortOpen && !isReconnecting) {
                ChatController c = (ChatController) Main.changeScene("views/chat.fxml");
                ControllerInstance.setChatController(c);
            }
        } else {
            if (!isValidUsername(username)) {
                createErrorMessage(hbUsername, lblUsername, "  Please enter a username.");
            }

            if (!isValidIP(ip)) {
                createErrorMessage(hbIP, lblIP,"  Please enter a valid IP Address.");
            }

            if (!isValidPort(port)) {
                createErrorMessage(hbPort, lblPort,"  Please enter a valid port number.");
            }
        }
    }

    private void createErrorMessage(HBox parent, Label label, String message) {
        Label err = new Label(message);
        err.getStyleClass().addAll("error", "label", "italic");
        parent.getChildren().clear();
        parent.getChildren().add(label);

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
        if (fIPAddress.getText().equals("") || fPort.getText().equals(""))
            return false;

        String username = fUsername.getText();
        String ip = fIPAddress.getText();
        String port = fPort.getText();

        return isValidUsername(username) && isValidIP(ip) && isValidPort(port);
    }
}
