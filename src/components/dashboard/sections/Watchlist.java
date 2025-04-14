package components.dashboard.sections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.gson.*;

public class Watchlist extends JPanel {
    private int hoveredRow = -1;
    private JTable table;
    private DefaultTableModel model;
    private final int userId;

    public Watchlist(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // === Header ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(12, 15, 4, 15));
        JLabel title = new JLabel("Your Watchlist");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(98, 0, 238));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(new JSeparator(), BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"Company", "Last Price", "Change", "5-Day Chart"};
        model = new DefaultTableModel(generateWatchlistRows(), columns);
        table = createStyledTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        JButton addButton = new JButton("➕");
        JButton removeButton = new JButton("🗑");

        addButton.setBackground(new Color(98, 0, 238));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);

        removeButton.setBackground(Color.RED);
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.setOpaque(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        tableWrapper.setPreferredSize(new Dimension(0, 200));
        add(tableWrapper, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            JTextField symbolField = new JTextField();
            JTextField companyField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Stock Symbol:"));
            panel.add(symbolField);
            panel.add(new JLabel("Company Name:"));
            panel.add(companyField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Add to Watchlist", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String symbol = symbolField.getText().trim().toUpperCase();
                    String company = companyField.getText().trim();
                    if (symbol.isEmpty() || company.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
                        return;
                    }

                    String[] test = fetchLatestPriceFromAPI(symbol);
                    if (test[0].equals("$0.00")) {
                        JOptionPane.showMessageDialog(this, "Invalid or unrecognized stock symbol.");
                        return;
                    }

                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
                        PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM watchlist WHERE user_id = ? AND stock_symbol = ?");
                        checkStmt.setInt(1, userId);
                        checkStmt.setString(2, symbol);
                        ResultSet rs = checkStmt.executeQuery();
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this, "Company already exists in your watchlist.");
                            return;
                        }
                        checkStmt.close();

                        PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO watchlist (user_id, stock_symbol, company_name) VALUES (?, ?, ?)");
                        stmt.setInt(1, userId);
                        stmt.setString(2, symbol);
                        stmt.setString(3, company);
                        stmt.executeUpdate();
                        stmt.close();

                        refreshTable();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error adding to watchlist.");
                    }
                } catch (Exception outerEx) {
                    outerEx.printStackTrace();
                }
            }
        });

        removeButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String symbol = model.getValueAt(row, 0).toString();
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM watchlist WHERE user_id = ? AND stock_symbol = ?");
                    stmt.setInt(1, userId);
                    stmt.setString(2, symbol);
                    stmt.executeUpdate();
                    stmt.close();
                    refreshTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error removing from watchlist.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a row to remove.");
            }
        });
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (c instanceof JLabel lbl) {
                    lbl.setOpaque(true);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    String val = getValueAt(row, col).toString();
                    if (col == 2) {
                        lbl.setForeground(val.startsWith("+") ? new Color(0, 153, 0) :
                                val.startsWith("-") ? Color.RED : Color.BLACK);
                    } else {
                        lbl.setForeground(Color.BLACK);
                    }
                    lbl.setBackground(row == hoveredRow ? new Color(98, 0, 238, 30) :
                            row % 2 == 0 ? Color.WHITE : new Color(248, 248, 255));
                }
                return c;
            }
        };

        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer styledHeader = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                lbl.setForeground(new Color(98, 0, 238));
                lbl.setBackground(new Color(245, 245, 255));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 255)));
                return lbl;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(styledHeader);
        }

        table.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        table.getColumn("5-Day Chart").setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
        if (value instanceof JPanel panel) {
            panel.setBackground(isSelected ? new Color(220, 220, 255) : Color.WHITE);
            return panel;
        }
        return new JLabel("N/A");
    });
        return table;
    }

    private void refreshTable() {
        model.setDataVector(generateWatchlistRows(), new String[]{"Symbol", "Last Price", "Change", "5-Day Chart"});
        // Reapply header style
        DefaultTableCellRenderer styledHeader = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
                lbl.setForeground(new Color(98, 0, 238));
                lbl.setBackground(new Color(245, 245, 255));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 255)));
                return lbl;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(styledHeader);
        }
         table.getColumn("5-Day Chart").setCellRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
        if (value instanceof JPanel panel) {
            panel.setBackground(isSelected ? new Color(220, 220, 255) : Color.WHITE);
            return panel;
        }
        return new JLabel("N/A");
    });
    }

    private Object[][] generateWatchlistRows() {
    List<Object[]> rows = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
        PreparedStatement stmt = conn.prepareStatement("SELECT stock_symbol FROM watchlist WHERE user_id = ?");
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String symbol = rs.getString("stock_symbol");
            String[] priceChange = fetchLatestPriceFromAPI(symbol);

            double changeValue = Double.parseDouble(priceChange[1].replace("+", "").replace("-", ""));
            double signedChange = priceChange[1].startsWith("-") ? -changeValue : changeValue;

            JPanel chartPanel = generateMiniChart(symbol, rows.size(), signedChange);

            rows.add(new Object[]{
                symbol,
                priceChange[0],
                priceChange[1],
                chartPanel
            });
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return rows.toArray(new Object[0][]);
}


    private String[] fetchLatestPriceFromAPI(String symbol) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
                    "/Users/shravyadsouza/IdeaProjects/Stock/src/services/stocks_api.py", symbol, "1D"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String json = br.readLine();
            p.waitFor();

            if (json != null && json.startsWith("[")) {
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                if (arr.size() >= 2) {
                    JsonObject first = arr.get(0).getAsJsonObject();
                    JsonObject last = arr.get(arr.size() - 1).getAsJsonObject();

                    double firstClose = first.get("close").getAsDouble();
                    double lastClose = last.get("close").getAsDouble();
                    double change = lastClose - firstClose;

                    return new String[]{
                            "$" + String.format("%.2f", lastClose),
                            (change >= 0 ? "+" : "") + String.format("%.2f", change)
                    };
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{"$0.00", "0.00"};
    }
    private double getPriceChange(String symbol) {
        try {
            ProcessBuilder pb = new ProcessBuilder("/Users/shravyadsouza/.virtualenvs/Stock/bin/python", "src/services/stocks_api.py", symbol, "1D");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String json = br.readLine();
            p.waitFor();

            if (json != null && json.startsWith("[")) {
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                if (arr.size() >= 2) {
                    double latest = arr.get(arr.size() - 1).getAsJsonObject().get("close").getAsDouble();
                    double previous = arr.get(arr.size() - 2).getAsJsonObject().get("close").getAsDouble();
                    return latest - previous;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }

    private JPanel generateMiniChart(String symbol, int rowIndex, double priceChange) {
    List<Double> closes = get5DayClosePrices(symbol);

    JPanel chartPanel = new JPanel() {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (closes.size() < 2) return;

            Graphics2D g2d = (Graphics2D) g;

            // Determine gain/loss color
            Color lineColor = closes.get(closes.size() - 1) >= closes.get(0) ? new Color(0, 153, 0) : Color.RED;
            g2d.setColor(lineColor);
            g2d.setStroke(new BasicStroke(2.0f));

            double max = Collections.max(closes);
            double min = Collections.min(closes);
            int w = getWidth() - 10;
            int h = getHeight() - 10;

            for (int i = 0; i < closes.size() - 1; i++) {
                int x1 = (int) (i * (w / (double)(closes.size() - 1))) + 5;
                int x2 = (int) ((i + 1) * (w / (double)(closes.size() - 1))) + 5;
                int y1 = (int) (h - (closes.get(i) - min) / (max - min) * h) + 5;
                int y2 = (int) (h - (closes.get(i + 1) - min) / (max - min) * h) + 5;
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    };

    Color bgColor = rowIndex % 2 == 0 ? Color.WHITE : new Color(248, 248, 255);
    chartPanel.setBackground(bgColor);
    chartPanel.setPreferredSize(new Dimension(80, 24));
    return chartPanel;
}

    private List<Double> get5DayClosePrices(String symbol) {
        List<Double> prices = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("/Users/shravyadsouza/.virtualenvs/Stock/bin/python", "src/services/stocks_api.py", symbol, "5D");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String json = br.readLine();
            p.waitFor();

            if (json != null && json.startsWith("[")) {
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    prices.add(obj.get("close").getAsDouble());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }
}
