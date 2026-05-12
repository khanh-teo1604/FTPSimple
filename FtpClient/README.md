# Java FTP Client

Bachelor Project: Build an FTP Client in Java.

This project implements a simple FTP client with a simple GUI friendly for users. It can connect to an FTP server, log in, browse local files, browse remote server files, upload files, download files, delete files, change directories, and show replies from the FTP server.

The project is designed for learning how the FTP protocol works, especially the difference between the control connection and the data connection used by commands such as `LIST`, `RETR`, and `STOR`.

## Project Goal

Implement an FTP client using Java that can interact with a public FTP server, for example `ftp.gnu.org`, or a local FTP server.

The client must support:

- Connect to FTP server on the default port `21`
- Anonymous login and custom username/password login
- `pwd` using the FTP `PWD` command
- `cd` using the FTP `CWD` command
- `ls` using `PASV` + `LIST`
- `get` using `PASV` + `RETR` in binary mode
- `put` using `PASV` + `STOR` in binary mode
- `delete` using the FTP `DELE` command
- `mkdir` using the FTP `MKD` command
- `rmdir` using the FTP `RMD` command
- `quit` using the FTP `QUIT` command

## Library Rules

For the FTP protocol implementation, only these Java packages are allowed:

```java
java.io.*
java.net.*
java.util.*
```

## Requirements

Install these before running the project:

- Java JDK 17 or newer
- JavaFX SDK
- An IDE such as VS Code, IntelliJ IDEA, or Eclipse

This project uses JavaFX FXML files, so JavaFX must be configured correctly in the classpath or module path.

The `lib` folder is available for extra `.jar` files if needed, but the FTP implementation itself should not use third-party FTP libraries.

## Project Structure

```text
FtpClient/
+-- src/
|   +-- App.java
|   +-- Controller/
|   |   +-- MainController.java
|   |   +-- ConnectController.java
|   |   +-- ...
|   +-- FTP/
|   |   +-- FTPClient.java
|   |   +-- Constant.java
|   +-- FXMLFile/
|       +-- Main.fxml
|       +-- Connection.fxml
|       +-- ...
+-- bin/
|   +-- compiled .class files
+-- lib/
|   +-- optional .jar libraries
+-- .vscode/
|   +-- settings.json
|   +-- launch.json
+-- README.md
```

Important folders:

- `src`: Java source code and FXML files
- `src/FTP`: FTP command and socket logic
- `src/Controller`: JavaFX controller classes
- `src/FXMLFile`: JavaFX layout files
- `bin`: compiled output
- `lib`: optional external jar files

## Setup in VS Code

1. Download or clone this project.
2. Open the `FtpClient` folder in VS Code.
3. Install the Java extensions if VS Code asks for them.
4. Download the JavaFX SDK from the official JavaFX website.
5. Extract the JavaFX SDK somewhere on your computer.
6. Open `.vscode/settings.json`.
7. Replace the existing JavaFX jar paths with your own JavaFX SDK `lib` path.

Example JavaFX SDK path on Windows:

```text
C:\javafx-sdk-26\lib
```

Example referenced libraries:

```json
"java.project.referencedLibraries": [
    "lib/**/*.jar",
    "C:\\javafx-sdk-26\\lib\\javafx.base.jar",
    "C:\\javafx-sdk-26\\lib\\javafx.controls.jar",
    "C:\\javafx-sdk-26\\lib\\javafx.fxml.jar",
    "C:\\javafx-sdk-26\\lib\\javafx.graphics.jar"
]
```

Then open `.vscode/launch.json` and update the JavaFX module path:

```json
"vmArgs": "--module-path C:\\javafx-sdk-26\\lib --add-modules javafx.controls,javafx.fxml"
```

Use your own JavaFX path instead of `C:\\javafx-sdk-26\\lib`.

## Setup in IntelliJ IDEA

1. Open the project folder in IntelliJ IDEA.
2. Make sure the project SDK is set to JDK 17 or newer.
3. Add the JavaFX SDK `lib` folder as a library.
4. Open the run configuration for `App.java`.
5. Add VM options:

```text
--module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml
```

Replace the path with your own JavaFX SDK path.

## How to Run

The main class is:

```text
src/App.java
```

In VS Code, select the `App` launch configuration and press Run.

You can also compile and run from the terminal. Example on Windows PowerShell:

```powershell
javac --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -d bin (Get-ChildItem -Path src -Recurse -Filter *.java).FullName
java --module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;src" App
```

Notes:

- Replace `C:\javafx-sdk-26\lib` with your real JavaFX SDK path.
- The `src` folder is included in the classpath so Java can find the FXML files.
- On Windows, use `;` between classpath entries, for example `bin;src`.
- On macOS/Linux, use `:` between classpath entries, for example `bin:src`.

## How to Use the App

1. Start the app by running `App.java`.
2. Enter the FTP server host, port, username, and password.
3. Connect to the FTP server.
4. Use the left file list to browse local files.
5. Use the right file list to browse server files.
6. Use the response area to view FTP server replies.

Main buttons:

- `Back in Local`: go back to the parent local folder
- `Back in Server`: go back to the parent server folder
- `pwd`: show the current server directory
- `Download`: download the selected server file
- `Upload`: upload a selected local file
- `Delete`: delete the selected server file
- `Quit`: disconnect from the server and close the app

Command text field examples:

```text
pwd
ls
cwd folderName
pwd
pasv
list
retr filename
stor Filename
quit
```

Commands with spaces in the folder or file name should keep the command separate from the rest of the text. For example:

```text
cd my folder
```

The command is `cd`, and the folder name is `my folder`.

## Common Problems

### JavaFX not found

Check that the JavaFX SDK path in `.vscode/settings.json` and `.vscode/launch.json` matches your computer.

Wrong example:

```text
C:\Users\someone_else\Downloads\javafx-sdk-26\lib
```

Correct example:

```text
C:\your-own-folder\javafx-sdk-26\lib
```

### FXML file not found

Make sure the `src` folder is included in the classpath when running the app. The FXML files are inside:

```text
src/FXMLFile/
```

### Cannot connect to FTP server

Check:

- Server address is correct
- Port is usually `21`
- Username and password are correct
- Your network allows FTP connections
- The FTP server is running

### Passive mode or listing problem

The `ls`, `get`, and `put` commands need passive mode. The client sends `PASV`, reads the server IP and port, opens a data socket, then sends `LIST`, `RETR`, or `STOR`.

If listing does not work, check that:

- The server supports passive mode
- Your firewall allows the data connection
- The passive mode reply is parsed correctly

### Permission denied when deleting folders

Some FTP servers do not allow removing non-empty folders. Delete the files inside the folder first, then try `rmdir` again.

## Flow of FTPClient

```text
MainController calls FTPClient
FTPClient sends FTP command
FTPClient reads server replies
FTPClient returns replies/data
MainController displays replies/data in the GUI
```
