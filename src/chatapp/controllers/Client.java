package chatapp.controllers;


import chatapp.Constants;
import chatapp.Main;
import chatapp.repositories.*;
import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private String mUsername;
    private String mFilePath;
    private final DataInputStream mReader;
    private final DataOutputStream mWriter;
    private final Socket mClientEndpoint;
    private Thread sendMessage;
    private Thread readMessage;
    private Thread waitThread;
    private final AtomicBoolean running;

    private String otherClient;


    public Client(String username, String host, int serverPort) throws IOException {
        mUsername = username;
        mClientEndpoint = new Socket(host, serverPort);
        mReader = new DataInputStream(mClientEndpoint.getInputStream());
        mWriter = new DataOutputStream(mClientEndpoint.getOutputStream());

        waitThread = waitThread();
        sendMessage = sendMessage();
        readMessage = readMessage();

        running = new AtomicBoolean(true);
    }

    public void initialize() {
        checkUsernameThread().start();
    }

    public void stopAllThreads() {
//        running.set(false);
        waitThread = null;
        sendMessage = null;
        readMessage = null;
    }

    public String getUsername() { return mUsername; }

    public void setUsername(String username) {
        mUsername = username;
    }

    private String getCommandFromMessage(String message) {
        String command = "";
        StringTokenizer st = new StringTokenizer(message, " ");
        if (st.countTokens() >= 2)
            command = message.substring(0, message.indexOf(' '));

        return command;
    }

    private void sendFile(String message) {
        try {
            mFilePath = message.substring(message.indexOf(' ') + 1);
            FileInputStream fileInput = new FileInputStream(mFilePath);
            BufferedInputStream bis = new BufferedInputStream(fileInput);
            int bytes = (int) fileInput.getChannel().size();

            // Form the message to send to server
            String extension = mFilePath.substring(mFilePath.lastIndexOf('.') + 1);
            String fileSendMessage =
                String.format("%s:-sendFile:%d:%s ", mUsername, bytes, extension);

            System.out.println(fileSendMessage);

            // Send signal to server
            mWriter.writeUTF(fileSendMessage);

            // Chunk the bytes then send to server
            byte[] b = new byte[1500]; // maximum packet size for TCP: 1500 bytes
            int bytesRead;
            while ((bytesRead = bis.read(b)) > 0)
                mWriter.write(b, 0, bytesRead);

            bis.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String path, String extension, int bytes) throws IOException {
        // Get the chunks of bytes from the server
        byte[] b = new byte[1500];
        List<byte[]> fileContent = new ArrayList<>();
        int bytesRead = 0;
        int totalBytes = 0;
        while (totalBytes < bytes) {
            bytesRead = mReader.read(b);

            byte[] chunk = new byte[bytesRead];
            for (int i=0; i < bytesRead; i++)
                chunk[i] = b[i];

            fileContent.add(chunk);
            totalBytes += bytesRead;
        }

        System.out.println("(Server: " + "received" + "." + extension + " downloaded)");

        // Trigger a file chooser from the view
        Platform.runLater(() -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("You have received a file!");
            String[] filters = {"*.jpg", "*.png", "*.txt"};
            fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text or Image Files", filters)
            );

            File savedFile = fc.showSaveDialog(Main.getPrimaryStage());
            if (savedFile != null) {
                String filePath = savedFile.getPath();

                // Write file locally
                try {
                    FileOutputStream writer = new FileOutputStream(filePath);
                    for (byte[] chunk : fileContent)
                        writer.write(chunk, 0, chunk.length);

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ControllerInstance
                    .getChatController()
                    .createMessageItem(Constants.TEXT, Constants.RECEIVE, "File received and downloaded.");
        });
    }

    private void sendText(String message) throws IOException {
        mWriter.writeUTF(mUsername + ":" + message);
    }


    /**
     * Threads
     */
    private Thread checkUsernameThread() {
        return new Thread(() -> {
            System.out.println("Client: enter check username thread");
            try {
                // Send the username to the server
                mWriter.writeUTF(mUsername);

                // Let server check the sent username
                String serverCheck = mReader.readUTF();

                System.out.println("Server: " + serverCheck);

                if (serverCheck.equals("-acceptUsername")) {
                    System.out.println("Successfully connected to server at " +
                            mClientEndpoint.getRemoteSocketAddress());

                    // For checking duplicate username when other user is in
                    // the chat room
                    serverCheck = mReader.readUTF(); // Await the -full message
                    String[] serverTokens = serverCheck.split(":");
                    serverCheck = serverTokens.length > 1 ?
                            serverTokens[1] : // -full command
                            serverTokens[0];

                    System.out.println("Server inside: " + serverCheck);

                    if (serverCheck.equals("-full")) {
                        System.out.println("Username Thread: Rejected username - other user in room");
                        Platform.runLater(() -> LoginControllerInstance
                                .getLoginController()
                                .rejectClient());
                    } else {
                        System.out.println("Username Thread: Accepted username");
                        Platform.runLater(() -> {
                            LoginControllerInstance
                                    .getLoginController()
                                    .acceptClient();
                            waitThread.start();
                        });
                    }
                }

                // For checking duplicate username when the other user is in
                // the matching room
                else if (serverCheck.equals("-rejectUsername")) {
                    System.out.println("Username Thread: Rejected username - other user in matching");
                    Platform.runLater(() -> LoginControllerInstance
                            .getLoginController()
                            .rejectClient());
                }

                // For checking third username when room is already full
                else if (serverCheck.equals("-full")) {
                    System.out.println("Username Thread: Rejected username - foreign user");
                    Platform.runLater(() -> LoginControllerInstance
                            .getLoginController()
                            .rejectClient());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Thread waitThread() {
        return new Thread(() -> {
            boolean canProceed = true;

            ClientRepository.initialize();

            // Filling the client list
            while (ClientRepository.getClientList().size() < 2) {
                try {
                    String serverMessage = mReader.readUTF();

                    System.out.println("Wait thread: " + serverMessage);

                    if (serverMessage.equals("-full")) {
                        canProceed = false;
                        Platform.runLater(() ->
                                LoginControllerInstance.getLoginController().rejectClient());
                    } else {
                        if (!ClientRepository.containsClient(serverMessage))
                            ClientRepository.addClient(serverMessage);
                    }
                } catch (IOException e) {
                    break;
                }
            }

            System.out.println("Wait thread: After loop");

            if (canProceed) {
                // Choosing the other client
                otherClient = (ClientRepository.getClientList().get(0).equals(mUsername)) ?
                        ClientRepository.getClientList().get(1) :
                        ClientRepository.getClientList().get(0);

                // Show the chat pane
                Platform.runLater(() -> {
                    ChatController controller = ControllerInstance.getChatController();
                    controller.showChat(otherClient);
                    System.out.println("App thread: Show chat");
                });

                // Start the send and read threads
                sendMessage.start();
                readMessage.start();
            } else {

            }
        });
    }

    private Thread sendMessage() {
        MessageRepository.initialize();

        return new Thread(() -> {
            System.out.println("Send Message: start");
//            while (running.get()) {
                while (!MessageRepository.getLastMessage().equals("-logout")) {
                    while (!MessageRepository.isChanged());

                    if (MessageRepository.getMessageCount() > 0) {
                        try {
                            String message = MessageRepository.getLastMessage();
                            System.out.println("Send thread: " + message);
                            String command = getCommandFromMessage(message);

                            if (command.equals("-sendFile")) {
                                sendFile(message);
                            } else {
                                sendText(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    MessageRepository.setNoChange();
                }
//            }
            System.out.println("Send Message: stop");
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            System.out.println("Read Message: start");
//            while (running.get()) {
                while (!MessageRepository.getLastMessage().equals("-logout")) {
                    try {
                        // Tokenize the message
                        String message = mReader.readUTF();
                        StringTokenizer st = new StringTokenizer(message, ":");
                        String username = st.nextToken();
                        String command = (st.countTokens() >= 1) ?
                                st.nextToken() :
                                "";

                        System.out.println("Command: " + command);

                        if (command.equals("-disconnect")) {
                            Platform.runLater(() ->
                                    ControllerInstance.getChatController().onPartnerDisconnect());
                        }

                        else if (command.equals("-reconnect")) {
                            Platform.runLater(() ->
                                    ControllerInstance.getChatController().onPartnerReconnect());
                        }

                        else if (command.equals("-sendFile")) {
                            int bytes = Integer.parseInt(st.nextToken());
                            String extension = st.nextToken();
                            System.out.println("(Server: '" + username + "' is sending you a " + extension + " file)");

                            receiveFile(mFilePath, extension, bytes);
                        }

                        else { // Receive a text message
                            if (!command.equals("-ownReconnect"))
                                Platform.runLater(() ->
                                        ControllerInstance.getChatController().receiveMessage(message));
                        }
                    } catch (EOFException eof) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                }
            }
            System.out.println("Read Message: stop");
        });
    }
}
