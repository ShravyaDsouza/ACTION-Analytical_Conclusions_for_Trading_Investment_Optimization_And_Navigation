package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


public class SignupPage extends JFrame {

    public SignupPage() {
        setTitle("Sign Up");
        setSize(500, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(98, 0, 238)); // Purple

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(360, 500));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(98, 0, 238));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(userIcon, gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setToolTipText("Username");
        card.add(usernameField, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel emailIcon = new JLabel("📧");
        emailIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(emailIcon, gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 30));
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setToolTipText("Email");
        card.add(emailField, gbc);

        // Phone
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel phoneIcon = new JLabel("📱");
        phoneIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(phoneIcon, gbc);

        gbc.gridx = 1;
        JTextField phoneField = new JTextField();
        phoneField.setPreferredSize(new Dimension(200, 30));
        phoneField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        phoneField.setToolTipText("Phone Number");
        card.add(phoneField, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passIcon = new JLabel("🔒");
        passIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(passIcon, gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setToolTipText("Password");
        card.add(passwordField, gbc);

        // Sign Up Button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton signupButton = new JButton("Sign Up");
        signupButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        signupButton.setForeground(new Color(98, 0, 238));
        //signupButton.setForeground(Color.WHITE);
        signupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupButton.setFocusPainted(false);
        signupButton.setPreferredSize(new Dimension(260, 40));
        signupButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        signupButton.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        card.add(signupButton, gbc);

        // Footer - Already have an account?
        gbc.gridy++;
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setBackground(Color.WHITE);
        JLabel haveAccount = new JLabel("Already have an account?");
        haveAccount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel loginNow = new JLabel("<HTML><U>Login now</U></HTML>");
        loginNow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginNow.setForeground(new Color(98, 0, 238));
        loginNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(haveAccount);
        bottomPanel.add(loginNow);
        card.add(bottomPanel, gbc);

        // Add the card to the frame
        getContentPane().add(card);

        // Button logic
        signupButton.addActionListener(e -> {
    String username = usernameField.getText().trim();
    String email = emailField.getText().trim();
    String phone = phoneField.getText().trim();
    String pass = new String(passwordField.getPassword()).trim();

    if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate email format
    if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
        JOptionPane.showMessageDialog(this, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate phone number format
    if (!phone.matches("^[1-9]\\d{9}$")) {
        JOptionPane.showMessageDialog(this, "Phone number must be 10 digits and not start with 0.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {

        // === Duplicate email or phone check ===
        PreparedStatement checkStmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM users WHERE email = ? OR mobile_no = ?");
        checkStmt.setString(1, email);
        checkStmt.setString(2, phone);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) {
            JOptionPane.showMessageDialog(this, "Email or phone already registered.", "Duplicate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // === Hash the password using SHA-256 ===
        String hashedPassword = hashPassword(pass);

        // === Insert new user ===
        PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO users (name, email, mobile_no, password_hash) VALUES (?, ?, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, email);
        stmt.setString(3, phone);
        stmt.setString(4, hashedPassword);
        stmt.executeUpdate();

        JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        stmt.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error during signup.", "Error", JOptionPane.ERROR_MESSAGE);
    }
});

        loginNow.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Redirecting to login...");
                dispose();
                new LoginForm();
            }
        });
        setVisible(true);
    }

    private String hashPassword(String password) {
    try {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}
    public static void main(String[] args) {
        new SignupPage();
    }
}
