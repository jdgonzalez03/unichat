package UI;

import utils.Logger;
import config.ConfigLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerUI extends JFrame {
    private JLabel userCountLabel;
    private JLabel channelCountLabel;
    private JLabel ipLabel;
    private JLabel portLabel;
    private JLabel serverNameLabel;
    private JTextArea logsTextArea;

    private int userCount = 0;
    private int channelCount = 0;
    private final int PORT = 12345;

    private final Logger logger = Logger.getInstance();
    private final ConfigLoader config = ConfigLoader.getInstance();

    public ServerUI() {
        super("UniChat Server");

        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setResizable(false);

        createStatsPanel();
        createLogsPanel();
        redirectSystemStreams();

        getContentPane().setBackground(new Color(47, 49, 54)); // fondo general estilo Discord

        setVisible(true);
    }

    private void createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(2, 3, 10, 10));
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        statsPanel.setBackground(new Color(47, 49, 54)); // Fondo estilo Discord

        Font titleFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font valueFont = new Font("Segoe UI", Font.BOLD, 24);

        statsPanel.add(createStatBox("Users Online", "0", titleFont, valueFont, label -> userCountLabel = label));
        statsPanel.add(createStatBox("Active Channels", "0", titleFont, valueFont, label -> channelCountLabel = label));
        statsPanel.add(createStatBox("Server Status", "Running", titleFont, valueFont, label -> {
            label.setForeground(new Color(67, 181, 129)); // verde estilo Discord
        }));

        String ipAddress = "Unknown";
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "Error";
        }

        statsPanel.add(createStatBox("IPv4 Address", ipAddress, titleFont, valueFont, label -> ipLabel = label));
        statsPanel.add(createStatBox("Port", String.valueOf(PORT), titleFont, valueFont, label -> portLabel = label));

        String serverName = config.getServerName();
        statsPanel.add(createStatBox("Server name", serverName, titleFont, valueFont, label -> serverNameLabel =label )); // Empty to align grid

        add(statsPanel, BorderLayout.NORTH);
    }

    private JPanel createStatBox(String title, String value, Font titleFont, Font valueFont, java.util.function.Consumer<JLabel> labelConsumer) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(64, 68, 75), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(54, 57, 63)); // Discord card color
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(Color.LIGHT_GRAY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueFont);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(valueLabel);

        labelConsumer.accept(valueLabel);
        return panel;
    }

    private void createLogsPanel() {
        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        logsPanel.setBackground(new Color(47, 49, 54));

        JLabel logsHeader = new JLabel("Server Logs");
        logsHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logsHeader.setForeground(Color.WHITE);
        logsHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

        logsTextArea = new JTextArea();
        logsTextArea.setEditable(false);
        logsTextArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logsTextArea.setMargin(new Insets(10, 10, 10, 10));
        logsTextArea.setBackground(new Color(32, 34, 37));
        logsTextArea.setForeground(Color.LIGHT_GRAY);
        logsTextArea.setCaretColor(Color.WHITE);

        DefaultCaret caret = (DefaultCaret) logsTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(logsTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(64, 68, 75)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(47, 49, 54));

        JButton clearButton = new JButton("Clear Logs");
        JButton exportButton = new JButton("Export Logs");

        styleButton(clearButton);
        styleButton(exportButton);

        clearButton.addActionListener(e -> logsTextArea.setText(""));
        exportButton.addActionListener(e -> exportLogs());

        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);

        logsPanel.add(logsHeader, BorderLayout.NORTH);
        logsPanel.add(scrollPane, BorderLayout.CENTER);
        logsPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(logsPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(88, 101, 242)); // Discord blurple
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Server Logs");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                fw.write(logsTextArea.getText());
                JOptionPane.showMessageDialog(this, "Logs exported successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        PrintStream printStream = new PrintStream(out, true);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> logsTextArea.append(text));
    }

    public void updateUserCount(int count) {
        userCount = count;
        SwingUtilities.invokeLater(() -> userCountLabel.setText(String.valueOf(count)));
    }

    public void updateChannelCount(int count) {
        channelCount = count;
        SwingUtilities.invokeLater(() -> channelCountLabel.setText(String.valueOf(count)));
    }

    public void incrementUserCount() {
        updateUserCount(++userCount);
    }

    public void decrementUserCount() {
        updateUserCount(--userCount);
    }

    public void incrementChannelCount() {
        updateChannelCount(++channelCount);
    }

    public void decrementChannelCount() {
        updateChannelCount(--channelCount);
    }

    public void addLogMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s%n", timestamp, message);
        SwingUtilities.invokeLater(() -> logsTextArea.append(formattedMessage));
    }
}