package Controller;

import static FTP.Constant.REQUIRE_PASSWORD;
import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class LogInController extends AbstractController {

    @FXML
    private PasswordField password;

    @FXML
    private TextField userName;

    @FXML
    private Label message;

    private String lastReply;
    @FXML
    void login(ActionEvent event) throws Throwable {
        ftp.loginUsername(userName.getText());
        // ftp.loginUsername(USER_NAME);
        ArrayList<String> allReplys = ftp.readReplyFromServer();
        lastReply = allReplys.get(allReplys.size() - 1);
        if (lastReply.startsWith(REQUIRE_PASSWORD)) {
            ftp.loginPassword(password.getText());
            // ftp.loginPassword(PASSWORD);
            allReplys = ftp.readReplyFromServer();
            lastReply = allReplys.get(allReplys.size() - 1);
        }
        if (ftp.isNotLogin(lastReply)) {
            message.setText("Wrong username or password");
        } else {
            SceneLoader sceneLoader = new SceneLoader(ftp);
            sceneLoader.loadScene(event, "/FXMLFile/Main.fxml");
        }

    }

    @FXML
    void initialize() {
        assert message != null : "fx:id=\"message\" was not injected: check your FXML file 'Login.fxml'.";
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Login.fxml'.";
        assert userName != null : "fx:id=\"userName\" was not injected: check your FXML file 'Login.fxml'.";
    }

}
