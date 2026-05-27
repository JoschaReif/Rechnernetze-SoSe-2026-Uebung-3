import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCP_Chat_Server {

    //Liste aller aktiven Client-Ausgabestreams für das Broadcasting
    private static final List<PrintWriter> clientWriters = new CopyOnWriteArrayList<>();

    private static void fatal ( String comment ) {
        System.out.println(comment);
        System.exit(-1);
    }

    // ************************************************************************
    // MAIN
    // ************************************************************************
    public static void main(String[] args) throws IOException {
        if (args.length != 2)
            fatal("Usage: \"<netcat> -l <port>\" or \"netcat <ip> <port>\"");
        int port = Integer.parseInt(args[1]);
        if (args[0].equalsIgnoreCase("-l"))
            Server(port);
        else
            Client(args[0],port);
    }

    // ************************************************************************
    // Server
    // ************************************************************************
    private static void Server ( int port ) throws IOException {
        ServerSocket s = new ServerSocket(port);
        System.out.println("Server gestartet auf Port " + port);
        while (true) {
            Socket client = s.accept();
            Thread t = new Thread(() -> serveClient(client));
            t.start();
        }
    }

    private static void serveClient ( Socket clientConnection ) {
        PrintWriter output = null;
        try {
            output = new PrintWriter(clientConnection.getOutputStream(), true);
            //neuen Client in die globale Liste eintragen
            clientWriters.add(output);

            BufferedReader r = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            String line;

            //Info an den Server-Log, dass jemand da ist
            System.out.println("[Server Info] Ein neuer Client hat sich verbunden.");

            while ((line = r.readLine()) != null) {
                if (line.equalsIgnoreCase("stop")) {
                    break;
                }

                //System-Log auf dem Server
                System.out.println("Empfangen: " + line);

                //NACHRICHT AN ALLE ANDEREN CLIENTS WEITERLEITEN
                broadcast(line, output);
            }
        }
        catch (IOException e) {
            System.out.println("Verbindung zu einem Client verloren.");
        }
        finally {
            //Client aus der Liste entfernen, wenn er geht
            if (output != null) {
                clientWriters.remove(output);
            }
            try {
                clientConnection.close();
            } catch (IOException e) {
                // Ignore
            }
            System.out.println("[Server Info] Client hat die Verbindung getrennt.");
        }
    }

    //Hilfsmethode, um die Nachricht an alle Clients (außer den Absender) zu schicken
    private static void broadcast(String message, PrintWriter sender) {
        for (PrintWriter writer : clientWriters) {
            if (writer != sender) {
                writer.println(message);
            }
        }
    }

    // ************************************************************************
    // Client
    // ************************************************************************
    private static void Client ( String serverHost, int serverPort ) throws IOException {

        InetAddress serverAddress = InetAddress.getByName(serverHost);
        Socket serverConnect = new Socket(serverAddress,serverPort);

        Thread receiveThread = new Thread(() -> {
            try {
                BufferedReader readMessage = new BufferedReader(new InputStreamReader(serverConnect.getInputStream()));
                String lineIn;
                while ((lineIn = readMessage.readLine()) != null) {
                    System.out.println("\n[Partner]: " + lineIn);
                    System.out.print("Input: "); // Prompt wieder anzeigen
                }
            } catch (IOException e) {
                System.out.println("Verbindung zum Server verloren.");
            }
        });
        receiveThread.start();

        PrintWriter w = new PrintWriter(serverConnect.getOutputStream(),true);
        String line;
        do {
            System.out.print("Input: ");
            line = readString();
            if (line != null) {
                w.println(line);
            }
        } while (line != null && !line.equalsIgnoreCase("stop"));

        serverConnect.close();
    }

    private static String readString () {
        boolean again = false;
        String input = null;
        do {
            try {
                if (br == null)
                    br = new BufferedReader(new InputStreamReader(System.in));
                input = br.readLine();
            }
            catch (Exception e) {
                System.out.printf("Exception: %s\n",e.getMessage());
                again = true;
            }
        } while (again);
        return input;
    }

    private static BufferedReader br = null;
}