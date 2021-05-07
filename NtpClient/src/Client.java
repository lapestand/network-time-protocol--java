import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Client {

    static void prepareScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) new ProcessBuilder("cmd", "/c", "color", "0f", "&", "cls").inheritIO().start().waitFor();
            else Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }

    static void checkArguments(String[] args){
        if(args.length > 2){
            throw new ArithmeticException("Too much argument!");
        }
        
        int test = Integer.parseInt(args[0]);
        if (test < 0){
            throw new ArithmeticException("Offset can't be smaller then zero(0)!");
        }

        Pattern ipPattern = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
            + "|" + "localhost" // localhost
            + "|" + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip

            + ":" + "[0-9]{1,5}$" // Port
        );

        if(!ipPattern.matcher(args[1]).matches()){
            throw new IllegalArgumentException("Invalid Ip address | port");
        }else{
            int portNumber = Integer.parseInt(args[1].split(":")[1]);
            if(portNumber < 1 || portNumber > 65535){
                throw new IllegalArgumentException("Port number must between 1 - 65535");
            }
        }
        
    }
    public static void main(String[] args) throws Exception {
        prepareScreen();
        System.out.println("Client Started!");
        int offsetValue = 0, serverPort = 0;
        String serverHost = "";
        try {
            // Check Input has valid syntax or not
            checkArguments(args);

            offsetValue = Integer.parseInt(args[0]);
            serverHost = args[1].split(":")[0];
            serverPort = Integer.parseInt(args[1].split(":")[1]);
        } catch (Exception e) {
            System.out.println(e + "\n\n\n");
            System.out.println("Syntax to run client: java -cp . networkTimeProtocol.Client offset(int) (ip_address|domain_name|localhost):port(int)");
            System.exit(-1);
        }

        System.out.println("\n\n\nOffset value ->\t" + offsetValue);
        System.out.println("Connecting to " + serverHost + ":" + serverPort + "\n\n\n");
        
        // Send data to the server using an OutputStream.
        try (Socket clientSocket = new Socket( serverHost, serverPort )){

            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String serverResponse;
            do {
                writer.println(offsetValue);
                serverResponse = reader.readLine();
                System.out.println("Server: " + serverResponse);
 
            } while (!serverResponse.equals("bye"));

           
            clientSocket.close();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            System.exit(-1);
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            System.exit(-1);
        }

    }
}
