package Controller;

import java.util.ArrayList;

import FTP.FTPClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

// public class ControllerHelper {
//     private Stage stage;
//     private Scene scene;
//     private Parent root;
//     protected FTPClient ftp;
//     protected FXMLLoader loader;

//     public void setFTPClient(FTPClient ftp) {
//         this.ftp = ftp;
//     }

//     // I think this one need to use polymophism
//     public void loadAnotherScene(ActionEvent event, String resourceFolder) throws Throwable {
//         loader = new FXMLLoader(getClass().getResource(resourceFolder));
//         root = loader.load();
//         ((ControllerHelper) loader.getController()).setFTPClient(ftp);
//         stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
//         scene = new Scene(root);
//         stage.setScene(scene);

//         if (loader.getController() instanceof MainController) {
//             ((MainController) loader.getController()).loadFiles();
//             getStage().setMaximized(true);

//         }

//         stage.show();
//     }

//     public Scene getScene() {
//         return scene;
//     }

//     public Parent getRoot() {
//         return root;
//     }

//     public Stage getStage() {
//         return stage;
//     }

//     public void printArrayString(ArrayList<String> allStrings) {
//         for (String line : allStrings) {
//             System.out.println(line);
//         }
//     }
// }
public interface ControllerHelper {

    void setFTPClient(FTPClient ftp);
    default void onSceneLoaded(Stage stage){
        
    }
}
