package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.*;

public class SymbolValidator {
    private static final String API_KEY = "";
    private static final String[] SYMBOLS = {
        "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA",
        "TSLA", "META", "IBM", "TSM", "UNH",
        "JNJ", "V", "XOM", "WMT", "PG"
    };

    public static void main(String[] args) {
        for (String symbol : SYMBOLS) {
            try {
                boolean isValid = validateSymbol(symbol);
                System.out.println(symbol + " ➜ " + (isValid ? "✅ Valid" : "❌ Not Found"));
            } catch (Exception e) {
                System.out.println(symbol + " ➜ ⚠️ Error: " + e.getMessage());
            }
        }
    }

    public static boolean validateSymbol(String symbol) throws Exception {
        String queryUrl = String.format(
            "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=%s&apikey=%s",
            symbol, API_KEY
        );

        HttpURLConnection conn = (HttpURLConnection) new URL(queryUrl).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            json.append(line);
        }
        in.close();

        JsonObject root = JsonParser.parseString(json.toString()).getAsJsonObject();
        JsonArray bestMatches = root.getAsJsonArray("bestMatches");

        // Check if the symbol exists in any of the best matches
        for (JsonElement match : bestMatches) {
            JsonObject obj = match.getAsJsonObject();
            String matchSymbol = obj.get("1. symbol").getAsString();
            if (matchSymbol.equalsIgnoreCase(symbol)) {
                return true;
            }
        }

        return false;
    }
}
