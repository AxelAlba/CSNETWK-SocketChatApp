package chatapp.controllers;


import chatapp.Main;
import chatapp.repositories.ControllerRepo;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server implements Runnable {
    private static final ArrayList<ClientHandler> mActiveClients = new ArrayList<>();
    private static final StringBuilder logs = new StringBuilder();
    private static int mPort;

    private static Socket serverEndpoint;
    private static DataInputStream reader;
    private static DataOutputStream writer;

    private static Server mInstance = null;
    private static boolean isActive = false;

    // Singleton class to prevent multiple servers
    private Server(int port) {
        mPort = port;
        isActive = false;
    }

    public static Server getInstance(int port) {
        if (mInstance == null) {
            mInstance = new Server(port);
        }
        return mInstance;
    }

    public boolean isActive() {
        return isActive;
    }

    public void restrictSpawn() {
        isActive = true;
    }

    public static ArrayList<ClientHandler> getActiveClients() {
        return mActiveClients;
    }

    public static String getLogsToString() {
        return logs.toString();
    }

    public static void acceptClient(String username) throws IOException {
        ClientHandler duplicate = mActiveClients.stream()
                .filter(c -> c.name.equals(username))
                .findAny()
                .orElse(null);

        if (duplicate == null) {
            ClientHandler clientHandler = new ClientHandler(serverEndpoint, username, reader, writer);
            log("Server: '" + username + "' logged in to the server.", getTimeStamp());
            mActiveClients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public static void reconnectOrDenyClient(String username) throws IOException {
        boolean isPastUser = false;
        ClientHandler client1 = mActiveClients.stream()
                .filter(client -> client.name.equals(username) && !client.isActive)
                .findAny()
                .orElse(null);

        int index = mActiveClients.indexOf(client1);

        if (client1 != null) {
            ClientHandler client2 = getOtherClient(index);

            client1.reconnect(serverEndpoint, reader, writer);
            new Thread(client1).start();
            isPastUser = true;

            client1.writer.writeUTF("(Hello " + username + ", you have reconnected to the server)");
            if (client2.isActive) {
                client2.writer.writeUTF("(" + client2.name + " has reconnected to the server)");
            }
            log("Server: '" + username + "' has reconnected to the server. (", getTimeStamp());
        }


        if (isPastUser) {
            writer.writeUTF("Sorry " + username + ", the server is currently full...");
        }
    }

    public static void log(String message, String timestamp) {
        System.out.println(message + " (" + timestamp + ")\n");
        logs.append(message);
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
    }

    public static ClientHandler getOtherClient(int index) {
        return mActiveClients.get(index ^ 1);
    }

    private void viewChat(Stage stage, ArrayList<ClientHandler> clients) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("views/chat.fxml"));
        loader.load();

        ChatController controller;
        String name = "";
        if (stage.equals(Main.getSelfStage())) {
            controller = ControllerRepo.getController1();
            name = clients.get(1).name;
        } else {
            controller = ControllerRepo.getController2();
            name = clients.get(0).name;
        }

        String finalName = name;
        Platform.runLater(() -> controller.showChat(finalName));
    }

    @Override
    public void run() {
        String username = "";
        try {
            ServerSocket serverSocket = new ServerSocket(mPort);
            while (mActiveClients.size() < 2) {
                serverEndpoint = serverSocket.accept();
                reader = new DataInputStream(serverEndpoint.getInputStream());
                writer = new DataOutputStream(serverEndpoint.getOutputStream());
                username = reader.readUTF();
                acceptClient(username);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            viewChat(Main.getSelfStage(), mActiveClients);
            viewChat(Main.getOtherStage(), mActiveClients);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log("Server: Room is full", getTimeStamp());
    }
}
