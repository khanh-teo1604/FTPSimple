package FTP;

import java.util.ArrayList;

public class FTPListResult {
    private final ArrayList<String> replies;
    private final ArrayList<String> files;

    public FTPListResult(ArrayList<String> replies, ArrayList<String> files) {
        this.replies = replies;
        this.files = files;
    }

    public ArrayList<String> getReplies() {
        return replies;
    }

    public ArrayList<String> getFiles() {
        return files;
    }
}
