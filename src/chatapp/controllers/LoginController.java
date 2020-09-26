package chatapp.controllers;

import chatapp.Main;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.ControllerInstance;
import chatapp.repositories.MessageRepository;
import javafx.application.Platform;
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

    private Client client;

    @FXML
    public void login() throws Exception {
        String username = fUsername.getText();
        String ip = fIPAddress.getText();
        String port = fPort.getText();

        if (isValidLogin()) {
            int portNum = Integer.parseInt(port);

            try {
                System.out.println("LoginController: Enter login()");

                client = new Client(username, ip, portNum);
                client.initialize();
            } catch (ConnectException e) {
                System.out.println("Connection refused: Server not started.");
                createErrorMessage(hbIP, lblIP, "  Please check your IP");
                createErrorMessage(hbPort, lblPort, "  Please check if your port is open.");
            }
        }

        // Generate error messages for malformed inputs
        else {
            if (!isValidUsername(username))
                createErrorMessage(hbUsername, lblUsername, "  Please enter a username.");

            if (!isValidIP(ip))
                createErrorMessage(hbIP, lblIP,"  Please enter a valid IP Address.");

            if (!isValidPort(port))
                createErrorMessage(hbPort, lblPort,"  Please enter a valid port number.");
        }
    }

    public void rejectClient(String message) {
        System.out.println("View thread: Rejected client");
        createErrorMessage(hbUsername, lblUsername, message);
        client.stopAllThreads();
    }

    public void acceptClient() {
        System.out.println("View thread: Accepted client");
        try {
            ClientRepository.addClient(client.getUsername());
            ClientRepository.setThisClient(client); // To refer to "this" client

            ChatController c = (ChatController) Main.changeScene("views/chat.fxml");
            ControllerInstance.setChatController(c);
        } catch (Exception e) {}
    }

    public void resetClient() {
        client = null;
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
