package Controller;

import java.io.IOException;
import java.net.UnknownHostException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import static FTP.Constant.CONNECT_SUCCESSFUL;

public class ConnectController extends AbstractController {

    @FXML
    private Label message;

    @FXML
    private TextField port;

    @FXML
    private TextField server;

    @FXML
    void connect(ActionEvent event) throws Throwable {
        try {
            int portNumber = Integer.parseInt(port.getText());
            ftp.connect(server.getText(), portNumber);
            // ftp.connect(FTP_SERVER, PORT);
            ftp.initializeStreams();
            SceneLoader sceneLoader = new SceneLoader(ftp);
            printArrayString(ftp.readReplyWithSpecificCode(CONNECT_SUCCESSFUL));
            sceneLoader.loadScene(event, "/FXMLFile/Login.fxml");

        } catch (NumberFormatException e) {
            // TODO: handle exception
            message.setText("Wrong format for port (Expect integer)");
        } catch (UnknownHostException e) {
            message.setText("Unknown host");
        } catch (IOException e) {
            message.setText("Cannot connect");
        }
    }

}
