package networkTimeProtocol;

import java.io.*;
import java.net.*;

public class Server{
    static void prepareScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) new ProcessBuilder("cmd", "/c", "color", "0f", "&", "cls").inheritIO().start().waitFor();
            else Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }
    public static void main(String[] args) {
        // Clear screen
        prepareScreen();

        // Check if port is given
        if (args.length != 1){
            throw new IllegalArgumentException("Port number is necassary!");
        }
 
        int port = Integer.parseInt(args[0]);
        
        // First try to listen given port using Socket library.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                try {
                    // When new connection request comes, create new thread to handle connection
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected. IP: " + socket.getRemoteSocketAddress().toString().split("/")[1]);
                    new ServerThread(socket).start();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Connection Error");
                }
                
            }
 
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}


class ServerThread extends Thread {
    private Socket socket;
 
    public ServerThread(Socket socket) {
        this.socket = socket;
    }
 
    public void run() {
        // Initialize values
        String message = "";
        String clientIp = "";
        String clientPort = "";
        long t2 = 0;
        try {
            // Create input & output stream for communication between server & client.
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Read empty message from client
            message = reader.readLine();

            // Get client IP & Port
            String clientAddr = new String(socket.getRemoteSocketAddress().toString().split("/")[1]);
            
            clientIp = clientAddr.split(":")[0];
            clientPort = clientAddr.split(":")[1];


            // Send info to client
            writer.println("Connection established.");
            writer.println("Your IP is " + clientIp + " and port is " + clientPort);               
            writer.println("NTP started");

            // Send READY message to client
            writer.println("READY");

            // After READY message wait client for message
            while (true) {
                message = reader.readLine();
                t2 = System.currentTimeMillis();
                if(message.equals("bye")) break;

                // Send t2 and t3 to client
                writer.println(t2 + " " + System.currentTimeMillis());
            }

            // Send bye message to client
            writer.println("bye");
            System.out.println("Close message send to " + socket.getRemoteSocketAddress().toString().split("/")[1]);

            // Close streams & socket
            output.close();
            writer.close();
            socket.close();
        } catch (IOException ex) {
            if(message != null){
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }else{
                System.out.println(ex);
                System.out.println("Connection closed for client " + clientIp + ":" + clientPort);
            }
        }
    }
}