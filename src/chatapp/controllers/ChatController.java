package chatapp.controllers;

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
import chatapp.Main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    private static final int SEND = 0;
    private static final int RECEIVE = 1;
    private static final int IMAGE = 10;
    private static final int FILE = 11;
    private static final int TEXT = 12;

    @FXML
    TextField chatInput;

    @FXML
    VBox chatContainer;

    @FXML
    Button btnLogout;

    @FXML
    ImageView btnDownload;

    @FXML
    Label textDownload;

    @FXML
    ImageView imgProfile;

    @FXML
    BorderPane imageViewer;

    @FXML
    public void sendMessage() {
        String messageText = chatInput.getText();
        if (messageText.length() > 0) {
            createMessageItem(TEXT, SEND, messageText);
            chatInput.clear();
        }
    }

    @FXML
    private void receiveMessage() {
        String messageText = chatInput.getText();
        if (messageText.length() > 0) {
            createMessageItem(TEXT, RECEIVE, messageText);
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        createMessageItem(IMAGE, SEND, String.valueOf(file));
    }

    @FXML
    private void uploadFile() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        createMessageItem(FILE, SEND, String.valueOf(file));
    }

    @FXML
    private void logout() throws Exception {
        Main.changeScene("views/login.fxml");
    }

    private void viewImage(ImageView img) {
        imageViewer.setCenter(img);
        imageViewer.toFront();
        imageViewer.setOnMouseClicked(e -> imageViewer.toBack());

        ImageObject imageFile = new ImageObject(img);
        textDownload.setOnMouseClicked(e -> imageFile.downloadImage());
        btnDownload.setOnMouseClicked(e -> imageFile.downloadImage());
    }

    private void createMessageItem(int messageType, int action, String data) {
        BorderPane bp = new BorderPane();
        Node message = null;
        bp.getStyleClass().add("message-wrapper");

        if (messageType == IMAGE) {
            message = createImageMessageItem(data);
        } else if (messageType == TEXT) {
            message = createTextMessageItem(data, action);
        } else if (messageType == FILE) {
            message = createFileMessageItem(data, action);
        }

        if (action == SEND) {
            bp.setRight(message);
        } else if (action == RECEIVE) {
            bp.setLeft(message);
        }

        chatContainer.getChildren().add(bp);
    }

    private Node createTextMessageItem(String text, int action) {
        Label messageText = new Label(text);
        messageText.getStyleClass().add("message-box");
        messageText.setWrapText(true);
        if (action == SEND) {
            messageText.getStyleClass().add("message-box-self");
        } else if (action == RECEIVE) {
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ImageObject.setRoundCorners(imgProfile, 50);
    }
}
