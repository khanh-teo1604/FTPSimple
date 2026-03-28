import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class FTPClient {
    /**
     * First we will connect the Client to the Server
     * This will connect ftp.gnu.org with default port 21
     */
    private Socket connectionSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String ipPassiveMode;
    private int portPassiveMode;

    public void connect(String server, int port) throws UnknownHostException, IOException {
        connectionSocket = new Socket(server, port);
        reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new PrintWriter(connectionSocket.getOutputStream(), true);

        readReplyFromServer();
    }

    public void login(String user, String password) throws IOException {
        String line = sendCommand("USER " + user);
        // When logging is succesfull but need to fill the password
        if (line.startsWith("331")) {
            sendCommand("PASS " + password);
        }
    }

    public String sendCommand(String command) throws IOException {
        if (writer.checkError()) {
            System.out.println("An error occur while writing");
        }
        writer.println(command);
        return readReplyFromServer();

    }

    public void disconnect() throws IOException {
        sendCommand("QUIT");
        connectionSocket.close();
    }

    public String readReplyFromServer() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);

            // Stop when line starts with a 3-digit code followed by a space
            if (line.matches("^[0-9]{3} .*")) {
                break;
            }
        }
        return line;
    }

    public void pwd() throws IOException {
        sendCommand("PWD");
    }

    // It will show as format(h1,h2,h3,h4,p1,p2)
    // Which means IP.address = h1.h2.h3.h4
    // Port p1 * 2^8 + p2
    // For example 227 Entering Passive Mode (209,51,188,20,93,52).
    public void passiveMode() throws IOException {
        String passiveReply = sendCommand("PASV");
        int start = passiveReply.indexOf('(');
        int end = passiveReply.indexOf(')');

        String[] allNumbers = passiveReply.substring(start + 1, end).split(",");
        ipPassiveMode = allNumbers[0] + "." + allNumbers[1] + "." + allNumbers[2] + "." + allNumbers[3];
        portPassiveMode = Integer.parseInt(allNumbers[4]) * 256 + Integer.parseInt(allNumbers[5]);
    }

    public void ls() throws IOException {
        passiveMode();

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        BufferedReader dataBufferedReader = new BufferedReader(
                new InputStreamReader(dataSocket.getInputStream()));

        sendCommand("LIST");

        String line;
        while ((line = dataBufferedReader.readLine()) != null) {
            System.out.println(line);
        }

        dataSocket.close();

        readReplyFromServer();
    }

    public void cd(String directory) throws IOException {
        sendCommand("CWD " + directory);
    }

    // TODO: need to handle when we can not open a file (Ex: folder)
    public void get(String fromServerFile, String toLocalFile) throws IOException {
        passiveMode();

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        DataInputStream dataStream = new DataInputStream(dataSocket.getInputStream());

        sendCommand("RETR " + fromServerFile);

        FileOutputStream fileOutputStream = new FileOutputStream(toLocalFile);
        byte[] bufferBytes = new byte[65535];
        int lengthByteRead;

        // A file is just simply contains the raw bytes
        while ((lengthByteRead = dataStream.read(bufferBytes)) != -1) {
            fileOutputStream.write(bufferBytes, 0, lengthByteRead);
        }

        fileOutputStream.close();
        dataSocket.close();

        readReplyFromServer();
    }

    // Access denied (Actually not our problem)
    public void put(String fromLocalFile, String toServerFile) throws IOException {
        passiveMode();

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        DataOutputStream dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());

        sendCommand("STOR " + toServerFile);

        FileInputStream fileInputStream = new FileInputStream(fromLocalFile);
        byte[] bufferBytes = new byte[65535];
        int lengthByteRead;
        while ((lengthByteRead = fileInputStream.read(bufferBytes)) != -1) {
            dataOutputStream.write(bufferBytes, 0, lengthByteRead);
        }

        dataOutputStream.flush();

        fileInputStream.close();
        dataOutputStream.close();
        dataSocket.close();

        readReplyFromServer();
    }

    public void delete(String fileName) throws IOException {
        sendCommand("DELE " + fileName);
    }

    public void mkdir(String directory) throws IOException {
        sendCommand("MKD " + directory);
    }

    public void rmdir(String directory) throws IOException {
        sendCommand("RMD " + directory);
    }
}
