package chatapp;

import chatapp.repositories.ClientRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class Main extends Application {
    private static Stage mPrimaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mPrimaryStage = primaryStage;

        Parent root = FXMLLoader.load(Main.class.getResource("views/login.fxml"));
        Scene scene = new Scene(root, 1024, 768);

        scene.getStylesheets().add(Main.class.getResource("styles/styles.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("styles/scrollbar.css").toExternalForm());

        mPrimaryStage.setTitle("De La Salle Usap");
        mPrimaryStage.setScene(scene);
        mPrimaryStage.setResizable(false);
        mPrimaryStage.show();
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
