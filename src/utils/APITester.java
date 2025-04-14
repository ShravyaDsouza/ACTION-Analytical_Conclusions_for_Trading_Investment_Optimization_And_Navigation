package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class APITester {

    private static final String API_KEY = "";
    private static final String SYMBOL = "AAPL";

    public static void main(String[] args) {
        try {
            String url = String.format(
                "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
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
            JsonObject globalQuote = json.getAsJsonObject("Global Quote");

            if (globalQuote == null || globalQuote.size() == 0) {
                System.out.println("❌ No data returned. Please check your API key or symbol.");
                return;
            }

            System.out.println("=== 📈 Alpha Vantage Stock API: " + SYMBOL + " ===");
            System.out.println("Symbol: " + globalQuote.get("01. symbol").getAsString());
            System.out.println("Price: $" + globalQuote.get("05. price").getAsString());
            System.out.println("Change: " + globalQuote.get("09. change").getAsString());
            System.out.println("Change (%): " + globalQuote.get("10. change percent").getAsString());
            System.out.println("Volume: " + globalQuote.get("06. volume").getAsString());
            System.out.println("Open: " + globalQuote.get("02. open").getAsString());
            System.out.println("Day High: " + globalQuote.get("03. high").getAsString());
            System.out.println("Day Low: " + globalQuote.get("04. low").getAsString());
            System.out.println("Latest Trading Day: " + globalQuote.get("07. latest trading day").getAsString());
            System.out.println("Previous Close: " + globalQuote.get("08. previous close").getAsString());

        } catch (Exception e) {
            System.err.println("❌ Error fetching data: " + e.getMessage());
        }
    }
}
