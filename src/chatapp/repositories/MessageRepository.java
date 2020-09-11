package chatapp.repositories;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MessageRepository {
    private static volatile boolean messageAdded = false;

    private static volatile ArrayList<String> messageList = new ArrayList<>();
    private static volatile ObservableList<String> observableMessageList = FXCollections.observableList(messageList);

    public static synchronized void initialize() {
        observableMessageList.addListener((ListChangeListener) change -> messageAdded = true);
    }

    public static synchronized String getLastMessage() {
        if (messageList.size() > 0)
            return messageList.get(observableMessageList.size() - 1);
        return "";
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

    public static synchronized ObservableList<String> getMessageList() {
        return observableMessageList;
    }
}