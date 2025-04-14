import feedparser
import sys
import json

def fetch_yahoo_rss(symbol):
    rss_url = f"https://feeds.finance.yahoo.com/rss/2.0/headline?s={symbol}&region=US&lang=en-US"
    feed = feedparser.parse(rss_url)
    news_list = []

    for entry in feed.entries[:10]:
        truncated_title = ' '.join(entry.title.split()[:12])
        if len(entry.title.split()) > 10:
            truncated_title += "..."

        news = {
            "title": truncated_title,
            "link": entry.link,
            "published": entry.published
        }
        news_list.append(news)

    return news_list

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python news_api.py <STOCK_SYMBOL>")
        sys.exit(1)

    symbol = sys.argv[1].upper()
    news_data = fetch_yahoo_rss(symbol)
    print(json.dumps(news_data, indent=2))
