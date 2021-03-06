package main.chatapp.repositories;

import main.chatapp.controllers.ChatController;

public class ControllerInstance {
    private static ChatController chatController = null;

    public static void setChatController(ChatController c) {
        chatController = c;
    }

    public static ChatController getChatController() {
        return chatController;
    }
}
