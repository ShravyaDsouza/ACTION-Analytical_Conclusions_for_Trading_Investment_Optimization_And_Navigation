package components.dashboard.sections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import components.dashboard.StockDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortfolioTable extends JPanel {
    private int hoveredRow = -1;
    private final int userId;
    private final DecimalFormat df = new DecimalFormat("#0.00");
    private JTable table;
    private DefaultTableModel model;
    private StockDashboard dashboard;

    private final String[] columns = {
        "Company", "Last Price", "Change", "Your Equity",
        "Today's Return", "Total Return", "5-Day Chart"
    };

    public PortfolioTable(int userId, StockDashboard dashboard) {
        this.userId = userId;
        this.dashboard = dashboard;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(12, 15, 4, 15));
        JLabel title = new JLabel("Your Holdings");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(98, 0, 238));
        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(new JSeparator(), BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(fetchPortfolioData(), columns) {
            public Class<?> getColumnClass(int column) {
                return column == 6 ? JPanel.class : String.class;
            }
        };

        table = createStyledTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        // === Action Buttons ===
JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
actionPanel.setBackground(Color.WHITE);

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

actionPanel.add(addButton);
actionPanel.add(removeButton);

add(actionPanel, BorderLayout.SOUTH);

    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (col == 6 && c instanceof JPanel) return (JPanel) c;
                if (c instanceof JLabel lbl) {
                    lbl.setOpaque(true);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
                    String val = getValueAt(row, col).toString();
                    if (col == 2 || col == 4 || col == 5) {
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
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl.setForeground(new Color(98, 0, 238));
                lbl.setBackground(new Color(245, 245, 255));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 255)));
                return lbl;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setHeaderRenderer(styledHeader);

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

    private Object[][] fetchPortfolioData() {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT stock_symbol, shares_owned, average_price FROM holdings WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            int rowIndex = 0;
            while (rs.next()) {
                String symbol = rs.getString("stock_symbol");
                double shares = rs.getDouble("shares_owned");
                double avgPrice = rs.getDouble("average_price");

                double lastPrice = getLatestPrice(symbol);
                double priceChange = getPriceChange(symbol);
                double equity = shares * lastPrice;
                double todaysReturn = shares * priceChange;
                double totalReturn = equity - (shares * avgPrice);
                JPanel miniChart = generateMiniChart(symbol, rowIndex, priceChange);

                rows.add(new Object[]{
                        symbol,
                        "$" + df.format(lastPrice),
                        (priceChange >= 0 ? "+" : "") + df.format(priceChange),
                        "$" + df.format(equity),
                        (todaysReturn >= 0 ? "+" : "") + df.format(todaysReturn),
                        (totalReturn >= 0 ? "+" : "") + df.format(totalReturn),
                        miniChart
                });
                rowIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows.toArray(new Object[0][]);
    }

    private double getLatestPrice(String symbol) {
        try {
            ProcessBuilder pb = new ProcessBuilder("/Users/shravyadsouza/.virtualenvs/Stock/bin/python", "src/services/stocks_api.py", symbol, "1D");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String json = br.readLine();
            p.waitFor();

            if (json != null && json.startsWith("[")) {
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                if (arr.size() > 0) {
                    JsonObject last = arr.get(arr.size() - 1).getAsJsonObject();
                    return last.get("close").getAsDouble();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
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
