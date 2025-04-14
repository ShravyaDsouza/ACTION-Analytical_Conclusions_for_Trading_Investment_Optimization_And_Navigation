package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ForgotPassword extends JFrame {

    public ForgotPassword() {
        setTitle("Forgot Password");
        setSize(480, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(98, 0, 238)); // Purple theme

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(340, 350));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(16, 16, 16, 16);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Forgot Password?", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(98, 0, 238));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        // Instruction
        gbc.gridy++;
        JLabel instruction = new JLabel("Enter your registered email or phone", SwingConstants.CENTER);
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(Color.GRAY);
        card.add(instruction, gbc);

        // Email/Phone Icon
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel emailIcon = new JLabel("📨");
        emailIcon.setFont(new Font("SansSerif", Font.PLAIN, 20));
        card.add(emailIcon, gbc);

        // Email/Phone Input
        gbc.gridx = 1;
        JTextField contactField = new JTextField();
        contactField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contactField.setPreferredSize(new Dimension(200, 30));
        contactField.setToolTipText("Email or Phone");
        card.add(contactField, gbc);

        // Reset Button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton resetBtn = new JButton("Send Reset Link");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        resetBtn.setForeground(new Color(98, 0, 238));
        //resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setPreferredSize(new Dimension(260, 40));
        resetBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resetBtn.setBorder(BorderFactory.createLineBorder(new Color(98, 0, 238), 2, true));
        card.add(resetBtn, gbc);

        // Back to login
        gbc.gridy++;
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomPanel.setBackground(Color.WHITE);
        JLabel goBack = new JLabel("Remember your password?");
        goBack.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JLabel loginLink = new JLabel("<HTML><U>Back to Login</U></HTML>");
        loginLink.setForeground(new Color(98, 0, 238));
        loginLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(goBack);
        bottomPanel.add(loginLink);
        card.add(bottomPanel, gbc);

        // Add card to the frame
        getContentPane().add(card);

        // Button logic
        resetBtn.addActionListener(e -> {
    String contact = contactField.getText().trim();
    if (contact.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter your registered email or phone.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT * FROM users WHERE email = ? OR mobile_no = ?"
        );
        stmt.setString(1, contact);
        stmt.setString(2, contact);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            JOptionPane.showMessageDialog(this,
                "A reset link has been sent to: " + contact,
                "Link Sent", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "No account found with that email or phone.",
                "Not Found", JOptionPane.ERROR_MESSAGE);
        }

        rs.close();
        stmt.close();
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Database error occurred.",
            "Error", JOptionPane.ERROR_MESSAGE);
    }
});


        loginLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, "Redirecting to login...");
                dispose();
                new LoginForm();
            }
        });
        setVisible(true);
    }

    public static void main(String[] args) {
        new ForgotPassword();
    }
}
