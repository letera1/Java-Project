import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ChatClient2 extends JFrame {
    private ChatInterface server;
    private String clientName = "Client 2"; // Hardcoded for Client 2
    private boolean running;
    private JTextArea chatArea;
    private JTextField inputField;

    public ChatClient2() {
        super("Chat Client - Client 2");
        this.running = true;

        // Initialize GUI components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        // Layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Set up RMI connection
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            server = (ChatInterface) registry.lookup("ChatService");
            server.registerClient(clientName);
            chatArea.append("Connected to chat server as " + clientName + "\n");
        } catch (Exception e) {
            chatArea.append("Client2 exception: " + e.toString() + "\n");
            e.printStackTrace();
        }

        // Send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Input field action (Enter key)
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Start message checker thread
        Thread messageChecker = new Thread(() -> {
            ArrayList<String> lastMessages = new ArrayList<>();
            while (running) {
                try {
                    ArrayList<String> currentMessages = server.getMessages();
                    if (!currentMessages.equals(lastMessages)) {
                        for (String msg : currentMessages) {
                            if (!lastMessages.contains(msg)) {
                                chatArea.append(msg + "\n");
                            }
                        }
                        lastMessages = new ArrayList<>(currentMessages);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    chatArea.append("Message check exception: " + e.toString() + "\n");
                }
            }
        });
        messageChecker.start();

        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            try {
                if (message.equalsIgnoreCase("exit")) {
                    server.unregisterClient(clientName);
                    running = false;
                    dispose(); // Close the window
                } else {
                    server.sendMessage(clientName, message);
                    inputField.setText("");
                }
            } catch (Exception e) {
                chatArea.append("Send message exception: " + e.toString() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClient2());
    }
}