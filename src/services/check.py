import yfinance as yf

ticker = yf.Ticker("AAPL")
df = ticker.history(period="1d", interval="15m")
print(df)