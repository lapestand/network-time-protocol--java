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
        prepareScreen();
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);
 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Server is listening on port " + port);
 
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected. IP: " + socket.getRemoteSocketAddress().toString());
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
        String message = "";
        String clientIp = "";
        String clientPort = "";
        long t2 = 0;
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            message = reader.readLine();
            
            // currentTime = System.currentTimeMillis();
            
            clientIp = socket.getRemoteSocketAddress().toString().split(":")[0];
            clientPort = socket.getRemoteSocketAddress().toString().split(":")[1];

            writer.println("Connection established.");
            writer.println("Your IP is " + clientIp + " and port is " + clientPort);               
            writer.println("NTP started");

            writer.println("READY");
            while (true) {
                message = reader.readLine();
                t2 = System.currentTimeMillis();
                if(message == "bye") break;
                writer.println(t2 + " " + System.currentTimeMillis());
            }
            writer.println("bye");
            System.out.println("Close message send to " + socket.getRemoteSocketAddress().toString());
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