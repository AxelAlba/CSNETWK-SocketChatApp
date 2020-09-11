package chatapp.controllers;

import chatapp.Constants;
import chatapp.Main;
import chatapp.repositories.MessageRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    @FXML
    Button btnSend, btnLogout;

    @FXML
    TextField chatInput;

    @FXML
    VBox chatContainer;

    @FXML
    Label textDownload;

    @FXML
    Label lblName, lblStatus;

    @FXML
    ImageView imgProfile, btnDownload;

    @FXML
    BorderPane imageViewer, matchingScreen;

    @FXML
    public void sendMessage() {
        String messageText = chatInput.getText();
        if (messageText.length() > 0) {
            createMessageItem(Constants.TEXT, Constants.SEND, messageText);
            MessageRepository.addMessage(messageText);
            chatInput.clear();
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        createMessageItem(Constants.IMAGE, Constants.SEND, String.valueOf(file));
    }

    @FXML
    private void uploadFile() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        createMessageItem(Constants.FILE, Constants.SEND, String.valueOf(file));
    }

    @FXML
    private void logout() throws Exception {
        MessageRepository.addMessage("-logout");
        Main.changeScene("views/login.fxml");
    }


    public void receiveMessage(String message) {
        if (message.length() > 0) {
            createMessageItem(Constants.TEXT, Constants.RECEIVE, message);
        }
    }

    private void viewImage(ImageView img) {
        imageViewer.setCenter(img);
        imageViewer.toFront();
        imageViewer.setOnMouseClicked(e -> imageViewer.toBack());

        ImageObject imageFile = new ImageObject(img);
        textDownload.setOnMouseClicked(e -> imageFile.downloadImage());
        btnDownload.setOnMouseClicked(e -> imageFile.downloadImage());
    }

    public void createMessageItem(int messageType, int action, String data) {
        BorderPane bp = new BorderPane();
        Node message = null;
        bp.getStyleClass().add("message-wrapper");

        if (messageType == Constants.IMAGE) {
            message = createImageMessageItem(data);
        } else if (messageType == Constants.TEXT) {
            message = createTextMessageItem(data, action);
        } else if (messageType == Constants.FILE) {
            message = createFileMessageItem(data, action);
        }

        if (action == Constants.SEND) {
            bp.setRight(message);
        } else if (action == Constants.RECEIVE) {
            bp.setLeft(message);
        }

        chatContainer.getChildren().add(bp);
    }

    private Node createTextMessageItem(String text, int action) {
        Label messageText = new Label(text);
        messageText.getStyleClass().add("message-box");
        messageText.setWrapText(true);

        if (action == Constants.SEND) {
            messageText.getStyleClass().add("message-box-self");
        } else if (action == Constants.RECEIVE) {
            messageText.getStyleClass().add("message-box-other");
        }

        return messageText;
    }

    private Node createImageMessageItem(String path) {
        ImageObject imgObject = new ImageObject(path, 200.0, true);
        ImageObject enlarged = new ImageObject(path, 400.0, false);
        Node message = imgObject.createImageMessageItem();
        message.setOnMouseClicked(e -> viewImage(enlarged.getImageView()));

        return message;
    }

    private Node createFileMessageItem(String path, int action) {
        File file = new File(path);
        String filename = file.getName();
        return createTextMessageItem(filename, action);
    }

    public void showChat(String name) {
        matchingScreen.toBack();
        lblName.setText(name);
    }

    public void onDisconnect() {
        lblStatus.setText("Disconnected");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
