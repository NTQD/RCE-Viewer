package remotecommandexecution.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginForm extends JFrame {

    // ==========================
    // KHAI BÁO FIELD LOGIN
    // ==========================
    private JTextField serverField = new JTextField("localhost");
    private JTextField portField = new JTextField("12345");
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    private Font getAppFont(float size, int style) {
        return new Font("Verdana", style, (int) size);
    }

    private Border createRoundedLineBorder() {
        return new LineBorder(new Color(150, 150, 170), 2, true);
    }

    public LoginForm(String server, String port, String username, String password) {
        this();
        serverField.setText(server);
        portField.setText(port);
        usernameField.setText(username);
        passwordField.setText(password);
    }

    public LoginForm() {
        setTitle("Remote Command Execution - Team 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        add(mainPanel);

        // ==================== LEFT ====================
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(218, 222, 245));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(80, 50, 80, 50));
        leftPanel.add(Box.createVerticalGlue());

        JLabel teamLabel = new JLabel("Team 1");
        teamLabel.setFont(getAppFont(18, Font.PLAIN));
        JLabel titleLabel = new JLabel("Remote Command Execution");
        titleLabel.setFont(getAppFont(24, Font.BOLD));

        leftPanel.add(teamLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(titleLabel);

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/remotecommandexecution/images/hehenho.gif"));
            JLabel img = new JLabel(icon);

            JPanel imageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            imageWrapper.setOpaque(false);
            imageWrapper.add(img);

            imageWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            teamLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            leftPanel.add(Box.createVerticalStrut(30));
            leftPanel.add(imageWrapper);

        } catch (Exception e) {
            leftPanel.add(new JLabel("Image not found"));
        }

        leftPanel.add(Box.createVerticalGlue());
        mainPanel.add(leftPanel);

        // ==================== RIGHT PANEL ====================
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(70, 80, 70, 80));
        rightPanel.setBackground(Color.WHITE);

        JLabel loginLabel = new JLabel("LOG IN");
        loginLabel.setFont(getAppFont(22, Font.BOLD));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleRoundedField(serverField, "Server");
        styleRoundedField(portField, "Port");
        styleRoundedField(usernameField, "Username");

        JPanel passwordPanel = createPasswordFieldPanel("Password");

        // Nút LOGIN (gắn logic)
        RoundedButton nextButton = new RoundedButton("LOGIN", new Color(0, 30, 80), Color.WHITE);
        nextButton.setFont(getAppFont(16, Font.BOLD));
        nextButton.setMaximumSize(new Dimension(280, 48));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextButton.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin()); // Add this line

        // Add vào panel phải
        rightPanel.add(loginLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(serverField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(portField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(passwordPanel);
        rightPanel.add(Box.createVerticalStrut(40));
        rightPanel.add(nextButton);

        // Thêm label và nút cho Sign Up
        JLabel signUpPrompt = new JLabel("Don't have an account?");
        signUpPrompt.setFont(getAppFont(13, Font.PLAIN));
        signUpPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signUpButton = new JButton("<html><u>Sign up now</u></html>");
        signUpButton.setFont(getAppFont(13, Font.BOLD));
        signUpButton.setForeground(new Color(0, 102, 204));
        signUpButton.setBorder(null);
        signUpButton.setContentAreaFilled(false);
        signUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        signUpButton.addActionListener(e -> {
            new SignInForm().setVisible(true);
            dispose();
        });

        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(signUpPrompt);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(signUpButton);

        mainPanel.add(rightPanel);
    }

    // ==========================
    // LOGIC LOGIN SOCKET
    // ==========================
    private void doLogin() {
        new Thread(() -> {
            try {
                String server = serverField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                Socket socket = new Socket(server, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String msg = in.readLine();
                if (!"SERVER_READY".equals(msg)) {
                    JOptionPane.showMessageDialog(this, "Server không phản hồi đúng protocol!");
                    return;
                }

                out.println(username);
                out.println(password);

                while (true) {
                    msg = in.readLine();

                    if (msg == null) {
                        JOptionPane.showMessageDialog(this, "Server đóng kết nối!");
                        return;
                    }

                    switch (msg) {
                        case "WAITING_FOR_APPROVAL":
                            System.out.println("Chờ server duyệt...");
                            break;

                        case "AUTH_DECLINED":
                            JOptionPane.showMessageDialog(this, "Server đã từ chối đăng nhập!");
                            return;

                        case "AUTH_FAILED":
                            JOptionPane.showMessageDialog(this, "Thông tin tài khoản hoặc mật khẩu không chính xác!");
                            return;

                        default:
                            if (msg.startsWith("AUTH_OK")) {
                                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                                // Parse the admin status from the message
                                String[] parts = msg.split(":", 3);
                                boolean isAdmin = parts.length > 2 && Boolean.parseBoolean(parts[2]);

                                SwingUtilities.invokeLater(() -> {
                                    new ClientGui(socket, in, out, isAdmin).setVisible(true);
                                    dispose();
                                });
                                return;
                            }
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi kết nối: " + ex.getMessage());
            }
        }).start();
    }

    // ========================== GIỮ NGUYÊN CÁC HÀM UI CỦA BẠN
    // ==========================

    private void styleRoundedField(JTextField field, String placeholder) {
        field.setFont(getAppFont(15, Font.PLAIN));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setBorder(BorderFactory.createTitledBorder(
                createRoundedLineBorder(),
                placeholder,
                TitledBorder.LEFT, TitledBorder.TOP,
                getAppFont(13, Font.PLAIN),
                new Color(100, 100, 100)));
        field.setBackground(Color.WHITE);
    }

    private JPanel createPasswordFieldPanel(String placeholder) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);
        // Tăng chiều cao lên 60 để đủ chỗ hiển thị
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        fieldPanel.setBorder(BorderFactory.createTitledBorder(
                createRoundedLineBorder(), placeholder,
                TitledBorder.LEFT, TitledBorder.TOP,
                getAppFont(13, Font.PLAIN),
                new Color(100, 100, 100)));

        // Tăng padding để chữ không bị che
        passwordField.setFont(getAppFont(15, Font.PLAIN));
        passwordField.setBorder(new EmptyBorder(5, 10, 5, 5));

        // Custom Eye Icon drawing
        JLabel eyeIcon = new JLabel();
        eyeIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeIcon.setBorder(new EmptyBorder(0, 5, 0, 10));

        // Icon size
        int iconSize = 20;

        // Drawer for icon
        Icon hiddenIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(50, 50, 50));
                g2.setStroke(new BasicStroke(2));
                // Draw eye outline
                g2.drawArc(x, y + 5, 20, 10, 0, 180);
                g2.drawArc(x, y + 5, 20, 10, 180, 180);
                // Draw pupil
                g2.fillOval(x + 7, y + 7, 6, 6);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return iconSize;
            }

            @Override
            public int getIconHeight() {
                return iconSize;
            }
        };

        Icon visibleIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.GRAY);
                g2.setStroke(new BasicStroke(2));
                // Draw eye outline
                g2.drawArc(x, y + 5, 20, 10, 0, 180);
                g2.drawArc(x, y + 5, 20, 10, 180, 180);
                // Draw pupil
                g2.fillOval(x + 7, y + 7, 6, 6);
                // Draw slash
                g2.drawLine(x + 2, y + 18, x + 18, y + 2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return iconSize;
            }

            @Override
            public int getIconHeight() {
                return iconSize;
            }
        };

        eyeIcon.setIcon(hiddenIcon); // Mặc định là ẩn (có gạch chéo)

        eyeIcon.addMouseListener(new MouseAdapter() {
            boolean show = false;

            @Override
            public void mouseClicked(MouseEvent e) {
                show = !show;
                passwordField.setEchoChar(show ? (char) 0 : '•');
                eyeIcon.setIcon(show ? visibleIcon : hiddenIcon);
            }
        });

        fieldPanel.add(passwordField, BorderLayout.CENTER);
        fieldPanel.add(eyeIcon, BorderLayout.EAST);

        return fieldPanel;
    }

    // ======= BUTTON BO TRÒN =======
    class RoundedButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color currentColor;
        private final int arc = 30;

        public RoundedButton(String text, Color base, Color fg) {
            super(text);
            this.baseColor = base;
            this.hoverColor = base.darker();
            this.pressedColor = base.brighter();
            this.currentColor = base;

            setOpaque(false);
            setForeground(fg);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false); // Fix: Remove rectangular border artifact

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    currentColor = pressedColor;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(currentColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
