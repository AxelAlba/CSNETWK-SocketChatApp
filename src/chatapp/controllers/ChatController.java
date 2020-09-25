package chatapp.controllers;

import chatapp.Constants;
import chatapp.Main;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.MessageRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    TextField chatInput;

    @FXML
    VBox chatContainer;

    @FXML
    Label textDownload, lblName, lblStatus;

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
        ClientRepository.getThisClient().stopAllThreads();

        // Clear all repositories to simulate application exit
        ClientRepository.clearClients();
        ClientRepository.resetThisClient();
        MessageRepository.clearMessages();

        Main.changeScene("views/login.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ImageObject.setRoundCorners(imgProfile, 50);
    }

    public void receiveMessage(String message) {
        if (message.length() > 0) {
            message = message.split(":")[1];
            createMessageItem(Constants.TEXT, Constants.RECEIVE, message);
        }
    }

    public void showChat(String name) {
        matchingScreen.toBack();
        lblName.setText(name);
    }

    public void onPartnerDisconnect() {
        lblStatus.setText("Disconnected");
        lblStatus.getStyleClass().add("disconnected");
    }

    public void onPartnerReconnect() {
        lblStatus.setText("Online");
        lblStatus.getStyleClass().remove("disconnected");
    }

    private void viewImage(ImageView img) {
        imageViewer.setCenter(img);
        imageViewer.toFront();
        imageViewer.setOnMouseClicked(e -> imageViewer.toBack());

        ImageObject imageFile = new ImageObject(img);
        textDownload.setOnMouseClicked(e -> imageFile.downloadImage());
        btnDownload.setOnMouseClicked(e -> imageFile.downloadImage());
    }

    private void downloadFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Download File");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.docx", "*.pdf", "*.txt")
        );

        File savedFile = fc.showSaveDialog(Main.getPrimaryStage());
        String path = savedFile.getPath();
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

        HBox container = new HBox();
        Label messageItem = (Label) createTextMessageItem(filename, action);
        ImageView icon = new ImageView(new Image(String.valueOf(getClass().getResource("../assets/download_alt.png"))));

        messageItem.setUnderline(true);
        container.getChildren().add(icon);
        container.getChildren().add(messageItem);
        container.getStyleClass().add("message-box");
        container.setOnMouseClicked(e -> downloadFile());

        return container;
    }

}
