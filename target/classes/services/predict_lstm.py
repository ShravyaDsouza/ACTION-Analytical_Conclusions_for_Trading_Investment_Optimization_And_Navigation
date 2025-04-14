import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

import sys
import json
import yfinance as yf
import pandas as pd
import numpy as np
from tensorflow import keras
from datetime import datetime, timedelta
from sklearn.preprocessing import MinMaxScaler
import logging

logging.getLogger("yfinance").setLevel(logging.ERROR)

def fetch_last_60_closes(symbol, interval, range_val):
    if interval == "5m":
        period = "59d"
    elif range_val == "5Y":
        period = "10y"
    elif range_val == "2Y":
        period = "5y"
    elif range_val == "1Y":
        period = "3y"
    elif range_val == "3M":
        period = "6mo"
    elif range_val == "1M":
        period = "3mo"
    else:
        period = "6mo"

    df = yf.download(symbol, period=period, interval=interval, progress=False)
    df = df[['Close']].dropna()

    if df.shape[0] < 60:
        # fallback: try 1d interval
        if interval != "1d":
            fallback_df = yf.download(symbol, period="1y", interval="1d", progress=False)
            fallback_df = fallback_df[['Close']].dropna()
            if fallback_df.shape[0] >= 60:
                fallback_df = fallback_df.tail(60)
                fallback_df.reset_index(inplace=True)
                return fallback_df, "1d"
        print(f"[DEBUG] Not enough rows: {df.shape[0]} < 60 for {symbol} - Interval: {interval}", file=sys.stderr)
        print("[]")
        return None, None

    df = df.tail(60)
    df.reset_index(inplace=True)
    return df, interval

def generate_future_timestamps(start_date, steps, interval):
    timestamps = []
    for i in range(steps):
        if interval == "5m":
            timestamps.append((start_date + timedelta(minutes=(i + 1) * 5)).strftime("%Y-%m-%d %H:%M"))
        elif interval == "1d":
            timestamps.append((start_date + timedelta(days=i + 1)).strftime("%Y-%m-%d"))
        elif interval == "2d":
            timestamps.append((start_date + timedelta(days=(i + 1) * 2)).strftime("%Y-%m-%d"))
        elif interval == "30d":
            future_date = start_date + pd.DateOffset(months=i + 1)
            timestamps.append(future_date.strftime("%Y-%m-%d"))
    return timestamps

def predict(symbol, range_val):
    try:
        if range_val == "1D":
            steps = 12
            interval = "5m"
            model_tag = "5m"
        elif range_val == "1M":
            steps = 30
            interval = "1d"
            model_tag = "1d"
        elif range_val == "3M":
            steps = 20
            interval = "2d"
            model_tag = "1d"
        elif range_val in ["1Y", "2Y", "5Y"]:
            steps = 12 if range_val == "1Y" else 24
            interval = "30d"
            model_tag = "1mo"
        else:
            steps = 12
            interval = "1d"
            model_tag = "1d"

        df, actual_interval = fetch_last_60_closes(symbol, model_tag, range_val)
        if df is None or df.shape[0] < 60:
            return

        close_prices = df['Close'].values.reshape(-1, 1)
        scaler = MinMaxScaler()
        scaled = scaler.fit_transform(close_prices)
        input_seq = scaled.reshape(1, 60, 1)

        model_intervals = {
            "5m": "5m",
            "1d": "1d",
            "2d": "1wk",
            "30d": "1mo"
        }
        model_interval = model_intervals.get(actual_interval, "1d")
        model_path = f"/Users/shravyadsouza/IdeaProjects/Stock/models/{symbol}_{model_interval}_model.keras"

        if not os.path.exists(model_path):
            print("[]")
            return

        model = keras.models.load_model(model_path)

        predictions_scaled = []
        for _ in range(steps):
            next_val = model.predict(input_seq, verbose=0).flatten()[0]
            predictions_scaled.append(next_val)
            new_seq = np.append(input_seq.flatten()[1:], [next_val])
            input_seq = new_seq.reshape(1, 60, 1)

        y_pred = scaler.inverse_transform(np.array(predictions_scaled).reshape(-1, 1)).flatten()
        last_date = pd.to_datetime(df['Date'].values[-1])
        timestamps = generate_future_timestamps(last_date, steps, actual_interval)

        # Append the last actual close as the first predicted point for continuity
        last_actual_date = df['Date'].values[-1]
        last_actual_close = float(df['Close'].values[-1])
        result = [{"date": pd.to_datetime(last_actual_date).strftime("%Y-%m-%d"),
                   "predicted_close": last_actual_close}] + \
                 [{"date": ts, "predicted_close": float(price)} for ts, price in zip(timestamps, y_pred)]

        #result = [{"date": ts, "predicted_close": float(price)} for ts, price in zip(timestamps, y_pred)]
        print(json.dumps(result), flush=True)

    except Exception:
        print("[]")

if __name__ == "__main__":
    try:
        symbol = sys.argv[1]
        range_val = sys.argv[2]
        predict(symbol, range_val)
    except Exception:
        print("[]")
