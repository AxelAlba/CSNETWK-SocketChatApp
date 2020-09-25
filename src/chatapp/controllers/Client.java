package chatapp.controllers;


import chatapp.repositories.ClientRepository;
import chatapp.repositories.ControllerInstance;
import chatapp.repositories.MessageRepository;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private final String mUsername;
    private DataInputStream mReader;
    private DataOutputStream mWriter;
    private final Socket mClientEndpoint;
    private Thread sendMessage, readMessage, waitThread;
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

        running = new AtomicBoolean(true);
    }

    public void initialize() {
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
        return new Thread(() -> {
            System.out.println("read Message: start");
            while (running.get()) {
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
}
