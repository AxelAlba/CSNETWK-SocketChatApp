package chatapp.controllers;

public class ControllerRepo {
    private static ChatController chatController;

    public static void setChatController(ChatController c) {
        chatController = c;
    }

    public static ChatController getChatController() {
        return chatController;
    }
}
