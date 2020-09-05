import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;  


public class MPServer {

    // Vector to store active clients
    static Vector<ClientHandler> activeClients = new Vector<>();

    //counter for the clients
    final static int ServerPort = 1234;
    static String logs;
    public static void main(String[] args) {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
        System.out.println("Server: Listening on port " + ServerPort + "... (" +timeStamp+")");
        
        //saving to logs
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

            while(true) 
            {
                // accepting the incoming request
                if(activeClients.size() < 2) {
                    serverEndpoint = serverSocket.accept();

                    disReader = new DataInputStream(serverEndpoint.getInputStream());
                    dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
                    // to check the username sent by the client
                    username = disReader.readUTF();    
                    handler = new ClientHandler(serverEndpoint, username, disReader, dosWriter); 
                    t = new Thread(handler); 
                    timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                    System.out.println("Server: '"+username+"' logged in to the server. ("+timeStamp+")"); 
                    //saving to logs
                    logs += "Server: '"+username+"' logged in to the server. ("+timeStamp+")\n";

                    activeClients.add(handler); 
                    t.start();    
                }  

                else { //for reconnecting or denying new clients
                    serverEndpoint = serverSocket.accept();
                    disReader = new DataInputStream(serverEndpoint.getInputStream());
                    dosWriter = new DataOutputStream(serverEndpoint.getOutputStream());
                    
                    // to check the username sent by the client
                    username = disReader.readUTF();
                    //check if he/she is a past user with the username and if the isActive is false (make it true)... (for reconnecting) THIS IS IT

                    // to check if the username already exists
                    boolean isPastUser = false;
                    for (ClientHandler client : MPServer.activeClients)  
                    { 
                        if (client.name.equals(username) && client.isActive == false) {                             
                            client.reconnect(serverEndpoint, disReader, dosWriter);
                            t = new Thread(client);
                             //start the thread again
                            t.start();
                            isPastUser = true;

                            client.dosWriter.writeUTF("(Hello "+username+", you have reconnected to the server)");
                            // if reassigning to a new thread does not work, delete the client from the active clients and create new nalang
                            
                            //notify the other client
                            for (ClientHandler client2 : MPServer.activeClients)  
                            { 
                                if (!(client2.name.equals(username))) { 
                                    if(client2.isActive == true){
                                        client2.dosWriter.writeUTF("("+client.name+" has reconnected to the server)"); // he/she is connected to the server
                                        break;
                                    }
                                } 
                            }
                            timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());
                            System.out.println("Server: '"+username+"' has reconnected to the server. ("+timeStamp+")"); 
                            //saving to logs
                            logs += "Server: '"+username+"' has reconnected to the server. ("+timeStamp+")\n";

                            break; 
                        } 
                    } 
                    if(isPastUser == false) {
                        dosWriter.writeUTF("Sorry "+username+", the server is currently full...");
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
        this.disReader = disReader; 
        this.dosWriter = dosWriter;
        this.s = s; 
        this.isActive = true;
    }

    @Override
    public void run() { 
  
        String received; 
        String timeStamp;
        while (true)  
        { 
            try
            { 
                // receive the string from the clients
                received = disReader.readUTF(); 

                // break the string into message and recipient part 
                StringTokenizer st = new StringTokenizer(received, ":"); 
                String sender = st.nextToken(); 
                String MsgToSend = st.nextToken(); 
  
                if(MsgToSend.equals("-logout")){ 
                    this.isActive = false; 
                    //this.s.close(); //do not close the socket

                    timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());

                    System.out.println("Server: '"+this.name+"' logged out of the server. ("+timeStamp+")"); 
                    //saving to logs
                    MPServer.logs += "Server: '"+this.name+"' logged out of the server. ("+timeStamp+")\n";
                    // for notifying the other client for the logout
                    for (ClientHandler client : MPServer.activeClients)  
                    { 
                        if (!(client.name.equals(sender))) { 
                            if(client.isActive == true){
                                client.dosWriter.writeUTF("("+this.name+" has disconnected from the server)"); 
                                break;
                            }
                            else { // the other client is also not active, ask if the server wants to save the log or not.
                                boolean isCorrect = false;
                                String answer;
                                System.out.println("Server: Both clients disconnected. Would you like to save the logs? (yes or no):");
                                while (isCorrect == false)
                                {
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


                    // ask the client if he or she would like to reconnect then change the isActive variable ("afk" style)
                    // - ask the username or simple yes or no only
                    break; 
                }

                // search for the sender in the connected devices list and look for the partner.  
                for (ClientHandler client : MPServer.activeClients)  
                { 
                    // if the recipient is found, write on its 
                    // output stream 
                    if (!(client.name.equals(sender))) 
                    { 
                        if(client.isActive == true)
                        {
                            timeStamp = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss").format(new Date());

                            System.out.println("Server: "+this.name+" sent \""+MsgToSend+"\" to " + client.name + " ("+timeStamp+")");
                            //saving to logs
                            MPServer.logs += "Server: "+this.name+" sent \""+MsgToSend+"\" to " + client.name + " ("+timeStamp+")\n";

                            client.dosWriter.writeUTF(this.name +": "+ MsgToSend); 
                            break;
                        }
                        // else if not active tell the sender that the message is not going to be sent
                    } 
                } 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
              
        } 
        try
        { 
            // closing the disReader and dosWriter of a certain Client.
            this.disReader.close(); 
            this.dosWriter.close();     
        } catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
}