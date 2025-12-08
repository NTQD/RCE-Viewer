package remotecommandexecution.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Server IP: ");
        String serverIP = scanner.nextLine();

        System.out.print("Enter Server Port: ");
        int serverPort = scanner.nextInt();
        scanner.nextLine(); // clear

        try (
                Socket socket = new Socket(serverIP, serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {

            System.out.println("Connected to server.");

            // ===========================
            // STEP 1 – Đọc SERVER_READY
            // ===========================
            String line = in.readLine();
            if (!"SERVER_READY".equals(line)) {
                System.out.println("Server error: unexpected message → " + line);
                return;
            }
            System.out.println("[SERVER] Ready.");

            // ===========================
            // STEP 2 – Gửi Username/Password
            // ===========================
            System.out.print("Username: ");
            String username = scanner.nextLine();
            out.println(username);

            System.out.print("Password: ");
            String password = scanner.nextLine();
            out.println(password);

            // ===========================
            // STEP 3 – CHỜ PHẢN HỒI SERVER
            // ===========================
            while (true) {
                line = in.readLine();

                if (line == null) {
                    System.out.println("Server closed connection.");
                    return;
                }

                if (line.equals("WAITING_FOR_APPROVAL")) {
                    System.out.println("[SERVER] Waiting for admin approval...");
                }

                if (line.equals("AUTH_DECLINED")) {
                    System.out.println("[SERVER] Login REJECTED by admin.");
                    return;
                }

                if (line.equals("AUTH_FAILED")) {
                    System.out.println("[SERVER] Wrong username/password.");
                    return;
                }

                if (line.startsWith("AUTH_OK:")) {
                    System.out.println("[SERVER] Login successful.");
                    System.out.println("Welcome " + line);
                    break; // continue to command mode
                }
            }

            System.out.println("You can now enter commands (type EXIT to quit).");

            // ================================================================
            // ====================== RECEIVE THREAD ===========================
            // ================================================================
            Thread listener = new Thread(() -> {
                try {
                    String serverLine;
                    while ((serverLine = in.readLine()) != null) {
                        if (serverLine.equals("END")) {
                            System.out.println("---- END ----");
                        } else {
                            System.out.println(serverLine);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected.");
                }
            });
            listener.setDaemon(true);
            listener.start();

            // ================================================================
            // ========================= SEND LOOP =============================
            // ================================================================
            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine();

                if (cmd.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnecting...");
                    break;
                }

                out.println(cmd);
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
