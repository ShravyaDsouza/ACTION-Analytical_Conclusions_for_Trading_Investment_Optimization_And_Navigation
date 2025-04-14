package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsTester{

    private static final String API_KEY = ""; // Your API key
    private static final String SYMBOL = "AAPL";

    public static void main(String[] args) {
        try {
            String url = String.format(
                "https://www.alphavantage.co/query?function=NEWS_SENTIMENT&tickers=%s&apikey=%s",
                SYMBOL, API_KEY
            );

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray feed = json.getAsJsonArray("feed");

            if (feed == null || feed.size() == 0) {
                System.out.println("❌ No news returned. Try again later or check usage limits.");
                return;
            }

            System.out.println("=== 📰 Recent News for " + SYMBOL + " ===");

            for (int i = 0; i < Math.min(5, feed.size()); i++) {
                JsonObject newsItem = feed.get(i).getAsJsonObject();
                System.out.println("\n📌 Title: " + newsItem.get("title").getAsString());
                System.out.println("🗞️ Source: " + newsItem.get("source").getAsString());
                System.out.println("🔗 URL: " + newsItem.get("url").getAsString());
                System.out.println("📅 Published: " + newsItem.get("time_published").getAsString());
                System.out.println("🧠 Sentiment: " + newsItem.get("overall_sentiment_label").getAsString());
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching news: " + e.getMessage());
        }
    }
}
