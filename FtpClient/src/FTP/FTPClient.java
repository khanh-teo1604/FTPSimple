package FTP;

import static FTP.Constant.LOGIN_UNSUCCESSFUL;
import static FTP.Constant.PASSIVE_MODE;
import static FTP.Constant.PERMISSION_DENIED;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

    /**
     * Establish connection.
     * 
     * @param server
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     */
    public void connect(String server, int port) throws UnknownHostException, IOException {
        connectionSocket = new Socket(server, port);
    }

    /**
     * Initialization Reader and writer for FTP connection
     * 
     * @throws IOException
     */
    public void initializeStreams() throws IOException {
        reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new PrintWriter(connectionSocket.getOutputStream(), true);
    }

    /**
     * After sending command to server, the server will send back the response
     * message to the client. This function only return the specific code message.
     * 
     * @param code
     * @return
     * @throws IOException
     */

    public ArrayList<String> readReplyWithSpecificCode(String code) throws IOException {
        ArrayList<String> allLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            // It read until a specific reply code with a space
            if (line.startsWith(code)) {
                allLines.add(line);
                if (line.startsWith(code + " "))
                    break;
            }
        }
        return allLines;
    }

    // Consider in case when we need only username not password

    /**
     * Send user name command to server
     * 
     * @param username
     * @throws IOException
     */
    public void loginUsername(String username) throws IOException {
        sendCommand("USER " + username);
    }

    /**
     * Send password command to server
     * 
     * @param password
     * @throws IOException
     */
    public void loginPassword(String password) throws IOException {
        sendCommand("PASS " + password);

    }

    /**
     * Check Login sucessful or not
     * 
     * @param reply
     * @return
     */
    public boolean isNotLogin(String reply) {
        if (reply.startsWith(LOGIN_UNSUCCESSFUL))
            return true;
        return false;
    }

    /**
     * Send command to server
     * 
     * @param command
     * @throws IOException
     */
    public void sendCommand(String command) throws IOException {
        if (writer.checkError()) {
            System.out.println("An error occur while writing");
        }
        writer.println(command);
    }

    /**
     * Send quit command
     * 
     * @throws IOException
     */
    public void disconnect() throws IOException {
        sendCommand("QUIT");
        connectionSocket.close();
    }

    /**
     * Read reply response after sending command to server
     * 
     * @return
     * @throws IOException
     */

    public ArrayList<String> readReplyFromServer() throws IOException {
        ArrayList<String> allLines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            // Stop when line starts with a 3-digit code followed by a space
            allLines.add(line);
            if (line.matches("^[0-9]{3} .*")) {
                break;
            }
        }
        return allLines;
    }

    /**
     * Send pwd comand
     * 
     * @return
     * @throws IOException
     */

    public ArrayList<String> pwd() throws IOException {
        sendCommand("PWD");
        return readReplyFromServer();
    }

    /**
     * Send Passive command. The return message contains a list with
     * format(h1,h2,h3,h4,p1,p2). Which means
     * IP.address = h1.h2.h3.h4. Port = p1 * 2^8 + p2.
     * For example 227 Entering Passive Mode (209,51,188,20,93,52).
     * 
     * @return
     * @throws IOException
     */
    public ArrayList<String> setPassiveMode() throws IOException {
        sendCommand("PASV");
        ArrayList<String> passiveReplys = readReplyWithSpecificCode(PASSIVE_MODE);
        String passiveReply = passiveReplys.get(passiveReplys.size() - 1);
        int start = passiveReply.indexOf('(');
        int end = passiveReply.indexOf(')');

        String[] allNumbers = passiveReply.substring(start + 1, end).split(",");
        ipPassiveMode = allNumbers[0] + "." + allNumbers[1] + "." + allNumbers[2] + "." + allNumbers[3];
        portPassiveMode = Integer.parseInt(allNumbers[4]) * 256 + Integer.parseInt(allNumbers[5]);
        return passiveReplys;
    }

    /**
     * List comand
     * @return
     * @throws IOException
     */

    public FTPListResult ls() throws IOException {
        ArrayList<String> allReplies = new ArrayList<>();
        ArrayList<String> passiveReplies = setPassiveMode();
        allReplies.addAll(passiveReplies);

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        BufferedReader dataBufferedReader = new BufferedReader(
                new InputStreamReader(dataSocket.getInputStream()));

        sendCommand("LIST");
        ArrayList<String> startReplys = readReplyFromServer();
        allReplies.addAll(startReplys);

        ArrayList<String> allFolders = new ArrayList<>();
        String line;
        while ((line = dataBufferedReader.readLine()) != null) {
            allFolders.add(line);
        }

        dataSocket.close();

        ArrayList<String> finishReply = readReplyFromServer();
        allReplies.addAll(finishReply);

        return new FTPListResult(allReplies, allFolders);

    }

    /**
     * Change directory command
     * @param directory
     * @return
     * @throws IOException
     */
    public ArrayList<String> cd(String directory) throws IOException {
        sendCommand("CWD " + directory);
        return readReplyFromServer();
    }

    /**
     * Download command
     * @param fromServerFile
     * @param toLocalFile
     * @return
     * @throws IOException
     */
    public ArrayList<String> get(String fromServerFile, String toLocalFile) throws IOException {
        ArrayList<String> allReplies = new ArrayList<>();
        allReplies.addAll(setPassiveMode());

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        DataInputStream dataStream = new DataInputStream(dataSocket.getInputStream());

        sendCommand("RETR " + fromServerFile);

        ArrayList<String> replyAfterRetr = readReplyFromServer();
        allReplies.addAll(replyAfterRetr);
        String reply = replyAfterRetr.get(replyAfterRetr.size() - 1);
        if (reply.startsWith(PERMISSION_DENIED)) {
            dataSocket.close();
            return allReplies;
        }

        FileOutputStream fileOutputStream = new FileOutputStream(toLocalFile);
        byte[] bufferBytes = new byte[65535];
        int lengthByteRead;

        // A file is just simply contains the raw bytes
        while ((lengthByteRead = dataStream.read(bufferBytes)) != -1) {
            fileOutputStream.write(bufferBytes, 0, lengthByteRead);
        }

        fileOutputStream.close();
        dataSocket.close();
        ArrayList<String> replyAfterTransfer = readReplyFromServer();
        allReplies.addAll(replyAfterTransfer);

        return allReplies;

    }

    /**
     * Upload command
     * @param fromLocalFile
     * @param toServerFile
     * @return
     * @throws IOException
     */
    public ArrayList<String> put(String fromLocalFile, String toServerFile) throws IOException {
        ArrayList<String> allReplies = new ArrayList<>();
        allReplies.addAll(setPassiveMode());

        Socket dataSocket = new Socket(ipPassiveMode, portPassiveMode);
        DataOutputStream dataOutputStream = new DataOutputStream(dataSocket.getOutputStream());

        sendCommand("STOR " + toServerFile);
        ArrayList<String> replyAfterStor = readReplyFromServer();
        allReplies.addAll(replyAfterStor);
        String reply = replyAfterStor.get(replyAfterStor.size() - 1);

        if (reply.startsWith(PERMISSION_DENIED)) {
            dataSocket.close();
            return allReplies;
        }

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

        ArrayList<String> replyAfterTransfer = readReplyFromServer();
        allReplies.addAll(replyAfterTransfer);
        return allReplies;
    }

    /**
     * Delete command
     * @param fileName
     * @return
     * @throws IOException
     */
    public ArrayList<String> delete(String fileName) throws IOException {
        sendCommand("DELE " + fileName);
        return readReplyFromServer();
    }

    /**
     * Make directory command
     * @param directory
     * @return
     * @throws IOException
     */
    public ArrayList<String> mkdir(String directory) throws IOException {
        sendCommand("MKD " + directory);
        return readReplyFromServer();
    }

    /**
     * Remove directory command
     * @param directory
     * @return
     * @throws IOException
     */
    public ArrayList<String> rmdir(String directory) throws IOException {
        sendCommand("RMD " + directory);
        return readReplyFromServer();
    }
}
