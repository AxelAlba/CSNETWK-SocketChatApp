import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;  


public class MPServer {

    // Vector to store active clients
    static Vector<ClientHandler> activeClients = new Vector<>();
    // counter for the clients
    final static int ServerPort = 8000;
    // to store the logs of the clients
    static String logs;

    public static void main(String[] args) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
        System.out.println("Server: Listening on port " + ServerPort + "... (" +timeStamp+")");
        logs = "";
        logs += "Server: Listening on port " + ServerPort + "... (" +timeStamp+")\n";

        //For the Client hander
		ServerSocket serverSocket;
        Socket serverEndpoint;
        DataInputStream disReader;
        DataOutputStream dosWriter;
        String username;
        ClientHandler handler;
        Thread t;

        try {
            // initializing the port number of the serverSocket
            serverSocket = new ServerSocket(ServerPort);

            while(true) {
           
                // accepting the incoming request
                if(activeClients.size() < 2) {
                    System.out.println("activeclients < 2");

                    serverEndpoint = serverSocket.accept();

                    disReader = new DataInputStream(serverEndpoint.getInputStream());
                    dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());

                    // to check the username sent by the client
                    username = disReader.readUTF(); 
                  
                    // for uniqueness of username
                    boolean isAccepted = false;

                    if (activeClients.size() == 1) {
                        if (MPServer.activeClients.get(0).name.equals(username)) {
                            System.out.println("First time: Same client");
                            while (!isAccepted) {
                                System.out.println("First time: Inside while loop");
        
                                dosWriter.writeUTF("-rejectUsername");

                                serverEndpoint = serverSocket.accept();
                                disReader = new DataInputStream(serverEndpoint.getInputStream());
                                dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());

                                username = disReader.readUTF();
                                System.out.println("First time: Reading " + username);

                                
                                if (!MPServer.activeClients.get(0).name.equals(username)){
                                    System.out.println("First time: Accepting inside loop");
                                    isAccepted = true;
                                    dosWriter.writeUTF("-acceptUsername");
                                }
                            }
                        }
                        else {
                            System.out.println("First time: Accepting unique client");
                            isAccepted = true;
                            dosWriter.writeUTF("-acceptUsername");
                            System.out.println("First time: After send -acceptUsername");
                        }
                    }
                    else {
                        System.out.println("First time: Accepting because > 1");
                        dosWriter.writeUTF("-acceptUsername");                  
                        System.out.println("First time: After send -acceptUsername");
                    }

                    // To align to client side's checking for -full or -reject
                    dosWriter.writeUTF("-enter"); 

                    // creating the handler
                    handler = new ClientHandler(serverEndpoint, username, disReader, dosWriter); 
                    t = new Thread(handler); 

                    timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                    System.out.println("Server: '"+username+"' logged in to the server. ("+timeStamp+")"); 
                    logs += "Server: '"+username+"' logged in to the server. ("+timeStamp+")\n";

                    //adding the client to the activeClients vector of the server
                    activeClients.add(handler); 
                    t.start();    

                    if (activeClients.size() == 2) {
                      // Sends the client list to clients
                      for (ClientHandler client : activeClients) {
                        for (ClientHandler c : activeClients) {
                          client.dosWriter.writeUTF(c.name);
                        }
                      }
                    }
                }  



                // For reconnecting or denying new clients
                else { 
                    serverEndpoint = serverSocket.accept();
                    disReader = new DataInputStream(serverEndpoint.getInputStream());
                    dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
                    
                    // to check the username sent by the client
                    username = disReader.readUTF();
                    System.out.println("\n\nReconnecting Username: " + username);

                    System.out.println("activeClients.size(): " + activeClients.size());

                    System.out.println("Server active clients:");
                    System.out.println(MPServer.activeClients.get(0).name);
                    System.out.println(MPServer.activeClients.get(1).name);

                    // for uniqueness of username
                    boolean isAccepted = false;
                    boolean isPresent = 
                        MPServer.activeClients.get(0).name.equals(username) || 
                        MPServer.activeClients.get(1).name.equals(username);
                    

                    if (!isPresent) { // Foreign clients
                        System.out.println("Foreign client");
                        while (!isAccepted) { 
                            dosWriter.writeUTF("-full");
                            
                            // // **************** TEMP?
                            serverEndpoint = serverSocket.accept();
                            disReader = new DataInputStream(serverEndpoint.getInputStream());
                            dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
                            // // **************** TEMP?

                            username = disReader.readUTF(); // Keep checking until username belongs to client list
                            System.out.println(username);
                            if (isPresent) {
                              isAccepted = true;
                            }
                        }
                    } else { // Local clients
                        System.out.println("Reconnecting: accepting username");
                        dosWriter.writeUTF("-acceptUsername"); 
                        System.out.println("Reconnecting: after write accept");
                    }


                    // RECONNECTION
                    boolean isPastUser = false;
                    for (ClientHandler client : MPServer.activeClients)  { 
                        System.out.println("Reconnecting: for loop condition - " + 
                              (client.name.equals(username) && client.isActive == false));


                        if (client.name.equals(username) && client.isActive == false) {   

                            // To align to client side's checking for -full or -enter
                            System.out.println("Reconnecting: Not full, can enter");
                            dosWriter.writeUTF("-enter");

                            //client reconnects here                          
                            client.reconnect(serverEndpoint, disReader, dosWriter);
                            
                            // Send the client list to the returning client
                            for (ClientHandler c : activeClients) {
                                client.dosWriter.writeUTF(c.name);
                            }

                            t = new Thread(client);
                            t.start();
                            isPastUser = true;

                            client.dosWriter.writeUTF(client.name+":"+"-ownReconnect");
                            
                            //notify the other client
                            for (ClientHandler client2 : MPServer.activeClients)  
                            { 
                                if (!(client2.name.equals(username))) { 
                                    if(client2.isActive == true){
                                        client2.dosWriter.writeUTF(client.name+":"+"-reconnect"); 
                                        break;
                                    }
                                } 
                            }
                            timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                            System.out.println("Server: '"+username+"' has reconnected to the server. ("+timeStamp+")"); 
                            logs += "Server: '"+username+"' has reconnected to the server. ("+timeStamp+")\n";
                            break; 
                        } 
                    } 

                    if (isPastUser == false) {
                      System.out.println("Reconnecting: -full skipped for loop");
                      dosWriter.writeUTF(username+":-full");
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
            System.out.println("Server: Connection terminated ("+timeStamp+")");
            //saving to logs
            logs += "Server: Connection terminated ("+timeStamp+")\n";
		}

    }
}

class ClientHandler implements Runnable
{
    // Attributes of the ClientHandler
    Scanner scn = new Scanner(System.in);
    String name;
    DataInputStream disReader;
    DataOutputStream dosWriter;
    Socket s;
    boolean isActive;

    // constructor
    public ClientHandler(Socket s, String name, DataInputStream disReader, DataOutputStream dosWriter) {
        this.disReader = disReader; 
        this.dosWriter = dosWriter; 
        this.name = name; 
        this.s = s; 
        this.isActive = true;
    }

    public void reconnect(Socket s, DataInputStream disReader, DataOutputStream dosWriter){
        System.out.println("Reconnected");
        this.disReader = disReader; 
        this.dosWriter = dosWriter;
        this.s = s; 
        this.isActive = true;
    }

    @Override
    public void run() { 
  
        String received; 
        String timeStamp;
        while (true)  { 
            try{ 
                // receive the string from the clients
                received = disReader.readUTF(); 
                System.out.println("From Server: " + received);

                // break the string into message and recipient part 
                StringTokenizer st = new StringTokenizer(received, ":");
                String sender = st.nextToken(); 
                String MsgToSend;
                if (st.countTokens() >= 1)
                    MsgToSend = st.nextToken(); 
                else 
                    MsgToSend = "";

                // FOR DISCONNECTION
                if(MsgToSend.equals("-logout")){ 
                    this.isActive = false; 

                    timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                    System.out.println("Server: '"+this.name+"' logged out of the server. ("+timeStamp+")"); 
                    MPServer.logs += "Server: '"+this.name+"' logged out of the server. ("+timeStamp+")\n";

                    // for notifying the other client for the logout
                    for (ClientHandler client : MPServer.activeClients)  { 
                        if (!(client.name.equals(sender))) { 
                            if(client.isActive == true){ 
                                client.dosWriter.writeUTF(this.name+":"+"-disconnect"); 
                                break;
                            }

                            // the other client is also not active, ask if the server wants to save the log or not before server disconnection.
                            else { 
                                boolean isCorrect = false;
                                String answer;

                                System.out.println("Server: Both clients disconnected. Would you like to save the logs? (yes or no):");
                                while (isCorrect == false){
                                    answer = scn.nextLine();
                                    if (answer.equalsIgnoreCase("yes")){
                                        isCorrect = true;
                                        System.out.println("Server: Enter the name of the text file for the logs: ");
                                        answer = scn.nextLine();

                                        //save to file
                                        try (PrintWriter out = new PrintWriter(answer+".txt")) {
                                            String updatedText = MPServer.logs.replaceAll("\n", System.lineSeparator());
                                            out.println(updatedText);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        System.out.println("Server: Logs successfully saved to "+answer+".txt");
                                    }
                                    else if (answer.equalsIgnoreCase("no")){
                                        isCorrect = true;
                                        System.out.println("Server: Logs will not be saved.");    
                                    }
                                    else {
                                        System.out.println("Invalid answer, please try again... Enter yes or no: ");
                                    }
                                }
                            }
                        } 
                    } 
                    break; 
                }

                // FOR FILE SENDING
                else if(MsgToSend.equals("-sendFile")){
                    for (ClientHandler client : MPServer.activeClients){ 
                        // finds the other user and sent to its inputStream
                        if (!(client.name.equals(sender))){ 

                            // send file succeed
                            if(client.isActive == true){
                                // Get the rest of the message here (tokens) and send to other client
                                // format message:
                                int bytes = Integer.parseInt(st.nextToken());
                                String extension = st.nextToken();

                                //Sending the trigger message to the client 
                                client.dosWriter.writeUTF(this.name +":-sendFile:"+bytes+":"+extension);


                                // Simultaneously get and send the chunks to the other client

                                    // get a copy of the file for the server
                                    File file = new File("serverFile."+extension);
                                    FileOutputStream out = new  FileOutputStream(file);
                                    byte[] b = new byte[1500];
                                    int bytesRead = 0;
                                    int totalBytes = 0;
                                    while (totalBytes < bytes)
                                    {
                                        bytesRead = disReader.read(b);
                                        out.write(b, 0, bytesRead);
                                        totalBytes += bytesRead;
                                    }
                                    out.close();

                                        // send the server file to other client
                                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                                    bytesRead = 0;
                                    while ((bytesRead = bis.read(b)) > 0){
                                        client.dosWriter.write(b, 0, bytesRead);
                                    }
                                    bis.close();

                                timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                                System.out.println("Server: "+ this.name +" sent a "+ extension +" file to " + client.name + " ("+timeStamp+")");
                                MPServer.logs += "Server: "+ this.name +" sent a "+ extension +" file to " + client.name + " ("+timeStamp+")\n";
                                break;
                            }

                            // send file failed
                            else {
                                int bytes = Integer.parseInt(st.nextToken());
                                String extension = st.nextToken();
                                                     
                                // BUG SOLUTION: Get the file and store it in the server so that everything stays in the server.
                                File file = new File("serverFile."+extension);
                                FileOutputStream out = new  FileOutputStream(file);
                                byte[] b = new byte[1500];
                                int bytesRead = 0;
                                int totalBytes = 0;
                                while (totalBytes < bytes)
                                {
                                    bytesRead = disReader.read(b);
                                    out.write(b, 0, bytesRead);
                                    totalBytes += bytesRead;
                                }
                                out.close();

                                this.dosWriter.writeUTF(client.name+":-fileFailed:"+extension);
                                timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                                System.out.println("Server: "+ this.name +" failed to send a "+ extension +" file to " + client.name + " ("+timeStamp+")");
                                MPServer.logs += "Server: "+ this.name +" failed to send a "+ extension +" file to " + client.name + " ("+timeStamp+")\n";
                                break;
                            }
                        } 
                    }
                }

                // FOR MESSAGE SENDING 
                else {
                    for (ClientHandler client : MPServer.activeClients){ 
                        // finds the other user and sent to its inputStream
                        if (!(client.name.equals(sender))){ 
                            String message = received.substring(received.indexOf(':') + 1);
                            // send message succeed
                            if(client.isActive == true){
                                client.dosWriter.writeUTF(this.name +": "+ message); 

                                timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                                System.out.println("Server: "+this.name+" sent \""+message+"\" to " + client.name + " ("+timeStamp+")");
                                MPServer.logs += "Server: "+this.name+" sent \""+message+"\" to " + client.name + " ("+timeStamp+")\n";
                                break;
                            }

                            // send message failed
                            else {
                                this.dosWriter.writeUTF(client.name+":-messageFailed");

                                timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                                System.out.println("Server: "+ this.name +" failed to send \""+message+"\" to " + client.name + " ("+timeStamp+")");
                                MPServer.logs += "Server: "+ this.name +" failed to send \""+message+"\" to " + client.name + " ("+timeStamp+")\n";
                                break;
                            }
                        } 
                    }
                }
            } catch (IOException e){ 
                e.printStackTrace(); 
            } 
              
        } 
        try{ 
            // closing the disReader and dosWriter of a certain Client.
            this.disReader.close(); 
            this.dosWriter.close();     
        } catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
}