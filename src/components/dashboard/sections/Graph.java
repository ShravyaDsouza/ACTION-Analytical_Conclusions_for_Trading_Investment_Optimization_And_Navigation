package components.dashboard.sections;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.time.Month;
import org.jfree.data.xy.XYDataset;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.List;

import com.google.gson.*;

public class Graph extends JPanel {
    private ChartPanel chartPanel;
    private JComboBox<String> stockSelector;
    private String currentStockSymbol = "AAPL";
    private JLabel portfolioVal;
    private JLabel todaysGain;
    private JLabel totalGain;
    private final int userId;

    public Graph(int userId) {
        this.userId = userId;
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));

        portfolioVal = new JLabel();
        todaysGain = new JLabel();
        totalGain = new JLabel();

        statsPanel.add(portfolioVal);
        statsPanel.add(todaysGain);
        statsPanel.add(totalGain);

        JPanel rangePanel = new JPanel(new BorderLayout());
        rangePanel.setBackground(Color.WHITE);
        rangePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        String[] ranges = {"1D", "1M", "3M", "1Y", "2Y", "5Y"};
        List<JButton> rangeButtons = new ArrayList<>();

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftPanel.setBackground(Color.WHITE);

        JButton refreshButton = new JButton("⟳");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(28, 28));
        refreshButton.setBackground(new Color(240, 240, 255));
        refreshButton.setForeground(new Color(98, 0, 238));
        refreshButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 255), 1, true));
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshButton.setToolTipText("Refresh Stock Data");

        refreshButton.addActionListener(e -> {
            String currentRange = rangeButtons.stream()
                    .filter(btn -> btn.getFont().isBold())
                    .findFirst()
                    .map(AbstractButton::getText)
                    .orElse("1Y");
            chartPanel.setChart(createChart(currentRange));
            updatePortfolioMetrics(userId);
        });

        String[] stockOptions = {"AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META", "IBM", "TSM", "UNH", "JNJ", "V", "XOM", "WMT", "PG"};
        stockSelector = new JComboBox<>(stockOptions);
        stockSelector.setSelectedItem(currentStockSymbol);
        stockSelector.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stockSelector.setFocusable(false);
        stockSelector.setBackground(Color.WHITE);
        stockSelector.setForeground(new Color(60, 60, 60));

        stockSelector.addActionListener(e -> {
            currentStockSymbol = stockSelector.getSelectedItem().toString();
            String currentRange = rangeButtons.stream()
                    .filter(btn -> btn.getFont().isBold())
                    .findFirst()
                    .map(AbstractButton::getText)
                    .orElse("1Y");
            chartPanel.setChart(createChart(currentRange));
        });

        stockSelector.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        stockSelector.setBackground(new Color(245, 245, 255));
        stockSelector.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton("▾");
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setContentAreaFilled(false);
                button.setFocusable(false);
                button.setForeground(new Color(60, 60, 60));
                return button;
            }
        });
        stockSelector.setPreferredSize(new Dimension(80, 28));
        leftPanel.add(stockSelector);
        leftPanel.add(refreshButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(Color.WHITE);
        for (String r : ranges) {
            JButton btn = new JButton(r);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFont(new Font("SansSerif", r.equals("1Y") ? Font.BOLD : Font.PLAIN, 12));
            btn.setForeground(r.equals("1Y") ? new Color(98, 0, 238) : Color.GRAY);
            btn.addActionListener(e -> {
                for (JButton b : rangeButtons) {
                    b.setForeground(Color.GRAY);
                    b.setFont(new Font("SansSerif", Font.PLAIN, 12));
                }
                btn.setForeground(new Color(98, 0, 238));
                btn.setFont(btn.getFont().deriveFont(Font.BOLD));
                chartPanel.setChart(createChart(btn.getText()));
            });
            rangeButtons.add(btn);
            rightPanel.add(btn);
        }

        rangePanel.add(leftPanel, BorderLayout.EAST);
        rangePanel.add(rightPanel, BorderLayout.WEST);

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(Color.WHITE);
        topSection.add(statsPanel);
        topSection.add(rangePanel);
        rangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 0));
        rangePanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));

        JPanel graphArea = new JPanel();
        graphArea.setBackground(new Color(240, 240, 255));
        graphArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        graphArea.setLayout(new BorderLayout());

        chartPanel = new ChartPanel(createChart("1Y"));
        chartPanel.setPreferredSize(null); // or just don’t set at all
        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        chartPanel.setMinimumSize(new Dimension(300, 300)); // optional fallback
        //chartPanel.setMouseWheelEnabled(true);
        //chartPanel.setDomainZoomable(true);
        //chartPanel.setRangeZoomable(true);
        graphArea.add(chartPanel, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);
        add(graphArea, BorderLayout.CENTER);

        updatePortfolioMetrics(userId);
    }

    public void updatePortfolioMetrics(int userId) {
    double portfolioValue = 0.0;
    double totalGainValue = 0.0;
    double todaysGainValue = 0.0;

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocks", "root", "shravya2004")) {
            System.out.println("[DEBUG] Connected to MySQL database successfully.");

            PreparedStatement stmt = conn.prepareStatement("SELECT stock_symbol, shares_owned, average_price FROM holdings WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            boolean hasRows = false;

            while (rs.next()) {
                hasRows = true;

                String symbol = rs.getString("stock_symbol");
                double shares = rs.getDouble("shares_owned");
                double avgPrice = rs.getDouble("average_price");

                System.out.println("[DEBUG] Retrieved from DB - Symbol: " + symbol + ", Shares: " + shares + ", Avg Price: " + avgPrice);

                String[] priceInfo = getLatestPrice(symbol);
                System.out.println("[DEBUG] Latest price info for " + symbol + ": " + Arrays.toString(priceInfo));

                double latestPrice;
                double priceChange;

                try {
                    latestPrice = Double.parseDouble(priceInfo[0].replace("$", ""));
                    priceChange = Double.parseDouble(priceInfo[1].replace("+", "").replace("−", "-"));
                } catch (NumberFormatException e) {
                    System.err.println("[ERROR] Failed to parse price info for symbol: " + symbol);
                    e.printStackTrace();
                    continue;
                }

                double currentValue = latestPrice * shares;
                double invested = avgPrice * shares;
                double gain = currentValue - invested;
                double todayGain = priceChange * shares;

                System.out.println("[DEBUG] " + symbol + " | Latest: " + latestPrice + " | Current Value: " + currentValue + " | Gain: " + gain + " | Today Gain: " + todayGain);

                portfolioValue += currentValue;
                totalGainValue += gain;
                todaysGainValue += todayGain;
            }

            if (!hasRows) {
                System.out.println("[DEBUG] No holdings found for user ID: " + userId);
            }

            String portfolioText = String.format("<html><div style='font-size:10px; color:#888;'>Your Portfolio Value</div><div style='font-size:18px; font-weight:bold;'>$%,.2f</div></html>", portfolioValue);
            String totalGainText = String.format("<html><div style='font-size:10px; color:#888;'>Total Gain</div><div style='font-size:12px; color:%s;'>%+,.2f (%.2f%%)</div></html>",
                    (totalGainValue >= 0 ? "green" : "red"), totalGainValue, (portfolioValue == 0 ? 0 : (totalGainValue / (portfolioValue - totalGainValue)) * 100));
            String todayGainText = String.format("<html><div style='font-size:10px; color:#888;'>Today's Gain</div><div style='font-size:12px; color:%s;'>%+,.2f (%.2f%%)</div></html>",
                    (todaysGainValue >= 0 ? "green" : "red"), todaysGainValue, (portfolioValue == 0 ? 0 : (todaysGainValue / (portfolioValue - todaysGainValue)) * 100));

            portfolioVal.setText(portfolioText);
            totalGain.setText(totalGainText);
            todaysGain.setText(todayGainText);

            System.out.println("[DEBUG] Final Portfolio Value: $" + portfolioValue);
            System.out.println("[DEBUG] Final Total Gain: $" + totalGainValue);
            System.out.println("[DEBUG] Final Today's Gain: $" + todaysGainValue);

        } catch (Exception e) {
            System.err.println("[ERROR] Exception in updatePortfolioMetrics:");
            e.printStackTrace();
        }
    }

    private String[] getLatestPrice(String symbol) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
                    "/Users/shravyadsouza/IdeaProjects/Stock/src/services/stocks_api.py", symbol, "1D"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String json = br.readLine();
            System.out.println("[DEBUG] Python output"+json);
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

       private TimeSeries createActualSeries(String range) {
        TimeSeries actualSeries = new TimeSeries(currentStockSymbol + " Close Price");
        try {
            System.out.println("Launching stocks_api.py for " + currentStockSymbol + " with range " + range);
            ProcessBuilder pb = new ProcessBuilder(
                    "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
                    "/Users/shravyadsouza/IdeaProjects/Stock/src/services/stocks_api.py",
                    currentStockSymbol,
                    range
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[stocks_api.py output] " + line);
                jsonBuilder.append(line.trim());
            }
            process.waitFor();

            String json = jsonBuilder.toString();
            System.out.println("Final JSON received: " + json);

            int start = json.indexOf("[");
            int end = json.lastIndexOf("]");
            if (start != -1 && end != -1) {
                json = json.substring(start, end + 1);
            } else {
                System.err.println("Malformed JSON: Array bounds not found.");
                return actualSeries;
            }

            JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
            Double lastClose = null;
            for (JsonElement el : jsonArray) {
                JsonObject obj = el.getAsJsonObject();
                String dateStr = obj.get("date").getAsString();
                double close = obj.get("close").getAsDouble();
                if (lastClose != null && Double.compare(lastClose, close) == 0) continue;
                lastClose = close;

                DateTimeFormatter formatter;
                if (range.equals("1D")) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);
                    actualSeries.addOrUpdate(
                            new Minute(dt.getMinute(), dt.getHour(), dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()),
                            close
                    );
                } else {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate dt = LocalDate.parse(dateStr, formatter);
                    switch (range) {
                        case "1M", "3M" -> actualSeries.addOrUpdate(new Day(dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()), close);
                        case "1Y", "2Y", "5Y" -> actualSeries.addOrUpdate(new Month(dt.getMonthValue(), dt.getYear()), close);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in createActualSeries: " + e.getMessage());
            e.printStackTrace();
        }
        return actualSeries;
    }

    private TimeSeries createPredictedSeries(String range) {
    TimeSeries predictedSeries = new TimeSeries(currentStockSymbol + " LSTM Prediction");

    try {
        System.out.println("[DEBUG] Running LSTM prediction script for symbol: " + currentStockSymbol + ", range: " + range);

        ProcessBuilder pb = new ProcessBuilder(
                "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
                "/Users/shravyadsouza/IdeaProjects/Stock/src/services/predict_lstm1.py",
                currentStockSymbol,
                range
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[PYTHON OUTPUT] " + line);
            if (line.trim().startsWith("[")) {
                jsonBuilder.append(line.trim());
                break;
            }
        }

        int exitCode = process.waitFor();
        System.out.println("[DEBUG] Python process exited with code: " + exitCode);

        String json = jsonBuilder.toString();
        System.out.println("[DEBUG] Raw JSON output: " + json);

        int start = json.indexOf("[");
        int end = json.lastIndexOf("]");
        if (start != -1 && end != -1 && end > start) {
            json = json.substring(start, end + 1);
        } else {
            System.err.println("[ERROR] Invalid JSON array boundaries: start=" + start + ", end=" + end);
            return predictedSeries;
        }

        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        System.out.println("[DEBUG] Parsed JSON array size: " + jsonArray.size());

        for (JsonElement el : jsonArray) {
            JsonObject obj = el.getAsJsonObject();
            String dateStr = obj.get("date").getAsString();

            if (obj.has("predicted_close") && !obj.get("predicted_close").isJsonNull()) {
                try {
                    double predictedClose = obj.get("predicted_close").getAsDouble();
                    System.out.println("[DEBUG] Parsed prediction - Date: " + dateStr + ", Predicted Close: " + predictedClose);

                   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm:ss]");

                        if (range.equals("1D")) {
                            LocalDateTime dt = LocalDateTime.parse(dateStr, formatter);
                            predictedSeries.addOrUpdate(
                                new Minute(dt.getMinute(), dt.getHour(), dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()),
                                predictedClose
                            );
                        } else {
                            LocalDate dt = LocalDate.parse(dateStr.substring(0, 10));
                            predictedSeries.addOrUpdate(
                                    new Day(dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()),
                                    predictedClose
                            );
                        }
                } catch (Exception parseEx) {
                    System.err.println("[ERROR] Failed to parse date or add point: " + parseEx.getMessage());
                    parseEx.printStackTrace();
                }
            }
        }

    } catch (Exception e) {
        System.err.println("[ERROR] Exception occurred during LSTM prediction parsing:");
        e.printStackTrace();
    }

    System.out.println("[DEBUG] Total predicted points added: " + predictedSeries.getItemCount());
    return predictedSeries;
}

    /*private TimeSeries createPredictedSeries(String range) {
    TimeSeries predictedSeries = new TimeSeries(currentStockSymbol + " LSTM Prediction");

    try {
        System.out.println("[DEBUG] Running LSTM prediction script for symbol: " + currentStockSymbol + ", range: " + range);

        ProcessBuilder pb = new ProcessBuilder(
                "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
                "/Users/shravyadsouza/IdeaProjects/Stock/src/services/predict_lstm.py",
                currentStockSymbol,
                range
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[PYTHON OUTPUT] " + line);
            if (line.trim().startsWith("[")) {
                jsonBuilder.append(line.trim());
                break;
            }
        }

        int exitCode = process.waitFor();
        System.out.println("[DEBUG] Python process exited with code: " + exitCode);

        String json = jsonBuilder.toString();
        System.out.println("[DEBUG] Raw JSON output: " + json);

        int start = json.indexOf("[");
        int end = json.lastIndexOf("]");
        if (start != -1 && end != -1 && end > start) {
            json = json.substring(start, end + 1);
        } else {
            System.err.println("[ERROR] Invalid JSON array boundaries: start=" + start + ", end=" + end);
            return predictedSeries;
        }

        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        System.out.println("[DEBUG] Parsed JSON array size: " + jsonArray.size());

        for (JsonElement el : jsonArray) {
                JsonObject obj = el.getAsJsonObject();
                String dateStr = obj.get("date").getAsString();
                double predictedClose = obj.get("predicted_close").getAsDouble();

                System.out.println("[DEBUG] Parsed prediction - Date: " + dateStr + ", Predicted Close: " + predictedClose);

                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate dt = LocalDate.parse(dateStr, formatter);
                    predictedSeries.addOrUpdate(new Day(dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()), predictedClose);
                } catch (Exception parseEx) {
                    System.err.println("[ERROR] Failed to parse date or add point: " + parseEx.getMessage());
                    parseEx.printStackTrace();
                }
            }
        /*for (JsonElement el : jsonArray) {
            JsonObject obj = el.getAsJsonObject();
            String dateStr = obj.get("date").getAsString();
            double predictedClose = obj.get("predicted_close").getAsDouble();

            System.out.println("[DEBUG] Parsed prediction - Date: " + dateStr + ", Predicted Close: " + predictedClose);

            try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate dt = LocalDate.parse(dateStr, formatter);
                    switch (range) {
                        case "1D":
                            predictedSeries.addOrUpdate(new Minute(0, 0, dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()), predictedClose);
                            break;
                        case "1M":
                        case "3M":
                            predictedSeries.addOrUpdate(new Day(dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear()), predictedClose);                            break;
                        case "1Y":
                        case "2Y":
                        case "5Y":
                            predictedSeries.addOrUpdate(new Month(dt.getMonthValue(), dt.getYear()), predictedClose);
                            break;
                    }
                } catch (Exception parseEx) {
                    System.err.println("[ERROR] Failed to parse date or add point: " + parseEx.getMessage());
                    parseEx.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred during LSTM prediction parsing:");
            e.printStackTrace();
        }

        System.out.println("[DEBUG] Total predicted points added: " + predictedSeries.getItemCount());
        return predictedSeries;
    }*/

   private TimeSeriesCollection createDataset(String range) {
    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(createActualSeries(range));
     dataset.addSeries(createPredictedSeries(range));
    return dataset;
    }

    private JFreeChart createChart(String range) {
        XYDataset dataset = createDataset(range);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                currentStockSymbol + " Stock Performance", "Time", "Close Price", dataset,
                true, true, true
        );
        TimeSeries actualSeries = new TimeSeries("Actual Price");
        TimeSeries predictedSeries = new TimeSeries("LSTM Prediction");

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, new Color(98, 0, 238));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5, 5));

        renderer.setSeriesPaint(1, new Color(255, 165, 0, 180)); // translucent orange
        renderer.setSeriesStroke(1, new BasicStroke(
            1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0,
            new float[]{6.0f, 6.0f}, 0.0f)); // smoother dash
        renderer.setSeriesShapesVisible(1, false); // turn off shape clutter
        //renderer.setSeriesShape(1, new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0)); // Larger dot size
        renderer.setSeriesShapesVisible(1, false);

        plot.setRenderer(renderer);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));

        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        domainAxis.setVerticalTickLabels(true);

        if (range.equals("1Y")) {
            domainAxis.setDateFormatOverride(new java.text.SimpleDateFormat("MMM yyyy"));
            domainAxis.setTickUnit(new org.jfree.chart.axis.DateTickUnit(
                org.jfree.chart.axis.DateTickUnitType.MONTH, 2)); // Show every 2nd month
        }
        else if (range.equals("2Y")) {
            domainAxis.setDateFormatOverride(new java.text.SimpleDateFormat("MMM yyyy"));
            domainAxis.setTickUnit(new org.jfree.chart.axis.DateTickUnit(
                org.jfree.chart.axis.DateTickUnitType.MONTH, 3)); // Show every 4th month
        } else if (range.equals("5Y")) {
            domainAxis.setDateFormatOverride(new java.text.SimpleDateFormat("yyyy"));
            domainAxis.setTickUnit(new org.jfree.chart.axis.DateTickUnit(
                org.jfree.chart.axis.DateTickUnitType.MONTH, 6)); // Show one year per tick
        }
        chart.getXYPlot().getRenderer().setDefaultToolTipGenerator(StandardXYToolTipGenerator.getTimeSeriesInstance());
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 20));

        return chart;
    }
}


