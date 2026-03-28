public class App {
    public static void main(String[] args) throws Exception {
        FTPClient ftp = new FTPClient();
        ftp.connect("localhost", 21);
        // for filezilla
        ftp.login("test1", "khanhteo1604");
        ftp.pwd();
        ftp.cd("/test1");
        ftp.ls();
        ftp.put("localReadme", "firstFile");
        ftp.ls();
        ftp.mkdir("test2");
        ftp.rmdir("test2");
        ftp.disconnect();
    }
}
