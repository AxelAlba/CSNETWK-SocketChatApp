package chatapp.controllers;


import chatapp.repositories.Messages;
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
        Messages.initialize();
        return new Thread(() -> {
            boolean flag = true;
            while (flag) {
                // manipulation of flag should not be in the try catch block
                // listen for new messages
                if (Messages.isChanged()) {
                    System.out.println("messages changed");

                    String message = Messages.getLastMessage();
                    System.out.println(message);
                    try {
                        mWriter.writeUTF(mUsername + ":" + message);
                        Messages.setIsChanged(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        flag = false;
                    }
                }
            }
        });
    }

    private Thread readMessage() {
        System.out.println("Read thread: start");
        return new Thread(() -> {
            boolean flag = true;
            while (flag) {
                try {
                    String message = mReader.readUTF();
                    System.out.println(message);

                    Messages.addMessage(message);

                    if (Messages.isChanged())
                        Platform.runLater(() -> {
                            ChatController controller = ControllerRepo.getChatController();
                            controller.receiveMessage(message);
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    flag = false;
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
                ChatController controller = ControllerRepo.getChatController();
                controller.showChat(otherClient);
            });

            sendMessage().start();
            readMessage().start();
        });
    }
}
