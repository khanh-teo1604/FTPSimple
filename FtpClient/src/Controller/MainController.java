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
import javafx.scene.control.Label;
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
    private Label localPath;

    @FXML
    private Label serverPath;

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

    /**
     * Load all the file name from the local computer and add it to the list view.
     */

    private void loadLocalFiles() {
        File[] files = currentLocalDirectory.listFiles();

        localPath.setText(currentLocalDirectory.getAbsolutePath());
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

    /**
     * Load all the file name from server to the server list view.
     * 
     * @throws Throwable
     */

    private void loadServerFiles() throws Throwable {
        FTPListResult result = ftp.ls();
        serverPath.setText(extractCurrentDirectory(ftp.pwd()));

        printMessage(result.getReplies());

        serverListView.getItems().clear();
        serverFileInfoMap.clear();

        for (String fileInformation : result.getFiles()) {
            String fileName = extractFileName(fileInformation);

            if (isDirectory(fileInformation)) {
                serverListView.getItems().add("[DIR] " + fileName);
            } else {
                serverListView.getItems().add(fileName);
            }

            serverFileInfoMap.put(fileName, fileInformation);
        }
    }

    /**
     * After the client receive the information (after LIST command) this method
     * will extract the file name from the result.
     * 
     * @param fileInformation
     * @return
     */

    private String extractFileName(String fileInformation) {
        String[] parts = fileInformation.trim().split("\\s+", 9);
        String filename = parts[8];

        return filename;
    }

    /**
     * Load the main scene.
     */
    @Override
    public void onSceneLoaded(Stage stage) {
        // TODO Auto-generated method stub
        super.onSceneLoaded(stage);
        try {
            loadServerFiles();
            loadLocalFiles();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Change the server directory by clicking the file name 2 times and list all
     * the files inside the chosen directory.
     * If the user just click the file name 1 times, the app will assign this name
     * to the variable that will use for choosing file for downloading.
     * 
     * @param event
     * @throws Throwable
     */
    @FXML
    private void changeServerDirectory(MouseEvent event) throws Throwable {
        String selectedFile = serverListView.getSelectionModel().getSelectedItem();

        if (selectedFile == null) {
            return;
        }

        String fileName = selectedFile;

        if (fileName.startsWith("[DIR] ")) {
            fileName = fileName.replaceFirst("\\[DIR\\] ", "");
        }

        if (event.getClickCount() == 2) {
            printMessage(ftp.cd(fileName));

            serverListView.getItems().clear();
            loadServerFiles();
            return;
        }

        if (event.getClickCount() == 1) {
            fileClicked = fileName;
            fileInformation = serverFileInfoMap.get(fileName);
            return;
        }
    }

    /**
     * List all the file from double-clicked directory in local file side.
     * 
     * @param event
     */
    @FXML
    private void changeLocalDirectory(MouseEvent event) {
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

    /**
     * After client send to user PWD command, the server will send back the message
     * "[Current path] is current directory".
     * So this function will extract the file name from this message.
     * 
     * @param path
     * @return
     * @throws IOException
     */

    private String extractCurrentDirectory(ArrayList<String> path) throws IOException {
        ArrayList<String> currentDirectoryFromServer = ftp.pwd();
        return currentDirectoryFromServer.get(0).split(" ")[1].replace("\"", "");
    }

    /**
     * Downloading a file button implementation
     * 
     * @param event
     * @throws Throwable
     */

    @FXML
    private void downloadAFile(ActionEvent event) throws Throwable {
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
        fileChooser.setTitle("Enter a name file");

        if (lastDirectory != null && lastDirectory.exists()) {
            fileChooser.setInitialDirectory(lastDirectory);
        }

        fileChooser.setInitialFileName(fileClicked);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile == null) {
            return;
        }

        lastDirectory = saveFile.getParentFile();

        ArrayList<String> currentDirectoryFromServer = ftp.pwd();
        String serverPath = extractCurrentDirectory(currentDirectoryFromServer) + "/" + fileClicked;

        printMessage(ftp.get(serverPath, saveFile.getAbsolutePath()));
        loadServerFiles();
        loadLocalFiles();
    }

    /**
     * Uploading a file button implementation
     * 
     * @param event
     * @throws Throwable
     */

    @FXML
    private void uploadAFile(ActionEvent event) throws Throwable {
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
        String serverPath = extractCurrentDirectory(currentDirectoryFromServer) + "/" + fileName;

        printMessage(ftp.put(selectedFile.getAbsolutePath(), serverPath));
        serverListView.getItems().clear();
        loadServerFiles();
        loadLocalFiles();
    }

    /**
     * Deleting a file button implementation
     * 
     * @param event
     * @throws Throwable
     */
    @FXML
    private void deleteAFile(ActionEvent event) throws Throwable {
        if (isFile(fileInformation)) {
            printMessage(ftp.delete(fileClicked));
            serverListView.getItems().clear();
            loadServerFiles();
            loadLocalFiles();
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
            loadServerFiles();
            loadLocalFiles();
            return;
        }
    }

    /**
     * Pwd button implementation
     * 
     * @param event
     * @throws IOException
     */

    @FXML
    private void showPWD(ActionEvent event) throws IOException {
        printMessage(ftp.pwd());
    }

    /**
     * Print the message reply after sending command to the server and put the
     * message to the server reply view.
     * 
     * @param message
     * @throws IOException
     */
    private void printMessage(ArrayList<String> message) throws IOException {
        replyFromServerView.getItems().addAll(message);
        if (!message.isEmpty()) {
            int lastindex = replyFromServerView.getItems().size() - 1;
            replyFromServerView.scrollTo(lastindex);
            replyFromServerView.getSelectionModel().select(lastindex);
        }
    }

    /**
     * Quit button implementation
     * 
     * @param event
     * @throws IOException
     */
    @FXML
    private void quit(ActionEvent event) throws IOException {
        ftp.disconnect();
        // Get current window
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.close();
    }

    /**
     * Go back to the parent file one step in Server side button implementation
     * 
     * @param event
     * @throws Throwable
     */
    @FXML
    private void goBackServer(ActionEvent event) throws Throwable {
        printMessage(ftp.cd(".."));
        serverListView.getItems().clear();
        loadServerFiles();
        loadLocalFiles();

    }

    /**
     * Go back to the parent file one step in Client side button implementation
     */
    @FXML
    private void goBackLocal() {
        File parent = currentLocalDirectory.getParentFile();

        if (parent != null) {
            currentLocalDirectory = parent;
            loadLocalFiles();
        }
    }

    /**
     * Sending command to server implementation
     * 
     * @throws Throwable
     */
    @FXML
    private void runCommand() throws Throwable {
        String userCommand = command.getText().trim();

        ftp.sendCommand(userCommand);
        printMessage(ftp.readReplyFromServer());

        serverListView.getItems().clear();
        loadServerFiles();
        loadLocalFiles();
        command.clear();

    }

    /**
     * Base on the file information, the function will check that this file
     * information is directory or not
     * 
     * @param listLine
     * @return
     */
    private boolean isDirectory(String listLine) {
        return listLine != null && listLine.startsWith("d");
    }

    /**
     * Base on the file information, the function will check that this file
     * information is directory or not
     * 
     * @param listLine
     * @return
     */
    private boolean isFile(String listLine) {
        return listLine != null && listLine.startsWith("-");
    }
}
