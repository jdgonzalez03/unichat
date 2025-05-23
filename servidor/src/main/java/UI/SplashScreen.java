package UI;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    public SplashScreen() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(54, 57, 63)); // Discord dark background
        panel.setBorder(BorderFactory.createLineBorder(new Color(114, 137, 218), 3)); // Discord purple

        JLabel loadingLabel = new JLabel("Cargando UniChat Server...");
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(114, 137, 218));
        progressBar.setBackground(new Color(64, 68, 75));
        progressBar.setBorderPainted(false);

        panel.setLayout(new BorderLayout(10, 10));
        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        getContentPane().add(panel);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
}