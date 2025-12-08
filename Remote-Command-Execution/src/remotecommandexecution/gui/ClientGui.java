package remotecommandexecution.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ClientGui extends JFrame {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private JTextArea onlineUsersArea;
    private JTextArea resultsArea;
    
    private boolean isCurrentUserAdmin = false;
    private CommandAdmin adminForm = null;
    private HistoryForm historyForm = null;

    private Font getAppFont(float size, int style) {
        return new Font("Verdana", style, (int) size);
    }

    private final Color MAIN_BACKGROUND_COLOR = new Color(210, 210, 240);
    private final Color BORDER_COLOR = new Color(180, 180, 200);

    public ClientGui(Socket socket, BufferedReader in, PrintWriter out, boolean isAdmin) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.isCurrentUserAdmin = isAdmin;

        setTitle("RCE Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(MAIN_BACKGROUND_COLOR);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(" RCE Client");
        titleLabel.setFont(getAppFont(24, Font.BOLD));
        topPanel.add(titleLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        onlineUsersArea = createTextArea();
        JScrollPane onlineUsersScroll = createTitledScrollPane(onlineUsersArea, "Online users:", getAppFont(14, Font.BOLD));
        resultsArea = createTextArea();
        JScrollPane resultsScroll = createTitledScrollPane(resultsArea, "Results:", getAppFont(14, Font.BOLD));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, onlineUsersScroll, resultsScroll);
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        buttonPanel.setOpaque(false);
        Color buttonBaseColor = Color.WHITE;
        Color borderColor = new Color(50, 50, 50);

        RoundedButton cpuButton = new RoundedButton("Get CPU Usage", buttonBaseColor, Color.BLACK, borderColor);
        RoundedButton diskButton = new RoundedButton("Get Disk Space", buttonBaseColor, Color.BLACK, borderColor);
        RoundedButton customCmdButton = new RoundedButton("Send Custom Command", buttonBaseColor, Color.BLACK, borderColor);
        RoundedButton historyButton = new RoundedButton("History", buttonBaseColor, Color.BLACK, borderColor);
        RoundedButton adminButton = new RoundedButton("Admin", buttonBaseColor, Color.BLACK, borderColor);
        
        adminButton.setVisible(isCurrentUserAdmin);

        cpuButton.addActionListener(e -> sendCommand("cpu"));
        diskButton.addActionListener(e -> sendCommand("disk"));
        customCmdButton.addActionListener(e -> showCustomCommandDialog());
        historyButton.addActionListener(e -> {
            ensureHistoryForm();
            historyForm.setVisible(true);
            historyForm.showLoadingMessage("Đang tải lịch sử...");
            requestHistory("");
        });
        adminButton.addActionListener(e -> openAdminForm());

        buttonPanel.add(cpuButton);
        buttonPanel.add(diskButton);
        buttonPanel.add(customCmdButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(adminButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftButtonPanel.setOpaque(false);
        RoundedButton logoutButton = new RoundedButton("Log Out", new Color(220, 80, 80), Color.WHITE, borderColor);
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                close_app();
                dispose();
                new LoginForm().setVisible(true);
            }
        });
        leftButtonPanel.add(logoutButton);
        bottomPanel.add(leftButtonPanel, BorderLayout.WEST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        startListenerThread();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close_app();
            }
        });
    }
    
    public void close_app() {
        try {
            if (out != null) {
                out.println("LOGOUT");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error closing client resources: " + ex.getMessage());
        }
    }
    
    private void openAdminForm() {
        if (adminForm == null) {
            adminForm = new CommandAdmin(this, out);
        }
        adminForm.setVisible(true);
    }

    private void ensureHistoryForm() {
        if (historyForm == null) {
            historyForm = new HistoryForm(this, this::requestHistory, this::deleteHistory);
            historyForm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    historyForm = null;
                }
            });
        }
    }

    private void requestHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            out.println("GET_HISTORY_USER");
        } else {
            out.println("SEARCH_HISTORY:" + keyword.trim());
        }
    }

    private void deleteHistory(int historyId) {
        out.println("DELETE_HISTORY:" + historyId);
    }

    private void startListenerThread() {
        final ClientGui mainFrame = this;
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    final String currentLine = line;

                    if (currentLine.equals("BEGIN_HISTORY")) {
                        List<String[]> historyRows = new ArrayList<>();
                        String historyLine = in.readLine();
                        while (historyLine != null && !historyLine.equals("END_HISTORY")) {
                            if (!historyLine.isEmpty() && !historyLine.equals("NO_HISTORY")) {
                                String[] parts = historyLine.split("\\|", 4);
                                if (parts.length == 4) {
                                    String decodedCommand = decodeBase64(parts[2]);
                                    String decodedResult = decodeBase64(parts[3]);
                                    historyRows.add(new String[]{parts[0], parts[1], decodedCommand, decodedResult});
                                }
                            }
                            historyLine = in.readLine();
                        }
                        final List<String[]> finalHistoryRows = historyRows;
                        SwingUtilities.invokeLater(() -> {
                            mainFrame.ensureHistoryForm();
                            mainFrame.historyForm.updateHistory(finalHistoryRows);
                            mainFrame.historyForm.setVisible(true);
                        });
                        continue;
                    }
                    
                    if (currentLine.equals("BEGIN_ADMIN_CMDS")) {
                        List<String> commandRows = new ArrayList<>();
                        String cmdLine = in.readLine();
                        // Đọc danh sách lệnh admin tới khi gặp "END_ADMIN_CMDS" hoặc kết nối bị đóng
                        while (cmdLine != null && !cmdLine.equals("END_ADMIN_CMDS")) {
                            commandRows.add(cmdLine);
                            cmdLine = in.readLine();
                        }
                        if (adminForm != null) {
                            adminForm.refreshTable(commandRows);
                        }
                        continue;
                    }
                    
                    if (currentLine.equals("ALLOW_ADDED") || currentLine.equals("ALLOW_DELETED")) {
                        if (adminForm != null && adminForm.isVisible()) {
                            adminForm.loadCommands();
                        }
                        continue;
                    }
                    
                    if (currentLine.equals("ADD_ALLOW_DUPLICATE")) {
                        if (adminForm != null && adminForm.isVisible()) {
                            adminForm.showDuplicateError();
                        }
                        continue;
                    }

                    if (currentLine.equals("HISTORY_DELETED")) {
                        if (historyForm != null && historyForm.isVisible()) {
                            historyForm.reloadLastSearch();
                        }
                        continue;
                    }

                    if (currentLine.equals("HISTORY_DELETE_FAILED")) {
                        if (historyForm != null && historyForm.isVisible()) {
                            historyForm.showErrorMessage("Không thể xoá lịch sử đã chọn. Vui lòng thử lại.");
                        }
                        continue;
                    }

                    if (currentLine.startsWith("USERS:")) {
                        String users = currentLine.replace("USERS:", "");
                        SwingUtilities.invokeLater(() -> onlineUsersArea.setText(users.replace(",", "\n")));
                        continue;
                    }

                    SwingUtilities.invokeLater(() -> {
                        if (currentLine.equals("END")) {
                            resultsArea.append("----- END -----\n");
                        } else {
                            resultsArea.append(currentLine + "\n");
                        }
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> resultsArea.append("[Disconnected from server]\n"));
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private void sendCommand(String cmd) {
        out.println(cmd);
        resultsArea.append("> " + cmd + "\n");
    }

    private void showCustomCommandDialog() {
        String cmd = JOptionPane.showInputDialog(this, "Nhập lệnh tùy chỉnh:");
        if (cmd != null && !cmd.trim().isEmpty()) {
            sendCommand(cmd);
        }
    }

    private String decodeBase64(String value) {
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(getAppFont(14, Font.PLAIN));
        area.setBackground(Color.WHITE);
        return area;
    }

    private JScrollPane createTitledScrollPane(Component view, String title, Font titleFont) {
        JScrollPane scroll = new JScrollPane(view);
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(BORDER_COLOR, 1), title, TitledBorder.LEFT, TitledBorder.TOP);
        tb.setTitleFont(titleFont);
        scroll.setBorder(tb);
        return scroll;
    }

    class RoundedButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color currentColor;
        private Color borderColor;
        private final int arcWidth = 10;
        private final int borderThickness = 1;

        public RoundedButton(String text, Color base, Color fg, Color borderC) {
            super(text);
            this.baseColor = base;
            this.setForeground(fg);
            this.borderColor = borderC;

            this.hoverColor = new Color(240, 240, 240);
            this.pressedColor = new Color(220, 220, 220);
            this.currentColor = baseColor;

            this.setFont(new Font("Verdana", Font.BOLD, 13));
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { currentColor = hoverColor; repaint(); }
                @Override public void mouseExited(MouseEvent e) { currentColor = baseColor; repaint(); }
                @Override public void mousePressed(MouseEvent e) { currentColor = pressedColor; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    currentColor = getBounds().contains(e.getPoint()) ? hoverColor : baseColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arcWidth, arcWidth));
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderThickness));
            g2.draw(new RoundRectangle2D.Double(
                    borderThickness / 2.0,
                    borderThickness / 2.0,
                    getWidth() - borderThickness,
                    getHeight() - borderThickness,
                    arcWidth,
                    arcWidth
            ));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
