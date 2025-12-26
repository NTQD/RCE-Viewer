package server.core;

import server.db.UserDAO;
import server.db.CommandHistoryDAO;
import server.db.AllowCommandDAO;
import server.model.User;
import server.model.CommandHistory;
import server.model.AllowCommand;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class ServerManager {

    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running = false;
    private int connectionCount = 0;

    private final Map<Socket, String> onlineUsers = Collections.synchronizedMap(new HashMap<>());
    private final ServerEvents events;

    public interface ApproveCallback {

        void approve();

        void reject();
    }

    public interface ServerEvents {

        void onLog(String text);

        void onConnectionChanged(int count);

        void onLoginRequest(String username, ApproveCallback callback);
    }

    public ServerManager(ServerEvents events) {
        this.events = events;
    }

    private void broadcastUserList() {
        String userList = String.join(",", onlineUsers.values());
        String message = "USERS:" + userList;

        synchronized (onlineUsers) {
            for (Socket clientSocket : onlineUsers.keySet()) {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(message);
                } catch (IOException e) {
                    events.onLog("Could not send user list to " + clientSocket.getInetAddress());
                }
            }
        }
        events.onLog("Broadcasted user list: " + userList);
    }

    private void sendHistoryResponse(PrintWriter out, List<CommandHistory> history, int userId) {
        out.println("BEGIN_HISTORY");
        if (history == null || history.isEmpty()) {
            out.println("NO_HISTORY");
        } else {
            for (CommandHistory item : history) {
                String command = item.getCommand() == null ? "" : item.getCommand();
                String result = item.getResult() == null ? "" : item.getResult();
                String encodedCommand = encodeBase64(command);
                String encodedResult = encodeBase64(result);
                out.println(item.getId() + "|" + userId + "|" + encodedCommand + "|" + encodedResult);
            }
        }
        out.println("END_HISTORY");
    }

    private String encodeBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isRunning() {
        return running;
    }

    public void startServer(int port) {
        if (running) {
            return;
        }

        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                events.onLog("SERVER RUNNING ON PORT " + port);

                while (running) {
                    try {
                        Socket client = serverSocket.accept();
                        if (!running) {
                            client.close();
                            break;
                        }
                        connectionCount++;
                        events.onConnectionChanged(connectionCount);
                        events.onLog("Client connected: " + client.getInetAddress());
                        new Thread(() -> handleClient(client)).start();
                    } catch (IOException ex) {
                        if (running) {
                            events.onLog("ACCEPT ERROR: " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                events.onLog("SERVER ERROR: " + e.getMessage());
            }
        });
        serverThread.start();
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            synchronized (onlineUsers) {
                for (Socket s : onlineUsers.keySet()) {
                    try {
                        s.close();
                    } catch (Exception ignored) {
                    }
                }
                onlineUsers.clear();
            }
            connectionCount = 0;
            events.onConnectionChanged(0);
            events.onLog("SERVER STOPPED");
        } catch (Exception e) {
            events.onLog("ERROR STOPPING SERVER: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        User user = null;
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("SERVER_READY");
            String firstLine = in.readLine();
            if (firstLine == null) {
                return;
            }

            if (firstLine.startsWith("REGISTER ")) {
                events.onLog("Registration attempt from " + socket.getInetAddress());
                String[] parts = firstLine.split(" ", 5);
                if (parts.length < 5) {
                    out.println("REGISTER_FAILED_INVALID_COMMAND");
                    return;
                }

                String newUsername = parts[1];
                String newPassword = parts[2];
                String newFullName = parts[3];
                String newEmail = parts[4];

                UserDAO userDAO = new UserDAO();
                if (userDAO.getUserByUsername(newUsername) != null) {
                    out.println("REGISTER_FAILED_USER_EXISTS");
                    return;
                }

                User newUser = new User(0, newUsername, newPassword, newFullName, newEmail, false);
                boolean success = userDAO.addUser(newUser);

                if (success) {
                    out.println("REGISTER_OK");
                    events.onLog("New user registered: " + newUsername);
                } else {
                    out.println("REGISTER_FAILED_DB_ERROR");
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                return;
            }

            String username = firstLine;
            String password = in.readLine();
            if (password == null) {
                return;
            }

            out.println("WAITING_FOR_APPROVAL");
            events.onLog("LOGIN REQUEST FROM: " + username);

            final Object lock = new Object();
            final boolean[] approved = {false};
            final boolean[] rejected = {false};

            events.onLoginRequest(username, new ApproveCallback() {
                @Override
                public void approve() {
                    synchronized (lock) {
                        approved[0] = true;
                        lock.notifyAll();
                    }
                }

                @Override
                public void reject() {
                    synchronized (lock) {
                        rejected[0] = true;
                        lock.notifyAll();
                    }
                }
            });

            synchronized (lock) {
                while (!approved[0] && !rejected[0] && running && !socket.isClosed()) {
                    lock.wait();
                }
            }

            if (rejected[0] || !approved[0]) {
                out.println("AUTH_DECLINED");
                return;
            }

            UserDAO userDAO = new UserDAO();
            user = userDAO.login(username, password);

            if (user == null) {
                out.println("AUTH_FAILED");
                return;
            }

            out.println("AUTH_OK:" + user.getFullName() + ":" + user.isAdmin());
            events.onLog("LOGIN OK FOR: " + username);

            onlineUsers.put(socket, user.getUsername());
            broadcastUserList();

            AllowCommandDAO allowDAO = new AllowCommandDAO();
            CommandHistoryDAO histDAO = new CommandHistoryDAO();
            String command;

            while (running && !socket.isClosed() && (command = in.readLine()) != null) {
                String rawInput = command.trim();
                String realCommand = rawInput;

                // Nếu client nhập ID (chỉ toàn số)
                if (rawInput.matches("\\d+")) {
                    int cmdId = Integer.parseInt(rawInput);
                    AllowCommand ac = allowDAO.getById(cmdId);

                    if (ac == null || !ac.getIs_active()) {
                        out.println("INVALID_COMMAND_ID");
                        continue;
                    }

                    realCommand = ac.getCommand_text();
                }
                if (command.equals("GET_ALLOW_COMMANDS")) {
                    out.println("BEGIN_ADMIN_CMDS");
                    for (AllowCommand ac : allowDAO.getAll()) {
                        out.println(ac.getCmd_id() + "|" + ac.getCommand_text() + "|" + ac.getIs_active());
                    }
                    out.println("END_ADMIN_CMDS");
                    continue;
                }

                if (command.equals("GET_HISTORY_USER")) {
                    List<CommandHistory> history = histDAO.getByUserId(user.getId());
                    sendHistoryResponse(out, history, user.getId());
                    continue;
                }

                if (command.startsWith("SEARCH_HISTORY:")) {
                    String keyword = command.substring("SEARCH_HISTORY:".length()).trim();
                    List<CommandHistory> history;
                    if (keyword.isEmpty()) {
                        history = histDAO.getByUserId(user.getId());
                    } else {
                        history = histDAO.searchByUserIdAndKeyword(user.getId(), keyword);
                    }
                    sendHistoryResponse(out, history, user.getId());
                    continue;
                }
                if (command.equalsIgnoreCase("cpu")) {

                    String realCmd = "wmic cpu get name";

                    if (!allowDAO.isCommandAllowed(realCmd, user.isAdmin())) {
                        out.println("COMMAND_NOT_ALLOWED");
                        continue;
                    }

                    String result = execute(realCmd);
                    out.println(result);
                    out.println("END");
                    continue;
                }

                if (command.equalsIgnoreCase("disk")) {

                    String realCmd = "wmic logicaldisk get size,freespace,caption";

                    if (!allowDAO.isCommandAllowed(realCmd, user.isAdmin())) {
                        out.println("COMMAND_NOT_ALLOWED");
                        continue;
                    }

                    String result = execute(realCmd);
                    out.println(result);
                    out.println("END");
                    continue;
                }

                if (command.startsWith("ADD_ALLOW:")) {
                    if (!user.isAdmin()) {
                        out.println("DENIED");
                        continue;
                    }
                    String cmdText = command.substring("ADD_ALLOW:".length());
                    AllowCommand ac = new AllowCommand();
                    ac.setCommand_text(cmdText);
                    ac.setUser_id(user.getId());
                    ac.setIs_active(true);
                    boolean added = allowDAO.add(ac, user.getId());
                    if (added) {
                        out.println("ALLOW_ADDED");
                    } else {
                        out.println("ADD_ALLOW_DUPLICATE");
                    }
                    continue;
                }

                if (command.startsWith("DELETE_ALLOW:")) {
                    if (!user.isAdmin()) {
                        out.println("DENIED");
                        continue;
                    }
                    int id = Integer.parseInt(command.substring("DELETE_ALLOW:".length()));
                    allowDAO.delete(id, user.getId());
                    out.println("ALLOW_DELETED");
                    continue;
                }

                if (command.startsWith("DELETE_HISTORY:")) {
                    int historyId = Integer.parseInt(command.substring("DELETE_HISTORY:".length()));
                    boolean deleted = histDAO.deleteById(historyId, user.getId(), user.isAdmin());
                    out.println(deleted ? "HISTORY_DELETED" : "HISTORY_DELETE_FAILED");
                    continue;
                }

                if (!allowDAO.isCommandAllowed(realCommand, user.isAdmin())) {
                    out.println("COMMAND_NOT_ALLOWED");
                    continue;
                }

                events.onLog("EXECUTING FROM " + user.getUsername() + ": " + realCommand);
                Process p = null;
                try {
                    p = Runtime.getRuntime().exec("cmd.exe /c " + realCommand);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    StringBuilder result = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                        result.append(line).append("\n");
                    }
                    out.println("END");
                    p.waitFor();

                    histDAO.saveHistory(
                            user.getId(),
                            socket.getInetAddress().toString(),
                            command,
                            result.toString(),
                            socket.getInetAddress().toString()
                    );

                } catch (Exception ex) {
                    out.println("EXEC_ERROR: " + ex.getMessage());
                    out.println("END");
                } finally {
                    if (p != null) {
                        p.destroy();
                    }
                }
            }
        } catch (Exception e) {
            events.onLog("CLIENT ERROR: " + (user != null ? user.getUsername() : "") + " " + e.getMessage());
        } finally {
            if (onlineUsers.containsKey(socket)) {
                String username = onlineUsers.remove(socket);
                events.onLog("User disconnected: " + username);
                broadcastUserList();
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            connectionCount--;
            if (connectionCount < 0) {
                connectionCount = 0;
            }
            events.onConnectionChanged(connectionCount);
        }
    }

    private String execute(String command) {
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8")
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }

        return output.toString();
    }

}
