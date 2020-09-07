package chatapp.controllers;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ClientHandler implements Runnable {
    Scanner scn = new Scanner(System.in);
    String name;
    DataInputStream reader;
    DataOutputStream writer;
    Socket socket;
    boolean isActive;

    public ClientHandler(Socket socket, String name, DataInputStream disReader, DataOutputStream dosWriter) {
        this.reader = disReader;
        this.writer = dosWriter;
        this.name = name;
        this.socket = socket;
        this.isActive = true;
    }

    public void reconnect(Socket s, DataInputStream disReader, DataOutputStream dosWriter) {
        this.reader = disReader;
        this.writer = dosWriter;
        this.socket = s;
        this.isActive = true;
    }

    private void askToSaveLog() throws FileNotFoundException {
        boolean isCorrect = false;
        String answer;
        System.out.println("Server: Both clients disconnected. Would you like to save the logs? (yes or no):");

        while (!isCorrect) {
            answer = scn.nextLine();
            if (answer.equalsIgnoreCase("yes")) {
                isCorrect = true;
                System.out.println("Server: Enter the name of the text file for the logs: ");

                answer = scn.nextLine();
                saveLogToFile(answer);

                System.out.println("Server: Logs successfully saved to " + answer + ".txt");
            } else if (answer.equalsIgnoreCase("no")) {
                isCorrect = true;
                System.out.println("Server: Logs will not be saved.");
            } else {
                System.out.println("Invalid answer, please try again... Enter yes or no: ");
            }
        }
    }

    private void saveLogToFile(String filename) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(filename + ".txt");
        String updatedText = Server.getLogsToString().replaceAll("\n", System.lineSeparator());
        out.println(updatedText);
    }

    private void logout(String sender) throws IOException {
        this.isActive = false;
        // this.s.close(); //do not close the socket

        Server.log("Server: '" + this.name + "' logged out of the server.", Server.getTimeStamp());

        ClientHandler client1 = Server.getActiveClients().stream()
                .filter(client -> !client.name.equals(sender) && client.isActive)
                .findAny()
                .orElse(null);

        if (client1 != null) {
            client1.writer.writeUTF("(" + this.name + " has disconnected from the server)");
        } else { // The other client is also not active; ask if the server wants to save the log or not.
            askToSaveLog();
        }
    }

    @Override
    public void run() {
        String received;

        while (true) {
            try {
                // receive the string from the clients
                received = reader.readUTF();

                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, ":");
                String senderName = st.nextToken();
                String messageToSend = st.nextToken();

                if (messageToSend.equals("-logout")) {
                    logout(senderName);
                    break;
                }

                // search for the sender in the connected devices list and look for the partner.
                ClientHandler client1 = Server.getActiveClients().stream()
                        .filter(client -> !client.name.equals(senderName) && client.isActive)
                        .findAny()
                        .orElse(null);

                if (client1 != null) {
                    Server.log("Server: " + this.name + " sent \"" + messageToSend + "\" to " + client1.name, Server.getTimeStamp());
                    client1.writer.writeUTF(this.name + ": " + messageToSend);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            this.reader.close();
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
