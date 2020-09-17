package chatapp.repositories;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    private static volatile boolean clientAdded = false;
    private static ObservableList<String> observableClientList = FXCollections.observableList(new ArrayList<>());


    public static synchronized void initialize(List<String> clientList) {
        observableClientList = FXCollections.observableList(clientList);
        observableClientList.addListener((ListChangeListener) change -> clientAdded = true);
    }

    public static synchronized ObservableList<String> getClientList() {
        return observableClientList;
    }

    public static synchronized void addClient(String client) {
        observableClientList.add(client);
    }

    public static synchronized boolean containsClient(String client) {
        return observableClientList.contains(client);
    }

    public static synchronized boolean isFull() {
        return observableClientList.size() == 2;
    }
}
