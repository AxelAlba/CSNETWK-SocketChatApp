package chatapp.controllers;


import chatapp.repositories.ClientRepository;
import chatapp.repositories.ControllerInstance;
import chatapp.repositories.MessageRepository;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final String mUsername;
    private final DataInputStream mReader;
    private final DataOutputStream mWriter;
    private final Socket mClientEndpoint;
    private Thread sendMessage, readMessage, waitThread;

    private String otherClient;


    public Client(String username, String host, int serverPort) throws IOException {
        mUsername = username;
        mClientEndpoint = new Socket(host, serverPort);
        mReader = new DataInputStream(mClientEndpoint.getInputStream());
        mWriter = new DataOutputStream(mClientEndpoint.getOutputStream());
    }

    public void initialize() {
        try {
            System.out.println("Successfully connected to server at " + mClientEndpoint.getRemoteSocketAddress());

            mWriter.writeUTF(mUsername);
            waitThread = waitThread();
            sendMessage = sendMessage();
            readMessage = readMessage();

            waitThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() { return mUsername; }

    private Thread sendMessage() {
        MessageRepository.initialize();
        return new Thread(() -> {
            while (!MessageRepository.getLastMessage().equals("-logout")) {
                while (!MessageRepository.isChanged());
                if (MessageRepository.getMessageCount() > 0) {
                    try {
                        String message = MessageRepository.getLastMessage();
                        mWriter.writeUTF(mUsername + ":" + message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                MessageRepository.setNoChange();
            }
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            while (!MessageRepository.getLastMessage().equals("-logout")) {
                try {
                    String message = mReader.readUTF();

                    if (message.equals(otherClient + ":-disconnect")) {
                        Platform.runLater(() ->
                                ControllerInstance.getChatController().onPartnerDisconnect());
                    } else if (message.equals(otherClient + ":-reconnect")) {
                        Platform.runLater(() ->
                                ControllerInstance.getChatController().onPartnerReconnect());
                    } else {
                        Platform.runLater(() ->
                                ControllerInstance.getChatController().receiveMessage(message));
                    }
                } catch (EOFException eof) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread waitThread() {
        return new Thread(() -> {
            List<String> clientList = new ArrayList<>();

            while (clientList.size() < 2) {
                try {
                    String client = mReader.readUTF();
                    clientList.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            ClientRepository.initialize(clientList);
            otherClient = (clientList.get(0).equals(mUsername)) ?
                    clientList.get(1) :
                    clientList.get(0);

            Platform.runLater(() -> {
                ChatController controller = ControllerInstance.getChatController();
                controller.showChat(otherClient);
                sendMessage.start();
                readMessage.start();
            });
        });
    }

    public void stopAllThreads() {
        waitThread = null;
        sendMessage = null;
        readMessage = null;
    }
}
