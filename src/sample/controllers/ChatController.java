package sample.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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

    private void viewImage(ImageView img) {
        imageViewer.setCenter(img);
        imageViewer.toFront();
        imageViewer.setOnMouseClicked(e -> imageViewer.toBack());
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
        final double X = 200, LARGER_X = 400;
        double height, width;
        Image image = new Image("file:///" + path);
        ImageView img = new ImageView(image);
        VBox wrapper = new VBox(img);

        if (image.getHeight() > image.getWidth()) {
            height = image.getHeight() * X / image.getWidth();
            width = X;
        } else {
            height = X;
            width = image.getWidth() * X / image.getHeight();
        }

        wrapper.getStyleClass().add("message-image-wrapper");
        img.getStyleClass().add("message-image");
        img.setFitHeight(height);
        img.setFitWidth(width);
        img.setSmooth(true);
        img.setPreserveRatio(true);
        img.setCache(true);

        setRoundCorners(img, 50);


        ImageView enlarged = new ImageView(image);
        if (image.getHeight() > image.getWidth()) {
            height = image.getHeight() * LARGER_X / image.getWidth();
            width = LARGER_X;
        } else {
            height = LARGER_X;
            width = image.getWidth() * LARGER_X / image.getHeight();
        }
        enlarged.setFitHeight(height);
        enlarged.setFitWidth(width);
        enlarged.setSmooth(true);
        enlarged.setPreserveRatio(true);
        enlarged.setCache(true);

        img.setOnMouseClicked(e -> viewImage(enlarged));

        return wrapper;
    }

    private Node createFileMessageItem(String path, int action) {
        File file = new File(path);
        String filename = file.getName();
        return createTextMessageItem(filename, action);
    }


    private void setRoundCorners(ImageView image, int value) {
        Rectangle clip = new Rectangle(image.getFitWidth(), image.getFitHeight());
        clip.setArcHeight(value);
        clip.setArcWidth(value);
        image.setClip(clip);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setRoundCorners(imgProfile, 50);
    }
}
