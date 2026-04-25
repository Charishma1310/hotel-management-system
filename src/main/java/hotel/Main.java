/*
Extends javafx.application.Application. 
The start() method loads main.fxml via FXMLLoader, creates a Scene of size 1050×720, 
sets the window title, enforces a minimum size (900×620), and calls stage.show().
 main() just calls launch(args) which triggers the JavaFX lifecycle.
*/

package hotel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hotel/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1050, 720);
        stage.setTitle("LuxStay - Hotel Management System");
        stage.setMinWidth(900); stage.setMinHeight(620);
        stage.setScene(scene); stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
