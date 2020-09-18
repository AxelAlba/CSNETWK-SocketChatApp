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
                        String command = "";
                        StringTokenizer st = new StringTokenizer(msg, " ");
                        if (st.countTokens() >= 2)
                            command = msg.substring(0, msg.indexOf(' '));
                        
                        //FOR FILE SENDING
                        if (command.equals("-sendFile")){
                            String path = msg.substring(msg.indexOf(' ') + 1);
                            try {
                                FileInputStream fileInput = new FileInputStream(path);
                                BufferedInputStream bis = new BufferedInputStream(fileInput);
                                int bytes = (int)fileInput.getChannel().size();

                                // message format: username:-sendFile:"# Of Bytes":"File Extension" 
                                dosWriter.writeUTF(username + ":" + "-sendFile:" + bytes + ":" + path.substring(path.lastIndexOf('.') + 1)); 

                                // sending of bytes to server
                                byte[] b = new byte[bytes];
                                bis.read(b, 0, b.length);
                                dosWriter.write(b, 0, b.length);                                
                                bis.close();

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
                            StringTokenizer st = new StringTokenizer(msg, ":");
                            String username = st.nextToken();
                            String command;
                            if (st.countTokens() >= 1)
                                command = st.nextToken();
                            else 
                                command = "";

                            // FOR FILE RECEIVING
                            if (command.equals("-sendFile")){
                                //get the bytes from the server
                                int bytes = Integer.parseInt(st.nextToken());
                                String extension = st.nextToken();
                                byte[] b = new byte[bytes];

                                System.out.println("(Server: '"+username+"' is sending you a "+extension+" file)"); 
                                //String fileName = scanner.nextLine(); //can't handle two reads (thread issue)

                                // creation of file
                                FileOutputStream fr = new FileOutputStream("received"+"."+extension);
                                BufferedOutputStream bos = new BufferedOutputStream(fr);
                                // copy bytes to bytes array
                                disReader.read(b, 0, b.length);

                                // write bytes to file
                                bos.write(b, 0, b.length);
                                bos.close();

                                System.out.println("(Server: "+"received"+"."+extension+" downloaded)");           
                            }

                            // FOR DISCONNECTION
                            else if (command.equals("-disconnect")){
                                System.out.println("("+username+" has disconnected from the server)");
                            }

                            // FOR OTHER USER RECONNECTION
                            else if (command.equals("-reconnect")){
                                System.out.println("("+username+" has reconnected to the server)");  
                            }

                            // FOR OWN RECONNECTION
                            else if (command.equals("-ownReconnect")){
                                System.out.println("(Hello "+username+", you have reconnected to the server)");  
                            }

                            // FOR MESSAGE SEND FAILED
                            else if (command.equals("-messageFailed")){
                                System.out.println("(Your message to "+username+" has failed to send)");  
                            }

                            // FOR FILE SEND FAILED
                            else if (command.equals("-fileFailed")){
                                System.out.println("(Your "+st.nextToken()+" file to "+username+" has failed to send)");  
                            }    
                            
                            // FOR "FULL CLIENTS" NOTIFICATION
                            else if (command.equals("-full")){
                                System.out.println("(Sorry "+username+", the server is already full...)");  
                            }    

                            // FOR MESSAGE RECEIVING
                            else System.out.println(msg); 
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