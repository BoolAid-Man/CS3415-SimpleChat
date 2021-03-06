package simplechat;// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import ocsf.server.ConnectionToClient;
import ocsf.server.ObservableServer;
import simplechat.common.ChatIF;

import java.io.IOException;
import java.util.Locale;

/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends ObservableServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    public static ChatIF serverUI;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {
        super(port);
    }

    public EchoServer(int port, ChatIF ui) {
        super(port);
        serverUI = ui;
    }

    /**
     * This method is responsible for the creation of
     * the server instance (there is no UI in this phase).
     *
     * @param args [0] The port number to listen on.  Defaults to 5555
     *             if no argument is entered.
     */
    public static void main(String[] args) {
        int port = 0; //Port to listen on

        try {
            port = Integer.parseInt(args[0]); //Get port from command line
        } catch (Throwable t) {
            port = DEFAULT_PORT; //Set port to 5555
        }

        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            serverUI.display("ERROR - Could not listen for clients!");
        }
    }

    //Instance methods ************************************************
    public void handleMessageFromServerUI(String msg) {


        if (msg.charAt(0) == '#') {
            String[] command = msg.toLowerCase().replaceFirst("#", "").split(" ");

            switch (command[0]) {
                case "quit":
                    try {
                        close();
                    } catch (IOException e) {
                        serverUI.display("An error occurred while closing server.");
                    }
                    serverUI.display("Clients disconnected and server no longer listening.");
                    serverUI.display("Terminating server.");
                    System.exit(0);
                    break;
                case "stop":
                    stopListening();
                    break;
                case "close":
                    try {
                        close();
                    } catch (IOException e) {
                        serverUI.display("An error occurred while closing server.");
                    }
                    serverUI.display("Clients disconnected and server no longer listening.");
                    break;
                case "setport":
                    try {
                        setPort(Integer.parseInt(command[1]));
                        serverUI.display("Port set to " + getPort());
                    } catch (NumberFormatException e) {
                        serverUI.display("Port must be a number.");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        serverUI.display("Enter the command followed by a port number");
                    }
                    break;
                case "start":
                    try {
                        listen();
                    } catch (Exception e) {
                        serverUI.display("Couldn't listen for new clients.");
                    }
                    break;
                case "getport":
                    serverUI.display("Port: " + getPort());
                    break;
                default:
                    serverUI.display(String.format(
                            "The command qualifier was used without a valid command.%n" +
                                    "here's a list of valid commands:%n" +
                                    "#quit stops the server and terminates the client.%n" +
                                    "#stop stops the server listening for new clients.%n" +
                                    "#close closes server socket and disconnects all clients.%n" +
                                    "#start starts the server listening for new clients.%n" +
                                    "#setport <port> sets the port number.%n" +
                                    "#getport displays the current port number.%n"));
            }
        } else {
            this.sendToAllClients("SERVER MSG> " + msg);
            serverUI.display("SERVER MSG> " + msg);
        }
    }

    @Override
    public void clientConnected(ConnectionToClient client) {
        super.clientConnected(client);
        serverUI.display("Client connected from " + client);
        client.setInfo("hasSentMessage", false);
        client.setInfo("loginID", "");
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        super.clientDisconnected(client);
        serverUI.display(String.format("client %s disconnected from IP: %s", client.getInfo("loginID"), client));
    }

    /**
     * This method handles any messages received from the ocsf.client.
     *
     * @param msg    The message received from the ocsf.client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client) {
        serverUI.display("Message received: " + msg + " from " + client);

        //check for login command
        if (msg.toString().split(" ")[0].equalsIgnoreCase("#LOGIN")) {
            if (client.getInfo("loginID").equals("") && client.getInfo("hasSentMessage").equals(false)) {
                client.setInfo("loginID", msg.toString().split(" ")[1]);

            } else if (!client.getInfo("loginID").equals("")) {
                try {
                    client.sendToClient("Login ID already set");
                } catch (IOException e) {
                    serverUI.display("Couldn't send message to client");
                }
            } else if (client.getInfo("hasSentMessage").equals(true)){
                try {
                    client.sendToClient("#LOGIN command must be the first message you send");
                    client.close();
                } catch (IOException e) {
                    serverUI.display("Couldn't send message to client");
                }
            }

        } else {
            if(client.getInfo("loginID").equals("")){
                try {
                    serverUI.display(client + "sent a message without loggin in. Kicking from server");
                    client.sendToClient("Log in before sending messages");
                    client.close();
                } catch (IOException e){
                    serverUI.display(e.getMessage());
                }
            }
            this.sendToAllClients(String.format("%s> %s", client.getInfo("loginID"), msg));
        }

        client.setInfo("hasSentMessage", true);
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        super.serverStarted();
        serverUI.display
                ("Server listening for connections on port " + getPort());
    }

    //Class methods ***************************************************

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped() {
        super.serverStopped();
        serverUI.display
                ("Server has stopped listening for connections.");
    }
}
//End of EchoServer class
