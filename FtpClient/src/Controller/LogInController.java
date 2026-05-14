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
        ArrayList<String> allReplys = ftp.readReplyFromServer();
        lastReply = allReplys.get(allReplys.size() - 1);
        message.setText(lastReply);
        if (lastReply.startsWith(REQUIRE_PASSWORD)) {
            ftp.loginPassword(password.getText());
            allReplys = ftp.readReplyFromServer();
            lastReply = allReplys.get(allReplys.size() - 1);
        }
        if (ftp.isNotLogin(lastReply)) {
            message.setText(lastReply);
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
