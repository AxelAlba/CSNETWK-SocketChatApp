package sample.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

enum User { SELF, OTHER }

public class ChatController {

    @FXML
    Button btnSend;

    @FXML
    TextField chatInput;

    @FXML
    VBox selfContainer;

    @FXML
    VBox otherContainer;

    @FXML
    public void sendMessage() {
        String chatText = chatInput.getText();
        String[] classes = {"message-box", "message-box-self"};
        VBox message = createMessageBlob(chatText, classes, User.SELF);
        selfContainer.getChildren().add(message);
    }

    private void receiveMessage() {
        String chatText = chatInput.getText();
        String[] classes = {"message-box", "message-box-other"};
        VBox message = createMessageBlob(chatText, classes, User.OTHER);
        otherContainer.getChildren().add(message);
    }

    private VBox createMessageBlob(String text, String[] classes, User type) {
        VBox messageBox = new VBox();
        Text messageText = new Text(text);

        for (String aClass : classes) {
            messageBox.getStyleClass().add(aClass);
        }

        messageBox.getChildren().add(messageText);

        if (text.length() > 40) {
            messageBox.setMaxWidth(150);
            messageText.wrappingWidthProperty().bind(messageBox.maxWidthProperty());
        } else {
            messageBox.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        }

        messageText.getStyleClass().add("message-text");

        if (type == User.SELF) {
            selfContainer.setAlignment(Pos.BASELINE_RIGHT);
        } else {
            otherContainer.setAlignment(Pos.BASELINE_LEFT);
        }

        return messageBox;
    }
}
