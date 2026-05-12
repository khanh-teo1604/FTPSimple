package Controller;

import FTP.FTPClient;
import javafx.stage.*;

public interface ControllerHelper {

    void setFTPClient(FTPClient ftp);
    default void onSceneLoaded(Stage stage){
        
    }
}
