
## Project Structure

- `src/main/java/W1/DatabaseConnection.java` — DB URL/credentials and connection helper
- `src/main/java/W1/LocalizationService.java` — loads localized key/value text from DB
- `src/main/java/W1/CartService.java` — persists cart record + cart items transactionally
- `src/main/java/W1/ShoppingCartCalculator.java` — CLI app flow using localization + persistence
- `db/schema.sql` — schema creation SQL + seed localization strings
- `Dockerfile` — container build for running the CLI app
- `Jenkinsfile` — CI pipeline for build/test/package/docker

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL or MariaDB
- (Optional) Docker
- (Optional) Jenkins with Docker and Maven/JDK tools configured

## 1) Create Database

Run the SQL script:

```bash
mysql -u root -p < db/schema.sql
```

Equivalent SQL included in the assignment:

```sql
CREATE DATABASE IF NOT EXISTS shopping_cart_localization
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shopping_cart_localization;

CREATE TABLE IF NOT EXISTS cart_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_items INT NOT NULL,
    total_cost DOUBLE NOT NULL,
    language VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cart_record_id INT,
    item_number INT NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (cart_record_id) REFERENCES cart_records(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS localization_strings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    `key` VARCHAR(100) NOT NULL,
    value VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL
);
```

## 2) Configure Environment variables(added Database Connection)

The app reads connection values from system properties first, then environment variables, then defaults:

- `DB_URL` (default: `jdbc:mysql://localhost:3306/shopping_cart_localization?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: empty)
- `JAVA_HOME`
- `MAVEN_HOME`
- `IMAGE_NAME`
- `IMAGE_TAG`
- `DOCKER_HUB_USER`
- `DOCKER_USER`
- `DOCKER_PASS`

Examples:

```bash
export DB_URL='jdbc:mysql://localhost:3306/shopping_cart_localization?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your_password'
```

## 3) Build and Test

```bash
mvn clean test
```

## 4) Run the Application (CLI)

```bash
mvn -q exec:java -Dexec.mainClass=W1.ShoppingCartCalculator
```
## 4.1) Run with GUI
```bash
mvn -q exec:java -Dexec.mainClass=W1.ShoppingCartGUI
```

## 5) Verify Stored Data

After checkout operations in the app:

```sql
SELECT * FROM cart_records ORDER BY id DESC;
SELECT * FROM cart_items ORDER BY id DESC;
```

## Docker

Build image:

```bash
docker build -t shopping-cart-calc:local .
```

Run container (connect to DB exposed on host):

```bash
docker run --rm -it \
  -e DB_URL='jdbc:mysql://host.docker.internal:3306/shopping_cart_localization?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -e DB_USERNAME='root' \
  -e DB_PASSWORD='your_password' \
  shopping-cart-calc:local
```

## Jenkins Pipeline Notes

The `Jenkinsfile` performs:
1. Checkout
2. `mvn clean verify`
3. Test report + artifact archiving (JAR, JaCoCo, schema, Dockerfile, Jenkinsfile)
4. Docker image build
5. Docker push on `main` branch using `docker-hub-credentials`

Expected Jenkins tools:
- Maven installation named `Local Maven 3.9.11`
- JDK installation named `Local JDK-21`

