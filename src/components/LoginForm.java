package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import components.dashboard.StockDashboard;

public class LoginForm extends JFrame {

    public LoginForm() {
        setTitle("Login");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(98, 0, 238)); // Purple

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 420));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Login Form", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(98, 0, 238));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(userIcon, gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField();
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setPreferredSize(new Dimension(200, 30));
        emailField.setToolTipText("Email or Phone");
        card.add(emailField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passIcon = new JLabel("🔒");
        passIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(passIcon, gbc);

        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField();
        passField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passField.setPreferredSize(new Dimension(200, 30));
        passField.setToolTipText("Password");
        card.add(passField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 12, 12, 12);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel forgot = new JLabel("<HTML><U>Forgot password?</U></HTML>");
        forgot.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgot.setForeground(new Color(98, 0, 238));
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Wrap in FlowLayout panel
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        forgotPanel.setBackground(Color.WHITE); // match card background
        forgotPanel.add(forgot);

        card.add(forgotPanel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Login");
        loginBtn.setContentAreaFilled(false);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setForeground(new Color(98, 0, 238));
        //loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginBtn.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        card.add(loginBtn, gbc);

        gbc.gridy++;
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setBackground(Color.WHITE);
        JLabel notMember = new JLabel("Not a member?");
        notMember.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel signupNow = new JLabel("<HTML><U>Signup now</U></HTML>");
        signupNow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        signupNow.setForeground(new Color(98, 0, 238));
        signupNow.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(notMember);
        bottomPanel.add(signupNow);
        card.add(bottomPanel, gbc);

        getContentPane().add(card);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004");
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
                    stmt.setString(1, email);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        String enteredHash = hashPassword(pass); // 👈 you need this method

                        if (enteredHash.equals(storedHash)) {
                            int userId = rs.getInt("user_id");
                            String username = rs.getString("name");
                            JOptionPane.showMessageDialog(this, "Welcome, " + username + "!", "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                            this.dispose();
                            new StockDashboard(userId);
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    rs.close();
                    stmt.close();
                    conn.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Database error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
            }
        });

        forgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Redirecting to forgot password...");
                new ForgotPassword();
            }
        });

        signupNow.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Redirecting to signup...");
                new SignupPage();
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
        new LoginForm();
       // SwingUtilities.invokeLater(LoginForm::new);
    }
}