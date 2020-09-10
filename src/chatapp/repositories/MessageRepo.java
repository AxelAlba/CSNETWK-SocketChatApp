package chatapp.repositories;

import java.util.ArrayList;

public class MessageRepo {
    private static final ArrayList<String> messageList = new ArrayList<>();

    public static String getLastMessage() {
        return messageList.get(messageList.size() - 1);
    }

    public static void addMessage(String message) {
        messageList.add(message);
    }

    public static int getMessageCount() {
        return messageList.size();
    }

    public static ArrayList<String> getMessageList() {
        return messageList;
    }
}
