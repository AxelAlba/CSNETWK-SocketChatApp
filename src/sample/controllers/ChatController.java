package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Main;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    private static final int SEND = 0;
    private static final int RECEIVE = 1;
    private static final int IMAGE = 10;
    private static final int TEXT = 11;

    @FXML
    Button btnSend;

    @FXML
    TextField chatInput;

    @FXML
    VBox chatContainer;

    @FXML
    Button btnLogout;

    @FXML
    ImageView imgProfile;

    @FXML
    public void sendMessage() {
        createMessageItem(TEXT, SEND, chatInput.getText());
    }

    @FXML
    private void receiveMessage() {
        createMessageItem(TEXT, RECEIVE, chatInput.getText());
    }

    @FXML
    private void uploadFile() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        createMessageItem(IMAGE, SEND, String.valueOf(file));
    }

    @FXML
    private void logout() throws Exception {
//        TODO: Disconnect from server
//        TODO: Once server has disconnected, show logout screen

        Main.changeScene("views/logout.fxml");
    }

    private void createMessageItem(int messageType, int action, String data) {
        BorderPane bp = new BorderPane();
        Node message = null;
        bp.getStyleClass().add("message-wrapper");

        if (messageType == IMAGE) {
            message = createImageMessageItem(data);
        } else if (messageType == TEXT) {
            message = createTextMessageItem(data, action);
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

        if (action == SEND) {
            messageText.getStyleClass().add("message-box-self");
        } else if (action == RECEIVE) {
            messageText.getStyleClass().add("message-box-other");
        }

        return messageText;
    }


    private Node createImageMessageItem(String path) {
        Image image = new Image("file:///" + path);
        ImageView img = new ImageView(image);
        BorderPane bp = new BorderPane(img);

        img.getStyleClass().addAll("message-box, message-image");

        img.setFitHeight(150);
        img.setFitWidth(150);

        chatContainer.getChildren().add(bp);
        return img;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Rectangle clip = new Rectangle(imgProfile.getFitHeight(), imgProfile.getFitWidth());
        clip.setArcHeight(50);
        clip.setArcWidth(50);
        imgProfile.setClip(clip);
    }
}
