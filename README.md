# ACTION: Analytical Conclusions for Trading Investment Optimization & Navigation
----------
**Description:**

ACTION is a platform designed to provide users with actionable insights for optimizing their trading and investment strategies. It leverages real-time data analysis and provides tools for portfolio management, watchlist tracking, and news aggregation.

**Key Features:**

* **User Management:** Secure user authentication and profile management.
* **Portfolio Tracking:** Real-time tracking of holdings, including equity value and average purchase price.
* **Watchlist Management:** Creation and monitoring of personalized watchlists.
* **Real-time Stock Data:** Real-time Stock Price Updates via Yahoo Finance
* **News Aggregation:** Access to relevant financial news related to tracked stocks.

--------

## 1. Clone the Repository
```
git clone https://github.com/ShravyaDsouza/ACTION-Analytical_Conclusions_for_Trading_Investment_Optimization_And_Navigation.git
cd ACTION
```

## 2. Python Environment Setup
* Navigate to the src/services folder and create a virtual environment:
```
cd src/services
python3 -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
```

* Install Required Packages
```pip install -r requirements.txt```


* requirements.txt
yfinance
numpy
pandas
tensorflow
scikit-learn
requests

#### 🐍 Python Compatibility
⚠️ Note: Ensure you're using Python 3.10 (recommended) for full compatibility with tensorflow and the .keras model format.
angular2html
python3 --version
# Output should be: Python 3.10.x


## 3. Schema Design

**Database:**  MySQL
#### Launch
```
mysql -u root -p
```

```mysql
CREATE DATABASE IF NOT EXISTS stocks;
```

```mysql 
USE stocks;
```

**Tables:**

### users

* **Purpose:** Stores user account information.
* **Columns:**
    * user_id (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each user.
    * name (VARCHAR(100), NOT NULL): User's full name.
    * email (VARCHAR(100), UNIQUE, NOT NULL): User's email address (unique).
    * mobile_no (VARCHAR(15), NOT NULL): User's mobile number.
    * password_hash (VARCHAR(255), NOT NULL): Hashed password for secure authentication.

```mysql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mobile_no VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);
```

### holdings
* **Purpose:** Tracks user's stock holdings.
* **Columns:**
    * holding_id (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each holding.
    * user_id (INT, NOT NULL, FOREIGN KEY referencing users(user_id)): User ID associated with the holding.
    * stock_symbol (VARCHAR(10), NOT NULL): Stock symbol (e.g., AAPL).
    * company_name (VARCHAR(100)): Company name.
    * shares_owned (DECIMAL(10, 2), NOT NULL): Number of shares owned.
    * average_price (DECIMAL(10, 2)): Average purchase price per share.
    * equity_value (DECIMAL(12, 2)): Calculated real time (optional to save to the database).
    * Foreign Key: user_id references users(user_id) with ON DELETE CASCADE.

```mysql
CREATE TABLE holdings (
    holding_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    stock_symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(100),
    shares_owned DECIMAL(10, 2) NOT NULL,
    average_price DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);
```

### watchlist
* **Purpose:** Stores user's watchlist of stocks.
* **Columns:**
    * watchlist_id (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each watchlist entry.
    * user_id (INT, NOT NULL, FOREIGN KEY referencing users(user_id)): User ID associated with the watchlist. stock_symbol (VARCHAR(10), NOT NULL): Stock symbol.
    * company_name (VARCHAR(100)): Company name. 
    * added_on (DATETIME, DEFAULT CURRENT_TIMESTAMP): Timestamp when the stock was added to the watchlist.
    * Foreign Key: user_id references users(user_id) with ON DELETE CASCADE.

```mysql
CREATE TABLE watchlist (
    watchlist_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    stock_symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(100),
    added_on DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);
```

### stock_data
* **Purpose:** Stores the historic values csv file produced from historic_data.ipynb (optional).
```mysql
DROP TABLE IF EXISTS stock_data;
CREATE TABLE stock_data (
    symbol VARCHAR(10),
    Date DATE,
    Open FLOAT,
    High FLOAT,
    Low FLOAT,
    Close FLOAT,
    Volume BIGINT
);
```

### Index creation for Optimized Performance (Optional)
```mysql
CREATE INDEX idx_symbol ON stock_data(symbol);
ALTER TABLE stock_data ADD COLUMN year INT GENERATED ALWAYS AS (YEAR(Date)) STORED;
CREATE INDEX idx_symbol_year ON stock_data(symbol, year);
```

#### Kindly refer the Yahoo Finance
API for integration in the project via Java: https://mvnrepository.com/artifact/com.yahoofinance-api/YahooFinanceAPI

## 4. Java Setup & Project Structure
This is a Maven project. Java 17 is recommended.

### Structure
```
📁 ACTION/
├── .idea/                               # IntelliJ project config files
├── csv/                                 # Historic CSV files (2018–2025) fetched via historic_data.ipynb
├── lib/                                 # External libraries for Java 
├── models/                              # Saved .keras models for each stock and interval
├── static/                              # Static assets used in GUI
│   ├── banner.png
│   └── login.png
├── src/
│   ├── components/                      # Java Swing UI components
│   │   ├── LoginForm.java
│   │   ├── SignupPage.java
│   │   ├── ForgotPassword.java
│   │   ├── OpeningPage.java
│   │   └── dashboard/
│   │       ├── StockDashboard.java
│   │       └── sections/
│   │           ├── Graph.java
│   │           ├── NavigationBar.java
│   │           ├── PlaceholderTextField.java
│   │           ├── PortfolioTable.java
│   │           ├── RelatedNews.java
│   │           └── Watchlist.java
│
│   ├── services/                        # Python + Java integration layer
│   │   ├── check.py                     # To test the working of yfiance
│   │   ├── predict_lstm.py              # LSTM price prediction for given stock
│   │   ├── stocks_api.py                # Fetch live price using Yahoo Finance
│   │   ├── news_api.py                  # Fetch financial news from Yahoo RSS
│   │   ├── DatabaseConnector.java       # MySQL DB connection class
│   │   └── services.iml
│
│   └── utils/                           # Utility testers and validators
│       ├── APITester.java
│       ├── NewsTester.java
│       ├── SymbolValidator.java         # Verifies stock ticker validity via Alpha Vantage
│       └── TestDBConnection.java        # Verifies DB connectivity
│
├── pom.xml                              # Maven dependency file
└── README.md                            # 📘 Project documentation (this file)
```

### How to Run the App
* Open project in IntelliJ IDEA or VS Code.
* Ensure Maven dependencies are resolved (refer the pom.xml file and click on Rebuild Module).
* Launch the GUI from OpeningPage.java → LoginForm.java → StockDashboard.java
* Python files are executed using ProcessBuilder.

## 5. Troubleshooting

* If Python script doesn't run, check:
  * Python path is correctly set in ProcessBuilder 
  * Virtual environment is activated
* Ensure .keras models are pre-trained and placed under /models with names
* Ensure that SQL connection is established with correct username , password and database name and is running.
*  No data fetched → Check your API key, internet access, or JSON parsing errors

## 6. API Keys & Config
Set your Alpha Vantage API key and update in place of yahoo fianace calls and Ensure internet access is allowed for API fetches.
private static final String API_KEY = "YOUR_API_KEY";


## 7. Deployment Note
ACTION is a Java Swing desktop app, and is intended for local use only (not web-based).

## 8. Screenshots
#### 🎬 Opening Animation
The app opens with a dramatic pixelated reveal, transitioning smoothly into the main splash screen — visually communicating the high-stakes world of finance and the core identity of ACTION.
<table> <tr> <th>Pixelated Reveal</th> <th>Final Splash</th> </tr> <tr> <td><<img width="400" alt="Screenshot 2025-04-14 at 12 37 03 PM" src="https://github.com/user-attachments/assets/a32eb689-32aa-4fb6-8552-0cd65a0b31d5"/></td> <td><<img width="400" alt="Screenshot 2025-04-14 at 12 37 06 PM" src="https://github.com/user-attachments/assets/863aa883-3d36-4dbe-a91a-1f5eb22564bb"/>
</td> </tr> </table>

#### 🧾 Authentication Screens Overview
<table>
   <tr>
    <th><img width="200" alt="Screenshot 2025-04-14 at 12 37 13 PM" src="https://github.com/user-attachments/assets/ea702c6e-a744-42a3-b6b3-fb99d3783285" />
</th>
    <th><img width="200" alt="Screenshot 2025-04-14 at 12 37 22 PM" src="https://github.com/user-attachments/assets/3300c1dd-0bc4-4604-81c0-8d17f9167304" />
</th>
    <th><img <img width="200" alt="Screenshot 2025-04-14 at 12 38 26 PM" src="https://github.com/user-attachments/assets/7ce2bdb5-f79a-4d68-b032-39b02578f9f2" />
</th>
  </tr>
  <tr>
    <th>Login Form</th>
    <th>Signup Page</th>
    <th>Forgot Password</th>
  </tr>
  <tr>
    <td>
      - Username and Password 🔐<br>
      - Emojis for intuitive input<br>
      - "Forgot Password?" link<br>
      - Hover tooltip: <i>"Enter your username / password"</i><br>
      ✅ Input validation for correct email format<br>
      ✅ Clear error prompts<br>
      ✅ Tooltips on hover
    </td>
    <td>
      - Fields: Name 👤, Email ✉️, Mobile 📱, Password 🔒<br>
      - All fields validated with tooltip hints<br>
      - Prevents duplicate email registration<br>
      ✅ Email format check + Mobile number (10-digit, starts with non-zero)<br>
      ✅ Ensures unique emails in DB
    </td>
    <td>
      - Accepts email or phone 📧📱<br>
      - Sends reset link (mock)<br>
      - Hover shows <i>“Enter registered Email or Phone”</i><br>
      ✅ Validates format before sending<br>
      ✅ User feedback if input not recognized
    </td>
  </tr>
</table>

#### 📊 Stock Dashboard Overview

The main dashboard provides a **comprehensive and interactive** experience for users to manage their investments. Below is a breakdown of each section visible in the interface:
<img width="1268" alt="Screenshot 2025-04-14 at 12 40 51 PM" src="https://github.com/user-attachments/assets/ab8b19bd-34cc-4fdc-9bcc-9627fe589d19" />

---

##### 🟣 Header Bar

| Element | Description |
|--------|-------------|
| **App Title - ACTION** | Branding with bold styling, aligned to the left. |
| **Tabs:** Dashboard, Trade, Accounts, New to Investing? | Navigation tabs for different sections (static placeholder). |
| **Search Bar** | Search functionality for stocks or news (not wired in this screenshot). |
| **Profile Avatar (Shavya)** | Placeholder for profile and dropdown menu (e.g., settings, logout). |



### 💼 Portfolio Summary

| Label | Description |
|-------|-------------|
| **Your Portfolio Value** | Total equity value of current holdings (e.g., $1980.20). |
| **Today’s Gain** | Real-time change for the day with percentage (green = positive). |
| **Total Gain** | Cumulative return over holding period. |


### 📈 Time Range Selection

- **Options:** 1D, 1M, 3M, 1Y, 2Y, 5Y
- **Selected:** 1Y (highlighted in purple)
- Adjusts the granularity of the stock performance graph.



### 📉 AAPL Stock Performance Chart

| Feature | Description |
|---------|-------------|
| **Graph** | Line chart showing historical Close Price (purple) and LSTM Predictions (yellow dots). |
| **Legend** | Bottom of the graph showing what each color represents. |
| **Dropdown (AAPL)** | Lets user select a different stock for viewing. |
| **Refresh Icon** | Reloads latest graph and prediction for selected stock. |


### 📋 Holdings Section

| Column | Description |
|--------|-------------|
| **Company** | Currently held stock (AAPL). |
| **Last Price** | Real-time price via Yahoo API. |
| **Change** | Price difference from previous close. |
| **Your Equity** | Shares Owned x Last Price. |
| **Today's Return** | Change in equity value for the day. |
| **Total Return** | Cumulative profit/loss. |
| **5–Day Chart** | Sparkline visual preview of recent trend. |

✅ **Add / Remove Button (Bottom Left)**  
- ➕ Adds a new holding via a pop-up.  
- 🗑 Deletes selected holding after user confirmation.


### 📌 Watchlist Panel (Top Right)

| Column | Description |
|--------|-------------|
| **Company** | Example: AMZN. |
| **Last Price** | Real-time fetched. |
| **Change** | Daily price change. |
| **5–Day Chart** | Sparkline preview chart. |

✅ **Buttons (Top Right):**
- ➕ Add stock to watchlist.
- 🗑 Remove selected stock from watchlist.


### 📰 Related News Panel

- Dynamically fetched from **Yahoo Finance RSS** using news_api.py.
- Lists stock-related news headlines, clickable for external reading.
- Each entry includes:
  - **Title**
  - **Published Date**

✅ **Pagination Bar**  
- Located at the bottom of the news panel.
- Supports navigation between multiple pages (buttons 1, 2, 3, ➡️).


### 🔁 Summary of Interactions

| Icon/Button | Action |
|-------------|--------|
| 🔁 (Refresh) | Updates graph for selected stock |
| ➕ (Holdings / Watchlist) | Opens add dialog |
| 🗑 (Holdings / Watchlist) | Removes selected entry |
| 📊 Graph Toggle | Adjusts range from 1D to 5Y |
| 📄 News | Live financial news feed for tracked companies |

---


## 9. Future Enhancements

* **Market Alignment**  
  Extend compatibility and analytics to include Indian stock markets (e.g., NSE, BSE).

* **Expanded Stock Universe**  
  Scale beyond the initial 15 stocks to support a wider range of tickers across industries and geographies.

* **Advanced Graphing**  
  Integrate enhanced graph visualizations combining:
  - Real-time prices  
  - LSTM-predicted values  
  - Broader market indices and sector trends  

* **Intelligent Alerts**  
  Notify users of significant stock movements such as:
  - Sudden price surges or dips  
  - Volatility spikes  
  - Unusual trading volume  

* **Social Sentiment Signals**  
  Leverage crowd sentiment analytics (e.g., user buy/sell patterns, Twitter/X trends) to guide investment decisions.

* **Custom Report Exports**  
  Allow users to download personalized portfolio reports in formats like PDF/CSV for analysis or tax filing.
