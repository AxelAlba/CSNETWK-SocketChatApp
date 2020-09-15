import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.StringTokenizer;
  
public class MPClient  { 
    final static int ServerPort = 1234; // temporary, user should be asked.
  
    public static void main(String args[]) throws UnknownHostException, IOException { 

        Scanner scanner = new Scanner(System.in); 
          
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
                        // read the message to deliver to the server. 
                        String msg = scanner.nextLine(); 
                        
                        // NOTE: assume that the path name does not have spaces..
                        StringTokenizer st = new StringTokenizer(msg, " "); 
                        String command = st.nextToken();
                        
                        //FOR FILE SENDING
                        if (command.equals("-sendFile")){
                            String path = st.nextToken();
                            try {
                                FileInputStream fileInput = new FileInputStream(path);
                                int bytes = (int)fileInput.getChannel().size();

                                // message format: username:-sendFile:"# Of Bytes":"File Extension" 
                                dosWriter.writeUTF(username + ":" + "-sendFile:" + bytes + ":" + path.substring(path.lastIndexOf('.') + 1)); 

                                // sending of bytes to server
                                byte[] b = new byte[bytes];
                                fileInput.read(b, 0, b.length);
                                dosWriter.write(b, 0, b.length);                                
                                fileInput.close();

                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                            
                        }

                        // FOR MESSAGE SENDING
                        else{
                            try { 
                                // write on the output stream 
                                dosWriter.writeUTF(username+":"+msg); 
                            } catch (IOException e) { 
                                flag = false;
                                System.out.println("(disconnected - send message)");
                            }
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

                            // read the message sent to this client by the server
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