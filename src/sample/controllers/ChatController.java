package sample.controllers;

import javafx.embed.swing.SwingFXUtils;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    private static final int SEND = 0;
    private static final int RECEIVE = 1;
    private static final int IMAGE = 10;
    private static final int FILE = 11;
    private static final int TEXT = 12;

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
        String messageText = chatInput.getText();
        if (messageText.length() > 0) {
            createMessageItem(TEXT, SEND, messageText);
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
//        TODO: If partner is disconnected, disallow sending
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
//        TODO: Disconnect from server
//        TODO: Once server has disconnected, show logout screen
        Main.changeScene("views/login.fxml");
    }

    private void downloadImage(Image image) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.gif")
        );

        File savedFile = fc.showSaveDialog(Main.getPrimaryStage());
        String path = savedFile.getPath();

        File output = new File(path);
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        img.getStyleClass().addAll("message-box, message-image");
        img.setFitHeight(150);
        img.setFitWidth(150);

        setRoundCorners(img, 50);
        img.setOnMouseClicked(e -> downloadImage(image));

        return img;
    }


    private Node createFileMessageItem(String path, int action) {
        File file = new File(path);
        String filename = file.getName();
        return createTextMessageItem(filename, action);
    }


    private void setRoundCorners(ImageView image, int value) {
        Rectangle clip = new Rectangle(image.getFitHeight(), image.getFitWidth());
        clip.setArcHeight(value);
        clip.setArcWidth(value);
        image.setClip(clip);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setRoundCorners(imgProfile, 50);
    }
}
