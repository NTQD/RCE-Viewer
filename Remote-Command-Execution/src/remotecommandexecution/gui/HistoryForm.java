package remotecommandexecution.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class HistoryForm extends JDialog {

    private static final String SEARCH_PLACEHOLDER = "Search Command History...";

    private final JTextField searchField = new JTextField();
    private final JTable historyTable;
    private final DefaultTableModel tableModel;
    private final Consumer<String> searchHandler;
    private final IntConsumer deleteHandler;
    private String lastKeyword = "";

    private Font getAppFont(float size, int style) {
        return new Font("Verdana", style, (int) size);
    }

    private final Color MAIN_BACKGROUND_COLOR = new Color(210, 210, 240);
    private final Color BORDER_COLOR = new Color(180, 180, 200);

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
            this.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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
                    borderThickness / 2.0, borderThickness / 2.0,
                    getWidth() - borderThickness, getHeight() - borderThickness,
                    arcWidth, arcWidth));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public HistoryForm(Frame owner, Consumer<String> searchHandler, IntConsumer deleteHandler) {
        super(owner, "Command History", true);
        this.searchHandler = searchHandler;
        this.deleteHandler = deleteHandler;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(owner);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(MAIN_BACKGROUND_COLOR);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        final String ICON_PATH = "/images/hehenho.gif";
        ImageIcon serverIcon = null;
        try {
            java.net.URL imageUrl = getClass().getResource(ICON_PATH);
            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image originalImage = originalIcon.getImage();
                int targetSize = 32;
                BufferedImage bufferedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = bufferedImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(originalImage, 0, 0, targetSize, targetSize, null);
                g2.dispose();
                serverIcon = new ImageIcon(bufferedImage);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải Icon Server: " + e.getMessage());
        }

        if (serverIcon != null && serverIcon.getIconWidth() > 0) {
            JLabel iconLabel = new JLabel(serverIcon);
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            topPanel.add(iconLabel);
        }

        JLabel titleLabel = new JLabel(" Command History");
        titleLabel.setFont(getAppFont(24, Font.BOLD));
        topPanel.add(titleLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        searchField.setFont(getAppFont(14, Font.PLAIN));
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.setForeground(Color.GRAY);
        final Color ACTIVE_BORDER_COLOR = new Color(70, 70, 150);
        final Color INACTIVE_BORDER_COLOR = new Color(180, 180, 200);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(INACTIVE_BORDER_COLOR, 1),
                new EmptyBorder(5, 5, 5, 5)));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACTIVE_BORDER_COLOR, 2),
                        new EmptyBorder(4, 4, 4, 4)));
                if (searchField.getText().equals(SEARCH_PLACEHOLDER)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(INACTIVE_BORDER_COLOR, 1),
                        new EmptyBorder(5, 5, 5, 5)));
                if (searchField.getText().isEmpty()) {
                    searchField.setText(SEARCH_PLACEHOLDER);
                    searchField.setForeground(Color.GRAY);
                }
            }
        });
        searchField.addActionListener(e -> triggerSearch());
        searchPanel.add(searchField, BorderLayout.CENTER);

        String[] columns = { "ID", "User ID", "Command", "Result" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setFont(getAppFont(13, Font.PLAIN));
        historyTable.setRowHeight(24);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setFont(getAppFont(14, Font.BOLD));
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(new LineBorder(BORDER_COLOR, 1));

        JPanel historyViewPanel = new JPanel(new BorderLayout());
        historyViewPanel.setOpaque(false);
        JLabel historyTitle = new JLabel("Command History:");
        historyTitle.setFont(getAppFont(16, Font.BOLD));
        historyTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        historyViewPanel.add(historyTitle, BorderLayout.NORTH);
        historyViewPanel.add(historyScroll, BorderLayout.CENTER);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(searchPanel, BorderLayout.NORTH);
        centerContainer.add(historyViewPanel, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        JPanel gridButtonPanel = new JPanel(new GridLayout(1, 5, 20, 0));
        gridButtonPanel.setOpaque(false);
        Color buttonBaseColor = Color.WHITE;
        Color buttonBorderColor = new Color(50, 50, 50);

        RoundedButton backButton = new RoundedButton("Back", buttonBaseColor, Color.BLACK, buttonBorderColor);
        backButton.addActionListener(e -> dispose());

        RoundedButton deleteButton = new RoundedButton("Delete", buttonBaseColor, Color.BLACK, buttonBorderColor);
        deleteButton.addActionListener(e -> handleDeleteSelection());
        RoundedButton reloadButton = new RoundedButton("Reload", buttonBaseColor, Color.BLACK, buttonBorderColor);
        reloadButton.addActionListener(e -> {
            searchField.setText(SEARCH_PLACEHOLDER);
            searchField.setForeground(Color.GRAY);
            triggerSearch();
        });

        backButton.setPreferredSize(new Dimension(0, 40));

        gridButtonPanel.add(backButton);
        gridButtonPanel.add(deleteButton);
        gridButtonPanel.add(reloadButton);

        JPanel southContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southContainer.setOpaque(false);
        gridButtonPanel.setPreferredSize(new Dimension(700, 40));
        southContainer.add(gridButtonPanel);
        mainPanel.add(southContainer, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void triggerSearch() {
        if (searchHandler == null) {
            return;
        }
        String text = searchField.getText();
        boolean isPlaceholder = SEARCH_PLACEHOLDER.equals(text);
        String keyword = isPlaceholder ? "" : text.trim();

        lastKeyword = keyword;
        showLoadingMessage(keyword.isEmpty() ? "Đang tải toàn bộ lịch sử..." : "Đang tìm kiếm...");
        searchHandler.accept(keyword);
    }

    private void handleDeleteSelection() {
        if (deleteHandler == null) {
            JOptionPane.showMessageDialog(this, "Không thể xoá lịch sử ở chế độ hiện tại.", "Chức năng không khả dụng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch sử để xoá.", "Chưa chọn bản ghi",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = historyTable.convertRowIndexToModel(selectedRow);
        Object idValue = tableModel.getValueAt(modelRow, 0);
        if (idValue == null) {
            JOptionPane.showMessageDialog(this, "Bản ghi không hợp lệ.", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xoá lịch sử ID " + idValue + "?",
                "Xác nhận xoá",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            showLoadingMessage("Đang xoá lịch sử ID " + idValue + "...");
            deleteHandler.accept(Integer.parseInt(idValue.toString()));
        }
    }

    public void reloadLastSearch() {
        if (searchHandler == null)
            return;
        showLoadingMessage("Đang làm mới dữ liệu...");
        searchHandler.accept(lastKeyword == null ? "" : lastKeyword);
    }

    public void updateHistory(List<String[]> historyRows) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (historyRows == null || historyRows.isEmpty()) {
                setStatusMessage("No history found.");
                return;
            }
            for (String[] row : historyRows) {
                String id = getValue(row, 0);
                String userId = getValue(row, 1);
                String command = getValue(row, 2);
                String result = getValue(row, 3);
                tableModel.addRow(new Object[] { id, userId, command, result });
            }
            historyTable.clearSelection();
        });
    }

    public void showLoadingMessage(String message) {
        SwingUtilities.invokeLater(() -> setStatusMessage(message));
    }

    public void showErrorMessage(String message) {
        SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE));
    }

    private void setStatusMessage(String message) {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[] { "", "", message, "" });
        historyTable.clearSelection();
    }

    private String getValue(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index];
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            HistoryForm form = new HistoryForm(null, keyword -> {
            }, id -> {
            });
            form.updateHistory(java.util.Arrays.asList(
                    new String[] { "1", "5", "dir", "Volume in drive C is OS" },
                    new String[] { "2", "5", "ipconfig", "Windows IP Configuration" }));
            form.setVisible(true);
        });
    }
}