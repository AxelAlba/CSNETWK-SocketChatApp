package chatapp.controllers;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Server implements Runnable {
    private static final ArrayList<ClientHandler> mActiveClients = new ArrayList<>();
    private static final StringBuilder logs = new StringBuilder();
    private static int mPort;
    private static Socket serverEndpoint;
    private static DataInputStream reader;
    private static DataOutputStream writer;

    private static void initialize(int port) throws IOException {
        mPort = port;
        ServerSocket serverSocket = new ServerSocket(port);
        serverEndpoint = serverSocket.accept();
        reader = new DataInputStream(serverEndpoint.getInputStream());
        writer = new DataOutputStream(serverEndpoint.getOutputStream());
    }

    public static ArrayList<ClientHandler> getActiveClients() {
        return mActiveClients;
    }

    public static String getLogsToString() {
        return logs.toString();
    }

    public static void setPort(int port) {
        mPort = port;
    }

    public static void acceptClient(String username) throws IOException {
//        Search client list for duplicates
        ClientHandler duplicate = mActiveClients.stream()
                .filter(c -> c.name.equals(username))
                .findAny()
                .orElse(null);

        if (duplicate == null) {
            System.out.println(Arrays.toString(mActiveClients.toArray()));
            ClientHandler client = new ClientHandler(serverEndpoint, username, reader, writer);

            log("Server: '" + username + "' logged in to the server.", getTimeStamp());
            mActiveClients.add(client);
            new Thread(client).start();
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
        }

        log("Server: '" + username + "' has reconnected to the server. (", getTimeStamp());

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

    @Override
    public void run() {
        try {
            initialize(mPort);
            String username = reader.readUTF();

            while (true) {
                if (mActiveClients.size() < 2) {
                    acceptClient(username);
                } else {
                    reconnectOrDenyClient(username);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log("Server: Connection terminated", getTimeStamp());
        }
    }
}
