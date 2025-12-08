package remotecommandexecution.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CommandAdmin extends JDialog {

    private final PrintWriter out;
    private final DefaultTableModel tableModel;
    private final JTable commandTable;
    private final JTextField commandField = new JTextField(25);
    
    private final Font APP_FONT_BOLD = new Font("Verdana", Font.BOLD, 14);
    private final Font APP_FONT_PLAIN = new Font("Verdana", Font.PLAIN, 12);
    private final Color MAIN_BACKGROUND_COLOR = new Color(210, 210, 240);

    public CommandAdmin(Frame owner, PrintWriter out) {
        super(owner, "Admin - Command Management", true);
        this.out = out;
        
        setSize(900, 650);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(MAIN_BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Allowed Commands Management");
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // --- Table Setup ---
        String[] columnNames = {"ID", "Command Text", "Is Active"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        commandTable = new JTable(tableModel);
        commandTable.setFont(APP_FONT_PLAIN);
        commandTable.setRowHeight(25);
        commandTable.getTableHeader().setFont(APP_FONT_BOLD);
        JScrollPane scrollPane = new JScrollPane(commandTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Panel for Controls ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("New Command:"));
        inputPanel.add(commandField);
        JButton addButton = new JButton("Add");
        inputPanel.add(addButton);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        JButton deleteButton = new JButton("Delete Selected");
        JButton closeButton = new JButton("Close");
        actionPanel.add(deleteButton);
        actionPanel.add(closeButton);
        
        bottomPanel.add(inputPanel, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addCommand());
        deleteButton.addActionListener(e -> deleteCommand());
        closeButton.addActionListener(e -> dispose());
        
        // --- Window Listener to load data on open ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                loadCommands();
            }
        });
    }

    public void loadCommands() {
        out.println("GET_ALLOW_COMMANDS");
    }

    private void addCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Command cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        out.println("ADD_ALLOW:" + command);
        commandField.setText("");
    }

    private void deleteCommand() {
        int selectedRow = commandTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a command to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String commandId = (String) tableModel.getValueAt(selectedRow, 0);
        out.println("DELETE_ALLOW:" + commandId);
    }
    
    public void refreshTable(List<String> commandRows) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // Clear existing data
            for (String rowData : commandRows) {
                String[] parts = rowData.split("\\|", 3);
                if (parts.length >= 3) {
                    Vector<String> row = new Vector<>();
                    row.add(parts[0]);
                    row.add(parts[1]);
                    row.add(parts[2]);
                    tableModel.addRow(row);
                }
            }
        });
    }
    
    public void showDuplicateError() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this, 
                "Lệnh này đã tồn tại trong Database. Vui lòng chọn lệnh khác.", 
                "Lỗi: Lệnh trùng lặp", 
                JOptionPane.WARNING_MESSAGE
            );
        });
    }
}
