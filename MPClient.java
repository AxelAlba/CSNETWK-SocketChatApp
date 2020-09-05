import java.io.*; 
import java.net.*; 
import java.util.Scanner; 
  
public class MPClient  { 
    final static int ServerPort = 1234; // temporary, user should be asked.
  
    public static void main(String args[]) throws UnknownHostException, IOException { 
        Scanner scanner = new Scanner(System.in); 
        // getting localhost ip 
        //InetAddress ip = InetAddress.getByName("localhost"); // temporary??
          
        // establish the connection 
        try{
            System.out.println("Please Login...");
            System.out.println("Enter your username: ");
            String username = scanner.nextLine(); 

            // ask the ip and port here
            Socket clientEndpoint = new Socket("localhost", ServerPort); 

            // obtaining input and out streams 
            DataInputStream disReader = new DataInputStream(clientEndpoint.getInputStream()); 
            DataOutputStream dosWriter = new DataOutputStream(clientEndpoint.getOutputStream()); 

            System.out.println(": Successfully connected to server at " + clientEndpoint.getRemoteSocketAddress());
                
            // send the username to the server
            dosWriter.writeUTF(username);

            // sendMessage thread 
            Thread sendMessage = new Thread(new Runnable()  { 
                @Override
                public void run() { 
                    boolean flag = true;
                        while (flag) { 
                        // manipulation of flag should not be in the try catch block
                        // read the message to deliver. 
                        String msg = scanner.nextLine(); 
                        
                        try { 
                            // write on the output stream 
                            // username of this client should be included on the message sent so the server could parse....
                            dosWriter.writeUTF(username+":"+msg); 
                        } catch (IOException e) { 
                            flag = false;
                            System.out.println("(disconnected - send message)");
                        }
                    } 
                } 
            }); 
                
            // readMessage thread 
            Thread readMessage = new Thread(new Runnable()  { 
                @Override
                public void run() { 
                    boolean flag = true;
                    while (flag) { 
                        try { 
                            // read the message sent to this client 
                            String msg = disReader.readUTF(); 
                            System.out.println(msg); 
                        } catch (IOException e) { 
                            flag = false; 
                            System.out.println("(disconnected - read message)");
                        } 
                    } 
                } 
            }); 
        
            sendMessage.start(); 
            readMessage.start(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
} 