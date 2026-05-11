package FTP;

public final class Constant {
    private Constant() {
        // Can not initialize
    }

    public static String USER_NAME = "test1";
    public static String PASSWORD = "khanhteo1604";
    public static String FTP_SERVER = "localhost";
    public static int PORT = 21;
    public static String REQUIRE_PASSWORD = "331";
    public static String CONNECT_SUCCESSFUL = "220";
    public static String LOGIN_SUCCESSFUL = "230";
    public static String LOGIN_UNSUCCESSFUL = "530";
    public static String PASSIVE_MODE = "227";
    public static String LIST_COMMAND = "150";
    public static String PERMISSION_DENIED = "550";
    public static String STARTING_DATA_TRANSFER = "150";
    public static String TRANSMISSION_SUCCESSFUL = "226";
    public static String SHOW_CURRENT_DIRECTORY = "257";
}
