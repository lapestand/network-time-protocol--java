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
        if(args.length > 2){
            throw new ArithmeticException("Too much argument!");
        }
        
        Integer.parseInt(args[0]);
        
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
        int offsetValue = 0, serverPort = 0, ITERATION_COUNT = 8, MIN_DELAY = 1, MAX_DELAY = 1000, delay1, delay2, delay3, delay4;
        String serverHost = "", message = "";
        long t1, t2, t3, t4, delta = Long.MAX_VALUE, theta = Long.MAX_VALUE, cur_delta, cur_theta, cur_time;
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

            writer.println("");

            while (true) {
                message = reader.readLine();
                if (message.equals("READY")) {break;}
                else {System.out.println(message);}
            }
            System.out.println("Iterations starting");
            for (int i = 0; i < ITERATION_COUNT; i++) {
                delay1 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);
                delay2 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);
                delay3 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);
                delay4 = ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY + 1);


                t1 = System.currentTimeMillis() + offsetValue + delay1;
                writer.println("");

                serverResponse = reader.readLine();
                
                t4 = System.currentTimeMillis() + offsetValue + delay1 + delay2 + delay3 + delay4;
                t2 = Long.parseLong(serverResponse.split(" ")[0]) + delay1 + delay2;
                t3 = Long.parseLong(serverResponse.split(" ")[1]) + delay1 + delay2 + delay3;

                cur_delta = (t2 - t1 + t4 - t3) / 2;
                cur_theta = t3 + cur_delta - t4;

                if(cur_delta < delta){
                    theta = cur_theta;
                    delta = cur_delta;
                }

                System.out.println("ITERATION " + (i + 1) + ":");
                System.out.println("\tDelays: " + delay1 + " "  + delay2 + " "  + delay3 + " " + delay4);
                System.out.println("\tT1: " + t1 + "\tT2: " + t2 + "\tT3: " + t3 + "\tT4: " + t4);
                System.out.println("\tDELTA: " + cur_delta + "\tTHETA: " + cur_theta);
            }
            writer.println("bye");

            System.out.println("Selected THETA: " + theta + " that with the smallest DELTA which is: " + delta);

            cur_time = System.currentTimeMillis() + offsetValue;
            System.out.println("Old Local Timestamp: " + cur_time);
            System.out.println("New Local Timestamp: " + (cur_time + theta));

            System.out.println("Difference between new & old timestamp: " + theta);

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
