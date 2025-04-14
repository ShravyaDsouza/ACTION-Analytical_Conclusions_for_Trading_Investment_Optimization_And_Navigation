# ACTION: Analytical Conclusions for Trading Investment Optimization & Navigation

**Description:**

ACTION is a platform designed to provide users with actionable insights for optimizing their trading and investment strategies. It leverages real-time data analysis and provides tools for portfolio management, watchlist tracking, and news aggregation.

**Key Features:**

* **User Management:** Secure user authentication and profile management.
* **Portfolio Tracking:** Real-time tracking of holdings, including equity value and average purchase price.
* **Watchlist Management:** Creation and monitoring of personalized watchlists.
* **Real-time Stock Data:** Caching and display of up-to-date stock prices and changes.
* **News Aggregation:** Access to relevant financial news related to tracked stocks.

## Schema Design

**Database:** (Specify the database you are using, e.g., MySQL, PostgreSQL)

**Tables:**

### `users`

* **Purpose:** Stores user account information.
* **Columns:**
    * `user_id` (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each user.
    * `name` (VARCHAR(100), NOT NULL): User's full name.
    * `email` (VARCHAR(100), UNIQUE, NOT NULL): User's email address (unique).
    * `mobile_no` (VARCHAR(15), NOT NULL): User's mobile number.
    * `password_hash` (VARCHAR(255), NOT NULL): Hashed password for secure authentication.

```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mobile_no VARCHAR(15) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);
```

### `holdings`
* **Purpose:** Tracks user's stock holdings.
* **Columns:**
    * `holding_id` (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each holding.
    * `user_id` (INT, NOT NULL, FOREIGN KEY referencing users(user_id)): User ID associated with the holding.
    * `stock_symbol` (VARCHAR(10), NOT NULL): Stock symbol (e.g., AAPL).
    * `company_name` (VARCHAR(100)): Company name.
    * `shares_owned` (DECIMAL(10, 2), NOT NULL): Number of shares owned.
    * `average_price` (DECIMAL(10, 2)): Average purchase price per share.
    * `equity_value` (DECIMAL(12, 2)): Total equity value of the holding.
    * `Foreign Key`: user_id references users(user_id) with ON DELETE CASCADE.

```sql
CREATE TABLE holdings (
    holding_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    stock_symbol VARCHAR(10) NOT NULL,
    company_name VARCHAR(100),
    shares_owned DECIMAL(10, 2) NOT NULL,
    average_price DECIMAL(10, 2),
    equity_value DECIMAL(12, 2),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);
```

### `watchlist`
* **Purpose:** Stores user's watchlist of stocks.
* **Columns:**
    * `watchlist_id` (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each watchlist entry.
    * `user_id` (INT, NOT NULL, FOREIGN KEY referencing users(user_id)): User ID associated with the watchlist. stock_symbol (VARCHAR(10), NOT NULL): Stock symbol.
    * `company_name` (VARCHAR(100)): Company name. 
    * `added_on` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Timestamp when the stock was added to the watchlist.
    * `Foreign Key`: user_id references users(user_id) with ON DELETE CASCADE.

```sql
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

### `cached_prices`
* **Purpose**: Stores cached real-time stock prices.
* **Columns**:
   * `stock_symbol` (VARCHAR(10), PRIMARY KEY): Stock symbol.
   * `last_price` (DECIMAL(10, 2)): Last traded price.
   * `change_amount` (DECIMAL(10, 2)): Change in price.
   * `percent_change` (DECIMAL(5, 2)): Percentage change in price.
   * `last_updated` (DATETIME): Timestamp when the price was last updated.
```sql 
CREATE TABLE cached_prices (
    stock_symbol VARCHAR(10) PRIMARY KEY,
    last_price DECIMAL(10, 2),
    change_amount DECIMAL(10, 2),
    percent_change DECIMAL(5, 2),
    last_updated DATETIME
);
```

### `cached_news`
* **Purpose**: Stores cached financial news related to stocks.
* **Columns**:
   * `news_id` (INT, PRIMARY KEY, AUTO_INCREMENT): Unique identifier for each news article.
   * `stock_symbol` (VARCHAR(10)): Stock symbol related to the news.
   * `headline` (TEXT): News headline.
   * `source` (VARCHAR(100)): News source.
   * `link` (TEXT): Link to the news article.
   * `published_at` (DATETIME): Timestamp when the news was published.
``` sql
CREATE TABLE cached_news (
    news_id INT PRIMARY KEY AUTO_INCREMENT,
    stock_symbol VARCHAR(10),
    headline TEXT,
    source VARCHAR(100),
    link TEXT,
    published_at DATETIME
);
```

```
mysql -u root -p
```
```sql
CREATE DATABASE IF NOT EXISTS stocks;
```
```sql 
USE stocks;
```
```sql
show columns from users;
```
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
CREATE INDEX idx_symbol ON stock_data(symbol);
ALTER TABLE stock_data ADD COLUMN year INT GENERATED ALWAYS AS (YEAR(Date)) STORED;
CREATE INDEX idx_symbol_year ON stock_data(symbol, year);


API : https://mvnrepository.com/artifact/com.yahoofinance-api/YahooFinanceAPI