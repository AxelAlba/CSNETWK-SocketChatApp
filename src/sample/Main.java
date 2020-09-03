package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
    private static Stage mPrimaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mPrimaryStage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("views/login.fxml"));
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/scrollbar.css").toExternalForm());

        mPrimaryStage.setTitle("De La Salle Usap");
        mPrimaryStage.setScene(scene);
        mPrimaryStage.setResizable(false);
        mPrimaryStage.show();
    }

    public static void changeScene(String fxml) throws Exception {
        Parent pane = FXMLLoader.load(Main.class.getResource(fxml));
        mPrimaryStage.getScene().setRoot(pane);
    }

    public static Stage getPrimaryStage() {
        return mPrimaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
