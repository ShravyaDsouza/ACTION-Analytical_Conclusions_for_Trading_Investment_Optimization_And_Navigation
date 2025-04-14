import yfinance as yf
import sys
import json
import warnings
import contextlib
import io
import pytz
from datetime import time

warnings.filterwarnings("ignore")

def get_data(symbol, range_code):
    interval_map = {
        "1D": ("1d", "15m"),
        "5D": ("5d", "15m"),
        "1M": ("1mo", "1h"),
        "3M": ("3mo", "1d"),
        "1Y": ("1y", "1d"),
        "2Y": ("2y", "5d"),
        "5Y": ("5y", "1wk")
    }

    if range_code not in interval_map:
        return []

    period, interval = interval_map[range_code]

    try:
        with contextlib.redirect_stdout(io.StringIO()):
            data = yf.download(symbol, period=period, interval=interval, progress=False)

        if data.empty:
            return []

        output = []
        eastern = pytz.timezone("US/Eastern")
        market_open = time(9, 30)
        market_close = time(16, 0)

        for index, row in data.iterrows():
            # Convert each index to Eastern Time
            if index.tzinfo is None:
                index = index.tz_localize("UTC")
            index = index.tz_convert(eastern)

            # Only include data points within market hours for 1D and 5D
            if range_code in ["1D", "5D"]:
                if not (market_open <= index.time() < market_close):
                    continue
                timestamp = index.strftime("%Y-%m-%d %H:%M")
            else:
                timestamp = index.strftime("%Y-%m-%d")

            close_price = row["Close"]
            if not isinstance(close_price, (int, float)):
                close_price = float(close_price)

            output.append({
                "date": timestamp,
                "close": round(close_price, 2)
            })

        return output

    except Exception as e:
        return [{"error": str(e)}]


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(json.dumps({"error": "Usage: python stocks_api.py SYMBOL RANGE"}))
        sys.exit(1)

    symbol = sys.argv[1]
    range_code = sys.argv[2]
    result = get_data(symbol, range_code)

    print(json.dumps(result))
