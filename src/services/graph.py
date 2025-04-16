import os
import yfinance as yf
import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import matplotlib.dates as mdates
from tensorflow import keras
from sklearn.preprocessing import MinMaxScaler

# === CONFIG ===
MODEL_DIR = "/Users/shravyadsouza/IdeaProjects/Stock/models"
OUTPUT_DIR = "/Users/shravyadsouza/IdeaProjects/Stock/graphs"
LOOKBACK = 60

STOCKS = [
    "AAPL", "MSFT", "GOOGL", "AMZN", "NVDA",
    "TSLA", "META", "IBM", "TSM", "UNH",
    "JNJ", "V", "XOM", "WMT", "PG"
]

TIMEFRAMES = {
    "1D": ("1d", "5m"),
    "1M": ("6mo", "1d"),
    "3M": ("9mo", "1d"),
    "1Y": ("1y", "1d"),
    "2Y": ("2y", "1d"),
    "5Y": ("5y", "1d")
}

COLOR_MAP = {
    "1D": "orange", "1M": "green", "3M": "red",
    "1Y": "purple", "2Y": "brown", "5Y": "black"
}

os.makedirs(OUTPUT_DIR, exist_ok=True)

def prepare_sequences(data, lookback=60):
    X, y = [], []
    for i in range(lookback, len(data)):
        X.append(data[i - lookback:i])
        y.append(data[i])
    return np.array(X), np.array(y)

def fetch_yf_data(symbol, period, interval):
    try:
        df = yf.download(symbol, period=period, interval=interval, progress=False)
        df.dropna(inplace=True)
        return df
    except Exception as e:
        print(f"[ERROR] Fetching {symbol} failed: {e}")
        return None

def generate_detailed_timeline_graph(stock):
    model_file = os.path.join(MODEL_DIR, f"{stock}_model.keras")
    if not os.path.exists(model_file):
        print(f"[SKIPPED] Model not found: {model_file}")
        return

    model = keras.models.load_model(model_file)
    fig, axes = plt.subplots(2, 3, figsize=(18, 10))
    fig.suptitle(f"{stock} Stock Prediction - Timeline Comparison", fontsize=16)

    for idx, (label, (period, interval)) in enumerate(TIMEFRAMES.items()):
        row, col = divmod(idx, 3)
        ax = axes[row][col]

        df = fetch_yf_data(stock, period, interval)
        if df is None or df.shape[0] <= LOOKBACK:
            ax.set_title(f"{label} - Insufficient data")
            ax.axis("off")
            continue

        close_prices = df['Close'].values.reshape(-1, 1)
        if len(np.unique(close_prices)) == 1:
            ax.set_title(f"{label} - Flat data")
            ax.axis("off")
            continue

        scaler = MinMaxScaler()
        scaled = scaler.fit_transform(close_prices)

        X, y_true = prepare_sequences(scaled, LOOKBACK)
        if len(X) == 0:
            ax.set_title(f"{label} - No sequences")
            ax.axis("off")
            continue

        X = X.reshape((X.shape[0], X.shape[1], 1))
        y_pred = model.predict(X)

        y_true = scaler.inverse_transform(y_true.reshape(-1, 1)).flatten()
        y_pred = scaler.inverse_transform(y_pred).flatten()
        dates = df.index[-len(y_true):]

        # Plot
        ax.plot(dates, y_true, label="Actual", color="blue")
        ax.plot(dates, y_pred, label="Predicted", color=COLOR_MAP[label])
        ax.set_title(f"{label}")
        ax.set_xlabel("Date")
        ax.set_ylabel("Stock Price")
        ax.legend()
        ax.grid(True)
        ax.tick_params(axis='x', rotation=45)

        # Format x-axis ticks as dates
        if interval == "5m":
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%H:%M'))
        else:
            ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d'))

    plt.tight_layout(rect=[0, 0, 1, 0.95])
    save_path = os.path.join(OUTPUT_DIR, f"{stock}_detailed_timelines.png")
    plt.savefig(save_path)
    plt.close()
    print(f"[✔] Saved: {save_path}")

# === RUN ===
for symbol in STOCKS:
    generate_detailed_timeline_graph(symbol)
