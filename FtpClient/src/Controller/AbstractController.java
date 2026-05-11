package Controller;

import java.util.ArrayList;

import FTP.FTPClient;
import javafx.fxml.FXMLLoader;

public abstract class AbstractController implements ControllerHelper {

    protected FTPClient ftp;
    protected FXMLLoader loader;

    @Override
    public void setFTPClient(FTPClient ftp) {
        this.ftp = ftp;
    }

    public void printArrayString(ArrayList<String> allStrings) {
        for (String line : allStrings) {
            System.out.println(line);
        }
    }
}
