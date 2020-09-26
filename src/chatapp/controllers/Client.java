package chatapp.controllers;


import chatapp.Constants;
import chatapp.repositories.ClientRepository;
import chatapp.repositories.ControllerInstance;
import chatapp.repositories.FileRepository;
import chatapp.repositories.MessageRepository;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private final String mUsername;
    private String mFilePath;
    private DataInputStream mReader;
    private DataOutputStream mWriter;
    private final Socket mClientEndpoint;
    private Thread sendMessage, readMessage, waitThread, checkUsernameThread;
    private AtomicBoolean running;

    private String otherClient;


    public Client(String username, String host, int serverPort) throws IOException {
        mUsername = username;
        mClientEndpoint = new Socket(host, serverPort);
        mReader = new DataInputStream(mClientEndpoint.getInputStream());
        mWriter = new DataOutputStream(mClientEndpoint.getOutputStream());

        waitThread = waitThread();
        sendMessage = sendMessage();
        readMessage = readMessage();
        checkUsernameThread = checkUsernameThread();

        running = new AtomicBoolean(true);
    }

    public void initialize() {
//        checkUsernameThread().start();
        try {
            System.out.println("Successfully connected to server at " + mClientEndpoint.getRemoteSocketAddress());
            mWriter.writeUTF(mUsername); // Signals the server to send the client list

            // Perform handling of accept/reject here
            String serverCheck = mReader.readUTF();
            if (serverCheck.equals("-rejectUsername")) {
                ClientRepository.rejectClient();
            } else if (serverCheck.equals("-acceptUsername")) {
                waitThread.start(); // Proceed to the waiting room
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAllThreads() {
        running.set(false);
    }

    public String getUsername() { return mUsername; }

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
            fileContent.add(b);
            totalBytes += bytesRead;
        }

        System.out.println("(Server: " + "received" + "." + extension + " downloaded)");

        // Create file message item in chat controller
        Platform.runLater(() -> {
            FileRepository.setCurrentFile(fileContent);
            ControllerInstance
                    .getChatController()
                    .createMessageItem(Constants.FILE, Constants.RECEIVE, mFilePath);
        });
    }

    private void sendText(String message) throws IOException {
        mWriter.writeUTF(mUsername + ":" + message);
    }

    private Thread sendMessage() {
        MessageRepository.initialize();

        return new Thread(() -> {
            System.out.println("Send Message: start");
            while (running.get()) {
                while (!MessageRepository.getLastMessage().equals("-logout")) {
                    while (!MessageRepository.isChanged());
                    if (MessageRepository.getMessageCount() > 0) {
                        try {
                            String message = MessageRepository.getLastMessage();
                            System.out.println("Last message: " + message);
                            String command = getCommandFromMessage(message);

                            if (command.equals("-sendFile")) {
                                System.out.println("Enter -sendFile of send Thread");
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
            }
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            System.out.println("read Message: start");
            while (running.get()) {
                while (!MessageRepository.getLastMessage().equals("-logout")) {
                    try {
                        // Tokenize the message
                        String message = mReader.readUTF();
                        StringTokenizer st = new StringTokenizer(message, ":");
                        String username = st.nextToken();
                        String command = (st.countTokens() >= 1) ?
                                st.nextToken() :
                                "";

                        System.out.println("read thread message:" + message);
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
                }
            }
        });
    }

    private Thread waitThread() {
        return new Thread(() -> {
            ClientRepository.initialize();

            // Filling the client list
            while (ClientRepository.getClientList().size() < 2) {
                try {
                    String client = mReader.readUTF();
                    if (!ClientRepository.containsClient(client))
                        ClientRepository.addClient(client);
                } catch (IOException e) {
                    break;
                }
            }

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

    private Thread checkUsernameThread() {
        return new Thread(() -> {
            while (running.get()) {
                try {
                    // Send the username to the server
                    mWriter.writeUTF(mUsername);

                    // Let server check the sent username
                    String serverCheck = mReader.readUTF(); // this is not being triggered

                    if (serverCheck.equals("-acceptUsername")) {
                        System.out.println("Successfully connected to server at " + mClientEndpoint.getRemoteSocketAddress());
                        Platform.runLater(() ->
                                ClientRepository.acceptClient());
                        running.set(false);
                    } else if (serverCheck.equals("-rejectUsername")) {
                        Platform.runLater(() ->
                                ClientRepository.rejectClient());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> waitThread.start());
        });
    }
}
