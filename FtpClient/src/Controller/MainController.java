package Controller;

import static FTP.Constant.PERMISSION_DENIED;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import FTP.FTPListResult;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController extends AbstractController {

    private Map<String, String> serverFileInfoMap = new HashMap<>();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> serverListView;

    @FXML
    private ListView<String> clientListView;

    @FXML
    private TextField command;

    @FXML
    private ListView<String> replyFromServerView;

    private String fileClicked = null;

    private String fileInformation = null;

    private File lastDirectory = null;

    private File currentLocalDirectory = new File("D:/");

    @FXML
    void initialize() throws IOException {
        assert serverListView != null : "fx:id=\"serverListView\" was not injected: check your FXML file 'Main.fxml'.";
        assert replyFromServerView != null
                : "fx:id=\"replyFromServerView\" was not injected: check your FXML file 'Main.fxml'.";
    }

    private void loadLocalFiles() {
        File[] files = currentLocalDirectory.listFiles();
        if (files == null) {
            return;
        }

        clientListView.getItems().clear();

        for (File file : files) {
            if (file.isDirectory()) {
                clientListView.getItems().add("[DIR] " + file.getName());
            } else {
                clientListView.getItems().add(file.getName());
            }
        }
    }

    private String extractFileName(String fileInformation) {
        String[] parts = fileInformation.trim().split("\\s+", 9);
        String filename = parts[8];

        return filename;
    }

    @Override
    public void onSceneLoaded(Stage stage) {
        // TODO Auto-generated method stub
        super.onSceneLoaded(stage);
        try {
            loadFiles();
            loadLocalFiles();
            // startAutoRefresh();
            // ArrayList<String> message = ftp.readReplyFromServer();
            // printMessage(message);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        // refreshTimeline.play();
    }

    public void loadFiles() throws Throwable {
        FTPListResult result = ftp.ls();

        printMessage(result.getReplies());

        serverListView.getItems().clear();
        serverFileInfoMap.clear();

        for (String fileInformation : result.getFiles()) {
            String fileName = extractFileName(fileInformation);

            serverListView.getItems().add(fileName);
            serverFileInfoMap.put(fileName, fileInformation);
        }
    }

    // We need to think about hashmap
    @FXML
    void changeServerDirectory(MouseEvent event) throws Throwable {
        String selectedFile = serverListView.getSelectionModel().getSelectedItem();

        if (event.getClickCount() == 2) {
            printMessage(ftp.cd(selectedFile));

            serverListView.getItems().clear();
            loadFiles();
            return;
        }

        if (event.getClickCount() == 1) {
            fileClicked = selectedFile;
            fileInformation = serverFileInfoMap.get(selectedFile);
            return;
        }
    }

    @FXML
    void changeLocalDirectory(MouseEvent event) {
        if (event.getClickCount() != 2) {
            return;
        }

        String selectedItem = clientListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || !selectedItem.startsWith("[DIR] ")) {
            return;
        }

        String folderName = selectedItem.replace("[DIR] ", "");
        currentLocalDirectory = new File(currentLocalDirectory, folderName);

        loadLocalFiles();
    }

    @FXML
    void downloadAFile(ActionEvent event) throws IOException {
        if (fileClicked == null || fileClicked.isBlank()) {
            return;
        }

        if (!isFile(fileInformation)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Download");
            alert.setHeaderText("Please select a file to download");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to download");

        if (lastDirectory != null && lastDirectory.exists()) {
            fileChooser.setInitialDirectory(lastDirectory);
        }

        fileChooser.setInitialFileName(fileClicked);
        // Get current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Show dialog
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile == null) {
            return;
        }

        lastDirectory = saveFile.getParentFile();

        ArrayList<String> currentDirectoryFromServer = ftp.pwd();
        String serverPath = currentDirectoryFromServer.get(0).split(" ")[1].replace("\"", "") + "/" + fileClicked;

        printMessage(ftp.get(serverPath, saveFile.getAbsolutePath()));

        printMessage(ftp.readReplyFromServer());
    }

    @FXML
    void uploadAFile(ActionEvent event) throws Throwable {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");

        if (lastDirectory != null && lastDirectory.exists()) {
            fileChooser.setInitialDirectory(lastDirectory);
        }

        // Get current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Show dialog
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }

        String fileName = selectedFile.getName();

        ArrayList<String> currentDirectoryFromServer = ftp.pwd();
        String serverPath = currentDirectoryFromServer.get(0).split(" ")[1].replace("\"", "") + "/" + fileName;

        printMessage(ftp.put(selectedFile.getAbsolutePath(), serverPath));

        printMessage(ftp.readReplyFromServer());

        serverListView.getItems().clear();
        loadFiles();
    }

    @FXML
    void deleteAFile(ActionEvent event) throws Throwable {
        if (isFile(fileInformation)) {
            printMessage(ftp.delete(fileClicked));
            serverListView.getItems().clear();
            loadFiles();
            return;
        }
        if (isDirectory(fileInformation)) {
            ArrayList<String> replys = ftp.rmdir(fileClicked);
            printMessage(replys);

            String lastReply = replys.get(replys.size() - 1);

            if (lastReply.startsWith(PERMISSION_DENIED)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cannot Delete Folder");
                alert.setHeaderText("Folder is not empty or cannot be removed");
                alert.showAndWait();
                return;
            }

            serverListView.getItems().clear();
            loadFiles();
            return;
        }
    }

    @FXML
    void showPWD(ActionEvent event) throws IOException {
        printMessage(ftp.pwd());
    }

    public void printMessage(ArrayList<String> message) throws IOException {
        replyFromServerView.getItems().addAll(message);
        if (!message.isEmpty()) {
            int lastindex = replyFromServerView.getItems().size() - 1;
            replyFromServerView.scrollTo(lastindex);
            replyFromServerView.getSelectionModel().select(lastindex);
        }
    }

    @FXML
    void quit(ActionEvent event) throws IOException {
        ftp.disconnect();
        // Get current window
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.close();
    }

    @FXML
    void goBackServer(ActionEvent event) throws Throwable {
        printMessage(ftp.cd(".."));
        serverListView.getItems().clear();
        loadFiles();

    }

    @FXML
    private void goBackLocal() {
        File parent = currentLocalDirectory.getParentFile();

        if (parent != null) {
            currentLocalDirectory = parent;
            loadLocalFiles();
        }
    }

    @FXML
    private void runCommand() throws Throwable {
        String userCommand = command.getText().trim();

        ftp.sendCommand(userCommand);
        printMessage(ftp.readReplyFromServer());

        serverListView.getItems().clear();
        loadFiles();
        command.clear();

    }

    public boolean isDirectory(String listLine) {
        return listLine != null && listLine.startsWith("d");
    }

    public boolean isFile(String listLine) {
        return listLine != null && listLine.startsWith("-");
    }
}
