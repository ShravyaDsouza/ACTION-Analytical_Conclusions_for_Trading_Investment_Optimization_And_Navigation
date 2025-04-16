import yfinance as yf
import sys
import json
import warnings
import contextlib
import io
import pytz
from datetime import time, datetime, timedelta
import numpy as np
from tensorflow import keras
from sklearn.preprocessing import MinMaxScaler

warnings.filterwarnings("ignore")


def get_prediction(symbol, range_code):
    interval_map = {
        "1D": ("1d", "15m"),
        "1M": ("1mo", "1h"),
        "3M": ("3mo", "1d"),
        "1Y": ("1y", "1d"),
        "2Y": ("2y", "5d"),
        "5Y": ("5y", "1wk")
    }

    if range_code not in interval_map:
        return []

    period, interval = interval_map[range_code]
    model_tag = interval.replace("m", "min").replace("h", "hour").replace("d", "day").replace("wk", "week")

    model_path = f"../IdeaProjects/Stock/models/{symbol}_model.keras"
    try:
        model = keras.models.load_model(model_path)
    except Exception as e:
        return [{"error": f"Model loading failed: {e}"}]

    try:
        with contextlib.redirect_stdout(io.StringIO()):
            data = yf.download(symbol, period=period, interval=interval, progress=False)

        if data.empty:
            return []

        data = data[['Close']].dropna()
        close_prices = data['Close'].values.reshape(-1, 1)

        scaler = MinMaxScaler()
        scaled = scaler.fit_transform(close_prices)

        lookback = 60
        if len(scaled) <= lookback:
            return [{"error": "Insufficient data"}]

        X = []
        for i in range(lookback, len(scaled)):
            X.append(scaled[i - lookback:i])
        X = np.array(X).reshape(-1, lookback, 1)

        y_pred = model.predict(X, verbose=0)
        y_pred = scaler.inverse_transform(y_pred).flatten()

        output = []
        eastern = pytz.timezone("US/Eastern")
        market_open = time(9, 30)
        market_close = time(16, 0)

        for i in range(lookback):
            index = data.index[i]
            if index.tzinfo is None:
                index = index.tz_localize("UTC")
            index = index.tz_convert(eastern)

            if range_code in ["1D"]:
                if not (market_open <= index.time() < market_close):
                    continue
                timestamp = index.strftime("%Y-%m-%d %H:%M")
            else:
                timestamp = index.strftime("%Y-%m-%d")

            output.append({
                "date": timestamp,
                "predicted_close": round(float(close_prices[i][0]), 2)
            })

        for i, (index, pred_val) in enumerate(zip(data.index[lookback:], y_pred)):
            if index.tzinfo is None:
                index = index.tz_localize("UTC")
            index = index.tz_convert(eastern)

            if range_code in ["1D"]:
                if not (market_open <= index.time() < market_close):
                    continue
                timestamp = index.strftime("%Y-%m-%d %H:%M")
            else:
                timestamp = index.strftime("%Y-%m-%d")

            output.append({
                "date": timestamp,
                "predicted_close": round(float(pred_val), 2)
            })

        input_seq = scaled[-lookback:].reshape(1, lookback, 1)
        if range_code == "1D":
            steps = 4  
            step_interval = timedelta(minutes=15)
        else:
            steps = 10 
            step_interval = timedelta(days=1)

        future_time = data.index[-1]
        for _ in range(steps):
            next_val = model.predict(input_seq, verbose=0).flatten()[0]
            future_time += step_interval

            if future_time.tzinfo is None:
                future_time = future_time.tz_localize("UTC")
            future_time = future_time.tz_convert(eastern)

            timestamp = future_time.strftime("%Y-%m-%d %H:%M") if range_code == "1D" else future_time.strftime("%Y-%m-%d")

            output.append({
                "date": timestamp,
                "predicted_close": round(float(scaler.inverse_transform([[next_val]])[0][0]), 2)
            })

            input_seq = np.append(input_seq.flatten()[1:], [next_val]).reshape(1, lookback, 1)

        return output

    except Exception as e:
        return [{"error": str(e)}]


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(json.dumps({"error": "Usage: python predict_lstm1.py SYMBOL RANGE"}))
        sys.exit(1)

    symbol = sys.argv[1]
    range_code = sys.argv[2]
    result = get_prediction(symbol, range_code)

    print(json.dumps(result))
