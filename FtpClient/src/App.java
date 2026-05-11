import java.io.IOException;

import Controller.ConnectController;
import FTP.FTPClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FTPClient ftpClient = new FTPClient();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXMLFile/Connection.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        ConnectController connectController = loader.getController();
        connectController.setFTPClient(ftpClient);

        // controller.setMainWindow(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
