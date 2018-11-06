package paint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Moses Cuevas
 */
public class Paint extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("paintfxml.fxml"));
        
        stage.setTitle("CSUN Studio");
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        
        scene.getStylesheets().add(Paint.class.getResource("paint-css.css").toExternalForm());
        
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
