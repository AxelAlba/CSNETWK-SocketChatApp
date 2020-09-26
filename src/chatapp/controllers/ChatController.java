package chatapp.controllers;

import chatapp.Constants;
import chatapp.Main;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.FileRepository;
import chatapp.repositories.MessageRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.List;
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
    private void uploadFile() {
        FileChooser fc = new FileChooser();
        Stage stage = Main.getPrimaryStage();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            createMessageItem(Constants.FILE, Constants.SEND, String.valueOf(file));

            String path = String.valueOf(file);
            String fileSendMessage = String.format("-sendFile %s", path); // ex: -sendFile file.txt

            // Send this to server
            MessageRepository.addMessage(fileSendMessage);
        }
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
            if (message.split(":").length > 1) {
                message = message.split(":")[1];
                createMessageItem(Constants.TEXT, Constants.RECEIVE, message);
            }
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

    private void downloadFile()  {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text or Image Files", "*.jpg", "*png")
        );

        File savedFile = fc.showSaveDialog(Main.getPrimaryStage());
        if (savedFile != null) {
            String path = savedFile.getPath();
            System.out.println("Path: " + path);

            // Write file locally
            try {
                List<byte[]> fileContent = FileRepository.getFileContent();
                FileOutputStream writer = new FileOutputStream(path);
                for (byte[] chunk : fileContent)
                    writer.write(chunk, 0, chunk.length);

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        String extension = "";
        if (path != null) {
            extension = path.substring(path.lastIndexOf('.') + 1);
        }

        boolean isImage = extension.equals("jpg") || extension.equals("png");

        String filename = isImage ? "Image File" : "Text File";

        // Use the local file name if sending
        if (action == Constants.SEND) {
            File file = new File(path);
            filename = file.getName();
        }

        HBox container = new HBox();
        Label fileMessage = new Label(filename);
        Label space = new Label("   ");

        String mPath = (String.valueOf(getClass().getResource("../assets/download_alt.png")));
        ImageObject iconObj = new ImageObject(mPath, 20.0, 20.0);
        ImageView icon = iconObj.getImageView();

        icon.getStyleClass().add("icon");

        fileMessage.setUnderline(true);
        fileMessage.getStyleClass().add("text-white");
        fileMessage.setTextOverrun(OverrunStyle.ELLIPSIS);

        container.setAlignment(Pos.CENTER);
        container.getChildren().add(icon);
        container.getChildren().add(space);
        container.getChildren().add(fileMessage);

        if (action == Constants.SEND)
            container.getStyleClass().addAll( "file-message-box", "message-box-self", "cursor-hand");
        else if (action == Constants.RECEIVE)
            container.getStyleClass().addAll( "file-message-box", "message-box-other", "cursor-hand");

        container.setOnMouseClicked(e -> downloadFile());

        return container;
    }

}
