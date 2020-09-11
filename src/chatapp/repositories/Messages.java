package chatapp.repositories;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class Messages {
    private static boolean messageAdded = false;

    private static final ArrayList<String> messageList = new ArrayList<>();
    private static final ObservableList<String> observableMessageList = FXCollections.observableList(messageList);

    public static void initialize() {
        observableMessageList.addListener((ListChangeListener) change -> {
            messageAdded = true;
            System.out.println("Message added: " + getLastMessage() + " " + messageAdded);
        });
    }

    public static String getLastMessage() {
        return messageList.get(observableMessageList.size() - 1);
    }

    public static void addMessage(String message) {
        observableMessageList.add(message);
    }

    public static void setIsChanged(boolean value) {
        messageAdded = false;
    }

    public static boolean isChanged() {
        return messageAdded;
    }

    public static int getMessageCount() {
        return observableMessageList.size();
    }

    public static ObservableList<String> getMessageList() {
        return observableMessageList;
    }
}