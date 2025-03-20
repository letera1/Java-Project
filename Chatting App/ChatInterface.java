import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ChatInterface extends Remote {
    void sendMessage(String name, String message) throws RemoteException;
    ArrayList<String> getMessages() throws RemoteException;
    void registerClient(String name) throws RemoteException;
    void unregisterClient(String name) throws RemoteException;
}