package main.chatapp;

import main.chatapp.repositories.ClientRepository;
import main.chatapp.repositories.LoginControllerInstance;
import main.chatapp.repositories.MessageRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage mPrimaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        initialize(primaryStage);
    }

    public static void initialize(Stage primaryStage) throws IOException {
        mPrimaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("views/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(Main.class.getResource("styles/styles.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("styles/scrollbar.css").toExternalForm());

        LoginControllerInstance.setLoginController(loader.getController());

        // Initialize the message repository
        MessageRepository.initialize();

        mPrimaryStage.setTitle("De La Salle Usap");
        mPrimaryStage.setScene(scene);
        mPrimaryStage.setResizable(false);
        mPrimaryStage.show();

        mPrimaryStage.setOnCloseRequest(e -> {
            logout();
            Platform.exit();
            System.exit(0);
        });
    }


    public static Object changeScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource(fxml));
        Parent root = loader.load();
        mPrimaryStage.getScene().setRoot(root);
        return loader.getController();
    }

    public static Stage getPrimaryStage() {
        return mPrimaryStage;
    }

    public static void logout() {
        MessageRepository.addMessage("-logout");
        System.out.println("Controller last message: " + MessageRepository.getLastMessage());

        ClientRepository.clearClients();
        ClientRepository.resetThisClient();
        MessageRepository.clearMessages();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
