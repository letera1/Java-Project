import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ChatServer extends UnicastRemoteObject implements ChatInterface {
    private ArrayList<String> messages;
    private ArrayList<String> clients;

    public ChatServer() throws RemoteException {
        messages = new ArrayList<>();
        clients = new ArrayList<>();
    }

    @Override
    public void sendMessage(String name, String message) throws RemoteException {
        String formattedMessage = name + ": " + message;
        messages.add(formattedMessage);
        System.out.println("Received: " + formattedMessage);
    }

    @Override
    public ArrayList<String> getMessages() throws RemoteException {
        return messages;
    }

    @Override
    public void registerClient(String name) throws RemoteException {
        clients.add(name);
        messages.add("System: " + name + " has joined the chat");
        System.out.println(name + " registered");
    }

    @Override
    public void unregisterClient(String name) throws RemoteException {
        clients.remove(name);
        messages.add("System: " + name + " has left the chat");
        System.out.println(name + " unregistered");
    }

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ChatService", server);
            System.out.println("Chat Server is Running on Port 1099......");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
