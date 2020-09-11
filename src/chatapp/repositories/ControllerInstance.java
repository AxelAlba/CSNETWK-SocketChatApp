package chatapp.repositories;

import chatapp.controllers.ChatController;

public class ControllerInstance {
    private static ChatController chatController;

    public static void setChatController(ChatController c) {
        chatController = c;
    }

    public static ChatController getChatController() {
        return chatController;
    }
}
