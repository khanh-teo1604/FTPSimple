package Controller;

import javax.swing.Action;

import FTP.FTPClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SceneLoader {
    private final FTPClient ftp;

    public SceneLoader(FTPClient ftp) {
        this.ftp = ftp;
    }

    public void loadScene(ActionEvent event, String resourceFolder) throws Throwable {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourceFolder));
        Parent root = loader.load();

        ControllerHelper controllerHelper = (ControllerHelper) loader.getController();
        controllerHelper.setFTPClient(ftp);

        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        controllerHelper.onSceneLoaded(stage);

        stage.show();
    }
}
