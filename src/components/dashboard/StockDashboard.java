package components.dashboard;

import components.OpeningPage;
import components.dashboard.sections.Graph;
import components.dashboard.sections.NavigationBar;
import components.dashboard.sections.PortfolioTable;
import components.dashboard.sections.Watchlist;
import components.dashboard.sections.RelatedNews;

import javax.swing.*;
import java.awt.*;

public class StockDashboard extends JFrame {
    private final int userId;
    private Graph graph;

    public StockDashboard(int userId) {
        this.userId = userId;
        setTitle("ACTION");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new NavigationBar(userId), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        setVisible(true);
    }

      private JPanel createMainContent() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        graph = new Graph(userId);
        graph.setPreferredSize(new Dimension(600, 600));
        PortfolioTable table = new PortfolioTable(userId,this);
        table.setPreferredSize(new Dimension(600, 250));

        leftPanel.add(graph, BorderLayout.CENTER);
        leftPanel.add(table, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());

        Watchlist watchlist = new Watchlist(userId);
        JScrollPane watchlistScroll = new JScrollPane(watchlist);
        watchlistScroll.setPreferredSize(new Dimension(600, (int) (800 * 0.40)));

        RelatedNews relatedNews = new RelatedNews();
        JScrollPane newsScroll = new JScrollPane(relatedNews);

        rightPanel.add(watchlistScroll, BorderLayout.NORTH);
        rightPanel.add(newsScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.6);
        rightPanel.setMinimumSize(new Dimension(400, 600));
        leftPanel.setMinimumSize(new Dimension(600, 400));
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);

        container.add(splitPane, BorderLayout.CENTER);
        return container;
    }
     public void refreshGraph() {
        if (graph != null) {
            graph.updatePortfolioMetrics(userId);
        }
    }
    //public static void main(String[] args) {
       // SwingUtilities.invokeLater(StockDashboard::new);
    //}
    public static void main(String[] args) {
       new StockDashboard(2);
    }
}
