package chatapp.repositories;

import chatapp.controllers.Server;

public class ThreadRepo {
    private static Thread acceptClientThread;
    private static Thread reconnectClientThread;
    private static Thread sendMessageThread;
    private static Thread readMessageThread;


    public static Thread getAcceptClientThread() {
        return acceptClientThread;
    }

    public static void startAcceptClientThread(int port) {
        Server server = Server.getInstance(port);
        if (!server.isActive()) {
            server.restrictSpawn();
            acceptClientThread = new Thread(server);
            acceptClientThread.start();
        }
    }

}
