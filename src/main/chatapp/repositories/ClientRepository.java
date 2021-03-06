package main.chatapp.repositories;

import main.chatapp.controllers.Client;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    private static Client thisClient;
    private static volatile boolean
            isClientAdded = false,
            isClientRejected = false;
    private static ObservableList<String> observableClientList = FXCollections.observableList(new ArrayList<>());


    public static synchronized void initialize(List<String> clientList) {
        observableClientList = FXCollections.observableList(clientList);
        observableClientList.addListener((ListChangeListener) change -> isClientAdded = true);
    }

    public static synchronized void initialize() {
        observableClientList.addListener((ListChangeListener) change -> isClientAdded = true);
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

    public static synchronized void clearClients() {
        initialize(new ArrayList<>());
    }



    public static synchronized void setThisClient(Client client) {
        thisClient = client;
    }

    public static synchronized Client getThisClient() {
        return thisClient;
    }

    public static synchronized void resetThisClient() {
        thisClient = null;
    }


    public static synchronized boolean isClientRejected() {
        return isClientRejected;
    }

    public static synchronized void acceptClient() {
        isClientRejected = false;
    }

    public static synchronized void rejectClient() {
        isClientRejected = true;
    }
}
