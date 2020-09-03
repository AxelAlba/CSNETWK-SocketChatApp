package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class ChatController {

    private static final int SEND = 0;
    private static final int RECEIVE = 1;

    @FXML
    Button btnSend;

    @FXML
    TextField chatInput;

    @FXML
    VBox chatContainer;


    @FXML
    public void sendMessage() {
        createMessageItem(chatInput.getText(), SEND);
    }

    @FXML
    private void receiveMessage() {
        createMessageItem(chatInput.getText(), RECEIVE);
    }


    private void createMessageItem(String text, int action) {
        BorderPane bp = new BorderPane();
        Label messageText = new Label(text);

        bp.getStyleClass().add("message-wrapper");
        messageText.getStyleClass().add("message-box");

        if (action == SEND) {
            messageText.getStyleClass().add("message-box-self");
            bp.setRight(messageText);
        } else if (action == RECEIVE) {
            messageText.getStyleClass().add("message-box-other");
            bp.setLeft(messageText);
        }

        chatContainer.getChildren().add(bp);
    }
}
