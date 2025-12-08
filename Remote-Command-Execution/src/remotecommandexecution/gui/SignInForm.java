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


public class SignInForm extends JFrame {

    private JTextField fullnameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private Font getAppFont(float size, int style) {
        return new Font("Verdana", style, (int) size);
    }

    private Border createRoundedLineBorder() {
        return new LineBorder(new Color(150, 150, 170), 2, true); 
    }

    public SignInForm() {
        setTitle("Remote Command Execution - Team 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 650);  
        setLocationRelativeTo(null);
        setResizable(true);
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 0, 0));
        add(mainPanel);
        
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(218, 222, 245)); 
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(80, 50, 80, 50));
        leftPanel.add(Box.createVerticalGlue());
        
        JPanel textWrapper = new JPanel();
        textWrapper.setOpaque(false);
        textWrapper.setLayout(new BoxLayout(textWrapper, BoxLayout.Y_AXIS));
        
        textWrapper.setAlignmentX(Component.LEFT_ALIGNMENT); 
        
        JLabel teamLabel = new JLabel("Team 1");
        teamLabel.setFont(getAppFont(18, Font.PLAIN));
        teamLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Chữ căn trái

        JLabel titleLabel = new JLabel("Remote Command Execution");
        titleLabel.setFont(getAppFont(24, Font.BOLD));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Chữ căn trái
        
        textWrapper.add(teamLabel);
        textWrapper.add(Box.createVerticalStrut(10));
        textWrapper.add(titleLabel);

        JLabel imageLabel;
        try {
            java.net.URL imageUrl = getClass().getResource("/remotecommandexecution/images/hehenho.gif");
            if (imageUrl != null) {
                ImageIcon icon = new ImageIcon(imageUrl);
                imageLabel = new JLabel(icon);
            } else {
                imageLabel = new JLabel("Image Not Found");
                imageLabel.setFont(getAppFont(14, Font.BOLD));
                imageLabel.setForeground(Color.RED);
            }
        } catch (Exception e) {
            imageLabel = new JLabel("Error loading image");
        }

        JPanel imageWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imageWrapper.setOpaque(false);
        imageWrapper.setAlignmentX(Component.LEFT_ALIGNMENT); 
        imageWrapper.add(imageLabel);

        leftPanel.add(textWrapper); 
        leftPanel.add(Box.createVerticalStrut(60)); 
        leftPanel.add(imageWrapper); 
        leftPanel.add(Box.createVerticalStrut(30));

        leftPanel.add(Box.createVerticalGlue());
        mainPanel.add(leftPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(70, 80, 70, 80));  
        rightPanel.setBackground(Color.WHITE);

        JLabel loginLabel = new JLabel("SIGN IN");
        loginLabel.setFont(getAppFont(22, Font.BOLD));
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  

        fullnameField = new JTextField();
        styleRoundedField(fullnameField, "Full name");
        emailField = new JTextField();
        styleRoundedField(emailField, "Email");
        phoneField = new JTextField();
        styleRoundedField(phoneField, "Phone");
        usernameField = new JTextField();
        styleRoundedField(usernameField, "User name");
        
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        JPanel passwordPanel = createPasswordFieldPanel(passwordField, "Password");
        JPanel comfirmpasswordPanel = createPasswordFieldPanel(confirmPasswordField, "Confirm password");

        Color nextBaseColor = Color.decode("#0B0B45");
        RoundedButton nextButton = new RoundedButton("NEXT", nextBaseColor, Color.WHITE);
        nextButton.setFont(getAppFont(16, Font.BOLD));
        nextButton.setMaximumSize(new Dimension(280, 48));  
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);  
        nextButton.addActionListener(e -> performRegistration());

        RoundedButton backButton = new RoundedButton("Back to Login", new Color(230, 230, 230), Color.BLACK);
        backButton.setFont(getAppFont(16, Font.BOLD));
        backButton.setMaximumSize(new Dimension(280, 48));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        
        rightPanel.add(loginLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(fullnameField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(emailField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(phoneField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(usernameField);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(comfirmpasswordPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(passwordPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(rightPanel);
    }
    
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

    private void performRegistration() {
        String fullName = fullnameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 12345);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                String serverResponse = in.readLine();
                if (!"SERVER_READY".equals(serverResponse)) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(this, "Server not ready or invalid protocol.", "Connection Error", JOptionPane.ERROR_MESSAGE)
                    );
                    return;
                }
                
                String command = String.format("REGISTER %s %s %s %s", username, password, fullName, email);
                out.println(command);
                
                serverResponse = in.readLine();

                if (serverResponse == null) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "No response from server. Connection may have been closed.", "Error", JOptionPane.ERROR_MESSAGE)
                    );
                    return;
                }

                if ("REGISTER_OK".equals(serverResponse)) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); 
                    });
                } else {
                    final String finalResponse = serverResponse;
                    String userMessage;
                    if (finalResponse.startsWith("REGISTER_FAILED_USER_EXISTS")) {
                        userMessage = "User already exists.";
                    } else if (finalResponse.startsWith("REGISTER_FAILED_INVALID_COMMAND")) {
                        userMessage = "Invalid registration command sent to server.";
                    } else if (finalResponse.startsWith("REGISTER_FAILED_DB_ERROR")) {
                        userMessage = "Server database error while registering.";
                    } else {
                        userMessage = "Registration failed: " + finalResponse;
                    }
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(this, userMessage, "Error", JOptionPane.ERROR_MESSAGE)
                    );
                }

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "Could not connect to server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private JPanel createPasswordFieldPanel(JPasswordField inputField, String placeholder) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                createRoundedLineBorder(), 
                placeholder,
                TitledBorder.LEFT, TitledBorder.TOP, 
                getAppFont(13, Font.PLAIN), 
                new Color(100, 100, 100));
        fieldPanel.setBorder(titledBorder);

        inputField.setFont(getAppFont(15, Font.PLAIN));
        inputField.setBackground(Color.WHITE);
        inputField.setBorder(new EmptyBorder(10, 5, 5, 5)); 
    
        final char defaultEchoChar = inputField.getEchoChar();

        JLabel eyeIcon = new JLabel("\uD83D\uDDA5 ");
        eyeIcon.setFont(getAppFont(20, Font.PLAIN));
        eyeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eyeIcon.setForeground(new Color(50, 50, 50)); 
    
        eyeIcon.addMouseListener(new MouseAdapter() {
            boolean isShowing = false;
            @Override
            public void mouseClicked(MouseEvent e) {
                isShowing = !isShowing;
                if (isShowing) {
                    inputField.setEchoChar((char) 0);
                    eyeIcon.setText(" \uD83D\uDDA5 ");
                } else {
                    inputField.setEchoChar(defaultEchoChar);
                    eyeIcon.setText(" \uD83D\uDDA5 ");
                }
            }
        });

        JPanel eyePanel = new JPanel(new BorderLayout());
        eyePanel.setOpaque(false);
        eyePanel.add(eyeIcon, BorderLayout.CENTER);
    
        fieldPanel.add(inputField, BorderLayout.CENTER);
        fieldPanel.add(eyePanel, BorderLayout.EAST);

        return fieldPanel;
    }

    class RoundedButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color currentColor;
        private final int arcWidth = 30;  

        public RoundedButton(String text, Color base, Color fg) {
            super(text);
            this.baseColor = base;
            this.setForeground(fg);
            
            if (base == Color.WHITE) {
                this.hoverColor = new Color(240, 240, 240);
                this.pressedColor = new Color(220, 220, 220);
            } else {
                this.hoverColor = base.darker().darker();  
                this.pressedColor = base.brighter();
            }
            
            this.currentColor = baseColor;
            
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            this.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  

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

            Border border = getBorder();
            if (border instanceof LineBorder) {
                LineBorder lb = (LineBorder) border;
                if (currentColor.equals(baseColor)) { 
                    g2.setColor(lb.getLineColor());
                    g2.setStroke(new BasicStroke(lb.getThickness()));  
                    g2.draw(new RoundRectangle2D.Double(
                        lb.getThickness() / 2.0, lb.getThickness() / 2.0,  
                        getWidth() - lb.getThickness(), getHeight() - lb.getThickness(),  
                        arcWidth, arcWidth
                    ));
                }
            }

            super.paintComponent(g2);
            g2.dispose();
        }
    }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }  
        catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new SignInForm().setVisible(true));
    }
}
