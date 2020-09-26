package chatapp.repositories;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MessageRepository {
    private static volatile boolean messageAdded = false;

    private static ArrayList<String> messageList;
    private static ObservableList<String> observableMessageList;

    public static synchronized void initialize() {
        messageList = new ArrayList<>();
        observableMessageList = FXCollections.observableList(messageList);
        observableMessageList.addListener((ListChangeListener) change -> messageAdded = true);
    }

    public static synchronized ObservableList<String> getMessageList() {
        return observableMessageList;
    }

    public static synchronized String getLastMessage() {
        return (messageList.size() > 0) ?
            messageList.get(messageList.size() - 1) :
            "";
    }

    public static synchronized void clearMessages() {
        initialize();
    }

    public static synchronized void addMessage(String message) {
        observableMessageList.add(message);
    }

    public static synchronized void setNoChange() {
        messageAdded = false;
    }

    public static synchronized boolean isChanged() {
        return messageAdded;
    }

    public static synchronized int getMessageCount() {
        return observableMessageList.size();
    }
}