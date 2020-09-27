package main.chatapp.controllers;


import javafx.application.Platform;
import javafx.stage.FileChooser;
import main.chatapp.Constants;
import main.chatapp.Main;
import main.chatapp.repositories.ClientRepository;
import main.chatapp.repositories.ControllerInstance;
import main.chatapp.repositories.LoginControllerInstance;
import main.chatapp.repositories.MessageRepository;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Client {
    private String mUsername;
    private String mFilePath;
    private String otherClient;

    private DataInputStream mReader;
    private DataOutputStream mWriter;
    private final Socket mClientEndpoint;
    private Thread sendMessage;
    private Thread readMessage;
    private Thread waitThread;


    public Client(String username, String host, int serverPort) throws IOException {
        mUsername = username;
        mClientEndpoint = new Socket(host, serverPort);
        mReader = new DataInputStream(mClientEndpoint.getInputStream());
        mWriter = new DataOutputStream(mClientEndpoint.getOutputStream());

        waitThread = waitThread();
        sendMessage = sendMessage();
        readMessage = readMessage();
    }

    public void initialize() {
        checkUsernameThread().start();
    }

    public synchronized void stopAllThreads() {
        waitThread.interrupt();
        sendMessage.interrupt();
        readMessage.interrupt();

        waitThread = null;
        sendMessage = null;
        readMessage = null;
        mReader = null;
        mWriter = null;
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
            System.out.println("\n\n Image not found \n\n" + mFilePath);
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

            File savedFile;
            do {
                savedFile = fc.showSaveDialog(Main.getPrimaryStage());
            } while (savedFile == null);

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

            ControllerInstance
                    .getChatController()
                    .createMessageItem(Constants.TEXT, Constants.RECEIVE, 
                            "Server: File successfully received and downloaded.");
        });
    }

    private void sendText(String message) throws IOException {
        mWriter.writeUTF(mUsername + ":" + message);
        System.out.println("Message to send: " + message);

        if (message.equals("-logout")) {
            stopAllThreads();
        }
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
                    Platform.runLater(() -> {
                        LoginControllerInstance
                                .getLoginController()
                                .acceptClient();
                        waitThread.start();
                    });
                }

                else if (serverCheck.equals("-rejectUsername")) {
                    System.out.println("Username Thread: Rejected username - other user in matching");
                    Platform.runLater(() -> LoginControllerInstance
                            .getLoginController()
                            .rejectClient("   That username is taken."));
                }

                else if (serverCheck.equals("-full")) {
                    System.out.println("Username Thread: Rejected username - foreign user");
                    Platform.runLater(() -> LoginControllerInstance
                            .getLoginController()
                            .rejectClient("   The chat room is full."));
                }

                else if (serverCheck.equals("-activeClient")) {
                    System.out.println("Username Thread: Rejected username - active client");
                    Platform.runLater(() -> LoginControllerInstance
                            .getLoginController()
                            .rejectClient("   The client is currently active."));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Thread waitThread() {
        return new Thread(() -> {
            ClientRepository.initialize();

            // Filling the client list
            while (ClientRepository.getClientList().size() < 2) {
                try {
                    String serverMessage = mReader.readUTF();
                    if (!ClientRepository.containsClient(serverMessage))
                        ClientRepository.addClient(serverMessage);
                } catch (IOException e) {
                    break;
                }
            }

            System.out.println("Wait thread: After loop");

            // Choosing the other client
            otherClient = (ClientRepository.getClientList().get(0).equals(mUsername)) ?
                    ClientRepository.getClientList().get(1) :
                    ClientRepository.getClientList().get(0);

            // Show the chat pane
            Platform.runLater(() -> {
                ChatController controller = ControllerInstance.getChatController();
                controller.showChat(otherClient);
            });

            // Start the send and read threads
            sendMessage.start();
            readMessage.start();
        });
    }

    private Thread sendMessage() {
        return new Thread(() -> {
            MessageRepository.initialize();
            System.out.println("Send Message: start");
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
                            if (message.equals("-logout")) break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                MessageRepository.setNoChange();
            }
            System.out.println("Send Message: stop");
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            System.out.println("Read Message: start");
            while (!MessageRepository.getLastMessage().equals("-logout")) {
                try {
                    // Tokenize the message
                    String message = mReader.readUTF();
                    if (!MessageRepository.getLastMessage().equals("-logout")) {
                        StringTokenizer st = new StringTokenizer(message, ":");
                        String username = st.nextToken();
                        String command = (st.countTokens() >= 1) ?
                                st.nextToken() :
                                "";

                        System.out.println("Command: " + command);

                        if (command.equals("-disconnect")) {
                            Platform.runLater(() ->
                                    ControllerInstance.getChatController().onPartnerDisconnect());
                        } else if (command.equals("-reconnect")) {
                            Platform.runLater(() ->
                                    ControllerInstance.getChatController().onPartnerReconnect());
                        } else if (command.equals("-sendFile")) {
                            int bytes = Integer.parseInt(st.nextToken());
                            String extension = st.nextToken();
                            System.out.println("(Server: '" + username + "' is sending you a " + extension + " file)");

                            receiveFile(mFilePath, extension, bytes);
                        } else if (command.equals("-messageFailed")) {
                            Platform.runLater(() ->
                                    ControllerInstance
                                            .getChatController()
                                            .receiveMessage("Server: (Server - Your message failed to send)"));
                        } else if (command.equals("-fileFailed")) {
                            Platform.runLater(() ->
                                    ControllerInstance
                                            .getChatController()
                                            .receiveMessage("Server: (Server - Your file failed to send)"));
                        } else { // Receive a text message
                            if (!command.equals("-ownReconnect"))
                                Platform.runLater(() ->
                                        ControllerInstance.getChatController().receiveMessage(message));
                        }
                    }
                } catch (EOFException eof) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Read Message: stop");
        });
    }
}
