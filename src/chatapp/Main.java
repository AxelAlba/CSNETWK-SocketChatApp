package chatapp;

import chatapp.controllers.Client;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.LoginControllerInstance;
import chatapp.repositories.MessageRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage mPrimaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mPrimaryStage = primaryStage;


        // Instantiating the login controller
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("views/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(Main.class.getResource("styles/styles.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("styles/scrollbar.css").toExternalForm());

        LoginControllerInstance.setLoginController(loader.getController());

        mPrimaryStage.setTitle("De La Salle Usap");
        mPrimaryStage.setScene(scene);
        mPrimaryStage.setResizable(false);
        mPrimaryStage.show();

        mPrimaryStage.setOnCloseRequest(e -> {
            Client thisClient = ClientRepository.getThisClient();
            if (thisClient != null) {
                MessageRepository.addMessage("-logout");
                thisClient.stopAllThreads();
            }

            ClientRepository.clearClients();
            ClientRepository.resetThisClient();
            MessageRepository.clearMessages();

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

    public static void main(String[] args) {
        launch(args);
    }
}
