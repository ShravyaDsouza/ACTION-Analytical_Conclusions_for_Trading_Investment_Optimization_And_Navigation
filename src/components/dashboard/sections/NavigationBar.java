package components.dashboard.sections;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class NavigationBar extends JPanel {

    private final int userId;
    private JLabel userName;

    public NavigationBar(int userId) {
        this.userId = userId;

        setLayout(new BorderLayout());
        setBackground(new Color(98, 0, 238));
        setPreferredSize(new Dimension(1200, 60));

        initComponents();       // Initialize GUI
        loadUserName();         // Fetch name from DB
    }

    private void initComponents() {
        // === Left: Navigation Buttons ===
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        navPanel.setOpaque(false);

        JButton titleBtn = new JButton("ACTION");
        JButton dashboardBtn = new JButton("Dashboard");
        JButton tradeBtn = new JButton("Trade");
        JButton accountsBtn = new JButton("Accounts");
        JButton newToInvestingBtn = new JButton("New to Investing?");

        styleNavButton(titleBtn);
        styleNavButton(dashboardBtn);
        styleNavButton(tradeBtn);
        styleNavButton(accountsBtn);
        styleNavButton(newToInvestingBtn);

        navPanel.add(titleBtn);
        navPanel.add(dashboardBtn);
        navPanel.add(tradeBtn);
        navPanel.add(accountsBtn);
        navPanel.add(newToInvestingBtn);
        add(navPanel, BorderLayout.LINE_START);

        // === Center: Search ===
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 15));
        searchPanel.setOpaque(false);

        PlaceholderTextField searchField = new PlaceholderTextField("Search...");
        searchField.setPreferredSize(new Dimension(250, 30));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.CENTER);

        // === Right: User Profile ===
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        profilePanel.setOpaque(false);

        userName = new JLabel("Loading...");
        userName.setForeground(Color.WHITE);
        userName.setFont(new Font("SansSerif", Font.BOLD, 14));

        ImageIcon originalIcon = new ImageIcon("src/static/login.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JLabel profileImage = new JLabel(new ImageIcon(scaledImage));

        profilePanel.add(userName);
        profilePanel.add(profileImage);
        add(profilePanel, BorderLayout.LINE_END);
    }

    private void loadUserName() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name FROM users WHERE user_id = ?"
            );
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                userName.setText(name);  // 🟢 Update the label dynamically
            } else {
                userName.setText("User");
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            userName.setText("User");
        }
    }

    private void styleNavButton(JButton button) {
        if (button.getText().equals("ACTION")) {
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Helvetica", Font.BOLD, 22));
            button.setBackground(new Color(150, 0, 250));
        } else {
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Helvetica", Font.PLAIN, 14));
        }

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false); // Transparent background
    }
}
