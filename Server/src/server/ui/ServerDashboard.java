package server.ui;

import server.core.ServerManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.InetAddress;

public class ServerDashboard extends JFrame {

    private final JTextArea logArea = new JTextArea();
    private final JLabel lblStatus = new JLabel("STOPPED");
    private final JLabel lblConnections = new JLabel("0");
    private final JLabel lblIP = new JLabel("---");

    private RoundedButton btnStart;
    private RoundedButton btnStop;
    private RoundedPanel statusPanel; // Reference to the status panel to change its background

    private final Color MAIN_BACKGROUND = new Color(210, 210, 240);
    private final Color BORDER = new Color(180, 180, 200);
    private final Color ACTIVE = new Color(100, 50, 150);
    private final Color STOPPED = new Color(200, 50, 50);
    private final Color DISABLED = new Color(150, 150, 150);

    // MÀU SẮC TÙY CHỈNH (User Custom Changes here)
    // ============================================
    // 1. NỀN NÚT (Button Background)
    // Nút Start: Xanh lục (#6DCD01)
    private final Color START_GREEN = new Color(0x6DCD01);
    // Nút Stop: Cam (#FF7037)
    private final Color STOP_ORANGE = new Color(0xFF7037);

    // 2. CHỮ VÀ VIỀN (Text & Border)
    // Màu chữ của nút (Text Color): Đen
    private final Color BTN_TEXT_COLOR = Color.BLACK;
    // Màu viền của nút (Border Color): Đen
    private final Color BTN_BORDER_COLOR = Color.BLACK;

    // 3. TRẠNG THÁI (Status Field)
    // Nền Status khi RUNNING: Xanh lá nhẹ (Bạn có thể đổi mã màu ở đây nếu chưa
    // ưng)
    private final Color RUNNING_BG = new Color(164, 232, 139);

    private final int PORT = 12345;

    private final ServerManager server;

    public ServerDashboard() {
        setTitle("RCE Server Admin");
        setSize(1050, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }

        server = new ServerManager(new ServerManager.ServerEvents() {

            @Override
            public void onLog(String text) {
                SwingUtilities.invokeLater(() -> logArea.append(text + "\n"));
            }

            @Override
            public void onConnectionChanged(int count) {
                SwingUtilities.invokeLater(() -> lblConnections.setText(String.valueOf(count)));
            }

            @Override
            public void onLoginRequest(String username, ServerManager.ApproveCallback callback) {
                SwingUtilities.invokeLater(() -> {

                    int confirm = JOptionPane.showConfirmDialog(
                            ServerDashboard.this,
                            "Người dùng \"" + username + "\" yêu cầu đăng nhập.\nBạn có đồng ý không?",
                            "Phê duyệt đăng nhập",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        log("Admin approved login for: " + username);
                        callback.approve();
                    } else {
                        log("Admin rejected login for: " + username);
                        callback.reject();
                    }
                });
            }
        });

        initUI();
        loadIP();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    private void loadIP() {
        try {
            lblIP.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            lblIP.setText("Unknown");
        }
    }

    // ======================= UI ============================
    private void initUI() {

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(MAIN_BACKGROUND);
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        top.setOpaque(false);

        // --- Custom STATUS field construction to capture statusPanel ---
        statusPanel = new RoundedPanel(new BorderLayout(), Color.WHITE, BORDER);
        statusPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel statusHeader = new JLabel("STATUS");
        statusHeader.setHorizontalAlignment(SwingConstants.CENTER);
        statusHeader.setFont(new Font("Verdana", Font.BOLD, 10));

        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setFont(new Font("Verdana", Font.BOLD, 14));

        statusPanel.add(statusHeader, BorderLayout.NORTH);
        statusPanel.add(lblStatus, BorderLayout.CENTER);

        JPanel statusWrapper = new JPanel(new BorderLayout());
        statusWrapper.setOpaque(false);
        statusWrapper.add(statusPanel);

        top.add(statusWrapper);
        // -------------------------------------------------------------

        top.add(field("CONNECTIONS", lblConnections));
        top.add(field("SERVER IP", lblIP));
        top.add(field("PORT", new JLabel(String.valueOf(PORT))));
        Font BTN_FONT = new Font("Verdana", Font.BOLD, 16);

        // Start Button with START_GREEN and hover effect (brighter)
        // [Background, Hover, Border, Text]
        btnStart = new RoundedButton("Start",
                START_GREEN, START_GREEN.brighter(), BTN_BORDER_COLOR, BTN_TEXT_COLOR);
        btnStart.setFont(BTN_FONT);
        // Stop Button with STOP_ORANGE and hover effect (brighter)
        btnStop = new RoundedButton("Stop",
                STOP_ORANGE, STOP_ORANGE.brighter(), BTN_BORDER_COLOR, BTN_TEXT_COLOR);
        btnStop.setFont(BTN_FONT);
        btnStop.setEnabled(false);

        btnStart.addActionListener(e -> startServer());
        btnStop.addActionListener(e -> stopServer());

        top.add(btnStart);
        top.add(btnStop);

        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane sp = new JScrollPane(logArea);
        sp.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER, 2),
                "SERVER LOG",
                TitledBorder.LEFT, TitledBorder.TOP));

        main.add(top, BorderLayout.NORTH);
        main.add(sp, BorderLayout.CENTER);

        add(main);
    }

    // Panel small info
    private JPanel field(String title, JLabel label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        RoundedPanel box = new RoundedPanel(new BorderLayout(), Color.WHITE, BORDER);
        box.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel(title);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 10));

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Verdana", Font.BOLD, 14));

        box.add(header, BorderLayout.NORTH);
        box.add(label, BorderLayout.CENTER);
        p.add(box);

        return p;
    }

    private void startServer() {
        server.startServer(PORT);
        lblStatus.setText("RUNNING");
        lblStatus.setForeground(ACTIVE.darker());

        // Change Status background to RUNNING_BG
        statusPanel.bg = RUNNING_BG;
        statusPanel.repaint();

        btnStart.setEnabled(false);
        btnStop.setEnabled(true);
        log("Server started on port " + PORT);
    }

    private void stopServer() {
        server.stopServer();
        lblStatus.setText("STOPPED");
        lblStatus.setForeground(STOPPED.darker());

        // Reset Status background to WHITE
        statusPanel.bg = Color.WHITE;
        statusPanel.repaint();

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        log("Server stopped.");
    }

    // ================ Custom Button =======================
    class RoundedButton extends JButton {
        private Color base, hover, pressed, border, current;
        private final int arc = 12;

        public RoundedButton(String txt, Color base, Color hover, Color border, Color fg) {
            super(txt);
            this.base = base;
            this.hover = hover;
            this.pressed = new Color(
                    Math.max(hover.getRed() - 20, 0),
                    Math.max(hover.getGreen() - 20, 0),
                    Math.max(hover.getBlue() - 20, 0));
            this.border = border;
            this.current = base;

            setForeground(fg);
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled())
                        current = hover;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (isEnabled())
                        current = base;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled())
                        current = pressed;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isEnabled()) {
                        current = hover;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(current);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedPanel extends JPanel {
        public Color bg;
        private Color border;

        public RoundedPanel(LayoutManager layout, Color bg, Color border) {
            super(layout);
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerDashboard().setVisible(true));
    }
}
