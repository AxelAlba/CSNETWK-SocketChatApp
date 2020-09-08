package chatapp;

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

    public static void changeScene(String fxml) throws Exception {
        Parent root = FXMLLoader.load(Main.class.getResource(fxml));
        Window focused = Stage.getWindows()
                .stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);


        if (focused != null) {
            System.out.println(fxml + focused);
            focused.getScene().setRoot(root);
        }
    }

    // TODO: TEMP!!!
    public static Stage getPrimaryStage() {
        return mOtherStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
