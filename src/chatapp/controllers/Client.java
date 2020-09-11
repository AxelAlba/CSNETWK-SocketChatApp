package chatapp.controllers;


import chatapp.repositories.ControllerInstance;
import chatapp.repositories.MessageRepository;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private final String mUsername;
    private final DataInputStream mReader;
    private final DataOutputStream mWriter;
    private final Socket mClientEndpoint;

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
            waitThread().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Thread sendMessage() {
        System.out.println("Send thread: start");
        MessageRepository.initialize();
        return new Thread(() -> {
            while (!MessageRepository.getLastMessage().equals("-logout")) {
                synchronized (this) {
                    while (!MessageRepository.isChanged()) {
                    }
                    if (MessageRepository.getMessageCount() > 0) {
                        String message = MessageRepository.getLastMessage();
                        try {
                            mWriter.writeUTF(mUsername + ":" + message);
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
        System.out.println("Read thread: start");
        return new Thread(() -> {
            while (!MessageRepository.getLastMessage().equals("-logout")) {
                try {
                    String message = mReader.readUTF();
                    MessageRepository.addMessage(message);

                    if (MessageRepository.isChanged()) {
                        Platform.runLater(() -> ControllerInstance.getChatController().receiveMessage(message));
                    }

                    MessageRepository.setNoChange();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread waitThread() {
        return new Thread(() -> {
            System.out.println("Wait: start");
            List<String> clients = new ArrayList<>();
            while (clients.size() < 2) {
                try {
                    String client = mReader.readUTF();
                    clients.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }

            otherClient = (clients.get(0).equals(mUsername)) ?
                    clients.get(1) :
                    clients.get(0);

            Platform.runLater(() -> {
                ChatController controller = ControllerInstance.getChatController();
                controller.showChat(otherClient);
                sendMessage().start();
                readMessage().start();
            });
        });
    }
}
