package chatapp;

import chatapp.controllers.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class Main extends Application {
    private static Stage mSelfStage;
    private static Stage mOtherStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mSelfStage = primaryStage;
        mOtherStage = new Stage();

        initializeStage(mSelfStage);
        initializeStage(mOtherStage);
    }

    private static void initializeStage(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Main.class.getResource("views/login.fxml"));
        Scene scene = new Scene(root, 1024, 768);

        scene.getStylesheets().add(Main.class.getResource("styles/styles.css").toExternalForm());
        scene.getStylesheets().add(Main.class.getResource("styles/scrollbar.css").toExternalForm());

        stage.setTitle("De La Salle Usap");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static Object changeScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource(fxml));

        Parent root = loader.load();
        Stage.getWindows()
                .stream()
                .filter(Window::isFocused)
                .findFirst()
                .ifPresent(focused -> focused.getScene().setRoot(root));

        return loader.getController();
    }

    public static Object getController(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource(fxml));
        loader.load();
        return loader.getController();
    }

    // TODO: TEMP!!!
    public static Stage getPrimaryStage() {
        return mOtherStage;
    }

    public static Stage getSelfStage() {
        return mSelfStage;
    }

    public static Stage getOtherStage() {
        return mOtherStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
