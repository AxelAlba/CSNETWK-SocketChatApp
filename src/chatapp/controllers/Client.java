package chatapp.controllers;


import chatapp.repositories.ControllerRepo;
import chatapp.repositories.MessageRepo;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private final String mUsername;
    private final DataInputStream mReader;
    private final DataOutputStream mWriter;
    private final Socket mClientEndpoint;


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

            sendMessage().start();
            readMessage().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Thread sendMessage() {
        return new Thread(() -> {
            System.out.println("Send thread: Start");
            boolean flag = true;
            while (flag) {
                // manipulation of flag should not be in the try catch block
                try {
                    System.out.println("Hello");

                    if (MessageRepo.getMessageCount() > 0) {
                        System.out.println(MessageRepo.getMessageCount());
                        String lastMessage = MessageRepo.getLastMessage();
                        mWriter.writeUTF(mUsername + ":" + lastMessage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    flag = false;
                }
            }
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            System.out.println("Read thread: Start");
            ChatController c = ControllerRepo.getController1();

            boolean flag = true;
            while (flag) {
                try {
                    String message = mReader.readUTF();
                    if (message.length() > 0) {
                        System.out.println(message);
                        MessageRepo.addMessage(message);
                        Platform.runLater(() -> {
                            c.receiveMessage(MessageRepo.getLastMessage());
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    flag = false;
                }
            }
            System.out.println("Read thread: outside");
        });
    }
}
