package chatapp.controllers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
        // InetAddress ip = InetAddress.getByName("localhost"); // temporary??

        // Establish the connection
        try {
            System.out.println("Successfully connected to server at " + mClientEndpoint.getRemoteSocketAddress());
            mWriter.writeUTF(mUsername);

            String message = "temporary";
            sendMessage(message).start();
//            readMessage().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Thread sendMessage(String message) {
        return new Thread(() -> {
            boolean flag = true;
            while (flag) {
                // manipulation of flag should not be in the try catch block
                try {
                    mWriter.writeUTF(mUsername + ":" + message);
                } catch (IOException e) {
                    flag = false;
                }
            }
        });
    }

    private Thread readMessage() {
        return new Thread(() -> {
            boolean flag = true;
            while (flag) {
                try {
                    String msg = mReader.readUTF();
                    // Code for view
                } catch (IOException e) {
                    flag = false;
                }
            }
        });
    }
}
