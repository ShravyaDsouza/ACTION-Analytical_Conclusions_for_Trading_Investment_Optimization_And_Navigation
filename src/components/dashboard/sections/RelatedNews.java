package components.dashboard.sections;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

import com.google.gson.*;

public class RelatedNews extends JPanel {

    private JPanel newsListPanel;
    private JPanel paginationPanel;
    private int currentPage = 1;
    private int itemsPerPage = 5;
    private List<JsonObject> allNewsItems = new ArrayList<>();

    public RelatedNews() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        // === Wrapper panel for title + scrollable content ===
JPanel contentWrapper = new JPanel();
contentWrapper.setLayout(new BorderLayout());
contentWrapper.setBackground(Color.WHITE);

// Header title
JPanel headerPanel = new JPanel(new BorderLayout());
headerPanel.setBackground(Color.WHITE);
headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 4, 15));

JLabel headingLabel = new JLabel("Related News");
headingLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
headingLabel.setForeground(new Color(98, 0, 238));

JSeparator separator = new JSeparator();
separator.setForeground(new Color(180, 180, 255));

headerPanel.add(headingLabel, BorderLayout.NORTH);
headerPanel.add(separator, BorderLayout.SOUTH);

// Scrollable list
newsListPanel = new JPanel();
newsListPanel.setLayout(new BoxLayout(newsListPanel, BoxLayout.Y_AXIS));
newsListPanel.setBackground(Color.WHITE);

JScrollPane scrollPane = new JScrollPane(newsListPanel);
scrollPane.setBorder(null);
scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
scrollPane.getVerticalScrollBar().setUnitIncrement(16);

// Add header + scrollable content to wrapper
contentWrapper.add(headerPanel, BorderLayout.NORTH);
contentWrapper.add(scrollPane, BorderLayout.CENTER);

// Add wrapper and footer pagination to main layout
add(contentWrapper, BorderLayout.CENTER);

paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
paginationPanel.setBackground(Color.WHITE);
add(paginationPanel, BorderLayout.SOUTH);


        fetchAndDisplayNews();
    }

    private void fetchAndDisplayNews() {
        String[] symbols = {"AAPL", "GOOGL", "TSLA", "MSFT", "AMZN", "NVDA", "META", "IBM", "TSM", "UNH", "JNJ", "V", "XOM", "WMT", "PG"};
        for (String symbol : symbols) {
            JsonArray array = fetchNews(symbol);
            if (array != null) {
                int count = 0;
                for (JsonElement el : array) {
                    if (count >= 3) break;
                    allNewsItems.add(el.getAsJsonObject());
                    count++;
                }
            }
        }
        Collections.shuffle(allNewsItems);
        updateNewsPanel();
        updatePaginationPanel();
    }

    private JsonArray fetchNews(String symbol) {
    try {
        ProcessBuilder pb = new ProcessBuilder(
            "/Users/shravyadsouza/.virtualenvs/Stock/bin/python",
            "src/services/news_api.py",
            symbol
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[news_api.py output] " + line); // 👈 Print output
            jsonBuilder.append(line);
        }

        process.waitFor();
        String json = jsonBuilder.toString().trim();

        if (!json.startsWith("[")) {
            System.err.println("❌ Invalid JSON from news_api.py for " + symbol + ": " + json);
            return null;
        }

        // ✅ Parse only if valid array
        return JsonParser.parseString(json).getAsJsonArray();

    } catch (Exception e) {
        System.err.println("Error fetching news for: " + symbol);
        e.printStackTrace();
        return null;
    }
}

    private void updateNewsPanel() {
        newsListPanel.removeAll();

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allNewsItems.size());

        for (int i = start; i < end; i++) {
            JsonObject news = allNewsItems.get(i);
            newsListPanel.add(createNewsItem(
                news.get("link").getAsString().contains("AAPL") ? "AAPL" : news.get("link").getAsString().contains("GOOGL") ? "GOOGL" : "",
                news.get("title").getAsString(),
                news.get("published").getAsString(),
                news.get("link").getAsString()
            ));
        }

        newsListPanel.revalidate();
        newsListPanel.repaint();
    }

    private void updatePaginationPanel() {
        paginationPanel.removeAll();
        int totalPages = (int) Math.ceil((double) allNewsItems.size() / itemsPerPage);

        JButton prev = createArrowButton("←");
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> {
            currentPage--;
            updateNewsPanel();
            updatePaginationPanel();
        });
        paginationPanel.add(prev);

        int maxButtons = 3;
        int start = Math.max(1, currentPage - 1);
        int end = Math.min(start + maxButtons - 1, totalPages);
        for (int i = start; i <= end; i++) {
            JButton pageBtn = createPageButton(String.valueOf(i), currentPage == i);
            final int page = i;
            pageBtn.addActionListener(e -> {
                currentPage = page;
                updateNewsPanel();
                updatePaginationPanel();
            });
            paginationPanel.add(pageBtn);
        }

        JButton next = createArrowButton("→");
        next.setEnabled(currentPage < totalPages);
        next.addActionListener(e -> {
            currentPage++;
            updateNewsPanel();
            updatePaginationPanel();
        });
        paginationPanel.add(next);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private JPanel createNewsItem(String ticker, String headline, String publishedTime, String link) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 5));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel tickerLabel = new JLabel(ticker);
        tickerLabel.setForeground(new Color(101, 77, 255));
        tickerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel headlineLabel = new JLabel("<html><div style='width:350px;'><a href='" + link + "'>" + headline + "</a></div></html>");
        headlineLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headlineLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        headlineLabel.setForeground(new Color(0, 102, 204));

        headlineLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new java.net.URI(link));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        JLabel timeLabel = new JLabel(publishedTime);
        timeLabel.setForeground(Color.GRAY);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(Color.WHITE);
        textPanel.add(tickerLabel, BorderLayout.NORTH);
        textPanel.add(headlineLabel, BorderLayout.CENTER);
        textPanel.add(timeLabel, BorderLayout.SOUTH);

        itemPanel.add(textPanel, BorderLayout.CENTER);

        return itemPanel;
    }

    private JButton createPageButton(String text, boolean selected) {
    JButton button = new JButton(text);
    button.setFont(new Font("SansSerif", Font.BOLD, 13));
    button.setFocusPainted(false);
    button.setContentAreaFilled(true);
    button.setOpaque(true);
    button.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 255)));
    button.setBackground(selected ? new Color(98, 0, 238) : new Color(245, 245, 255));
    button.setForeground(selected ? Color.WHITE : Color.BLACK);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(35, 30));
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(180, 180, 255)),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));
    return button;
}

private JButton createArrowButton(String symbol) {
    JButton button = new JButton(symbol);
    button.setFont(new Font("SansSerif", Font.BOLD, 16));
    button.setFocusPainted(false);
    button.setContentAreaFilled(true);
    button.setOpaque(true);
    button.setBackground(new Color(245, 245, 255));
    button.setForeground(new Color(98, 0, 238));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setPreferredSize(new Dimension(40, 30));
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(180, 180, 255)),
        BorderFactory.createEmptyBorder(4, 10, 4, 10)
    ));
    return button;
}

}
