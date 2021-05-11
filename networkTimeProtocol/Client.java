package networkTimeProtocol;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;
import java.util.concurrent.ThreadLocalRandom;

public class Client {

    static void prepareScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) new ProcessBuilder("cmd", "/c", "color", "0f", "&", "cls").inheritIO().start().waitFor();
            else Runtime.getRuntime().exec("clear");
        } catch (IOException | InterruptedException ex) {}
    }
        

    static void checkArguments(String[] args){
        
        // Check if argument count is different than expected. Else Throw an exception.
        if(args.length > 2){
            throw new IllegalArgumentException("Too much argument!");
        }
        
        // Check if first argument is integer. It will throw an exception if argument is not an integer.
        Integer.parseInt(args[0]);
        
        // Simple pattern for (ip | domain name | "localhost"):port
        Pattern ipPattern = Pattern.compile("^"
            + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
            + "|" + "localhost" // localhost
            + "|" + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip

            + ":" + "[0-9]{1,5}$" // Port
        );

        // Check if given argument for server connection has valid syntax
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
        // Clear screen
        prepareScreen();
        System.out.println("Client Started!");

        // Initialize values
        int offsetValue = 0, serverPort = 0, ITERATION_COUNT = 8, MIN_DELAY = 1, MAX_DELAY = 1000, delay1, delay2;
        String serverHost = "", message = "";
        long t1, t2, t3, t4, delta = Long.MAX_VALUE, theta = Long.MAX_VALUE, cur_delta, cur_theta, cur_time;

        try {
            // Check Input is valid or not
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
        
        // First try to connect to server using Socket library.
        try (Socket clientSocket = new Socket( serverHost, serverPort )){

            // Then Create input & output stream for communication between client & server.
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String serverResponse;

            // Send empty message to Server
            writer.println("");

            // After initialization Wait for server to be ready to start. If message != "READY", print the message
            while (true) {
                message = reader.readLine();
                if (message.equals("READY")) { break; }
                else { System.out.println(message); }
            }

            // Start iterations
            System.out.println("Iterations starting");
            for (int i = 0; i < ITERATION_COUNT; i++) {
                // Create random delays if server in localhost else assign 0 to delays.
                if(serverHost.equals("localhost")){
                    delay1 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);
                    delay2 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);
                }else{
                    delay1 = 0;
                    delay2 = 0;
                }
                
                t1 = System.currentTimeMillis() + offsetValue;
                // Send empty message to server on time t1
                writer.println("");

                serverResponse = reader.readLine();
                
                // Get t2 & t3 from server. Then add delays.
                t2 = Long.parseLong(serverResponse.split(" ")[0]) + delay1;
                t3 = Long.parseLong(serverResponse.split(" ")[1]) + delay1;

                
                t4 = System.currentTimeMillis() + offsetValue + delay1 + delay2;

                // Calculate delta & theta for current iteration
                cur_delta = (t2 - t1 + t4 - t3) / 2;
                cur_theta = t3 + cur_delta - t4;

                // If current iteration's delta is smaller than theta, update theta & delta with current theta & delta
                if(cur_delta < delta){
                    theta = cur_theta;
                    delta = cur_delta;
                }

                // Print iteration info
                System.out.println("ITERATION " + (i + 1) + ":");
                System.out.println("\tDelays: " + delay1 + " "  + delay2);
                System.out.println("\tT1: " + t1 + "\tT2: " + t2 + "\tT3: " + t3 + "\tT4: " + t4);
                System.out.println("\tDELTA: " + cur_delta + "\tTHETA: " + cur_theta);
            }

            // Send bye message to server
            writer.println("bye");

            // Print result info
            System.out.println("Selected THETA: " + theta + " that with the smallest DELTA which is: " + delta);

            cur_time = System.currentTimeMillis() + offsetValue;
            System.out.println("Old Local Timestamp: " + cur_time);
            System.out.println("New Local Timestamp: " + (cur_time + theta));

            System.out.println("Difference between new & old timestamp: " + theta);

            System.out.println("\nOffset value: " + offsetValue);
            System.out.println("Difference between offset & theta: " + (Math.abs(offsetValue - theta)));

            // Close socket
            clientSocket.close();
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
            System.exit(-1);
        }
    }
}
