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

INSERT INTO localization_strings (`key`, value, language) VALUES
                                                              ('item', 'Item', 'en-US'),
                                                              ('price', 'Enter price', 'en-US'),
                                                              ('quantity', 'Enter quantity', 'en-US'),
                                                              ('add_more', 'Add more items', 'en-US'),
                                                              ('cart_summary', 'Shopping Cart Summary', 'en-US'),
                                                              ('total_cost', 'Total Cost', 'en-US'),
                                                              ('invalid_price', 'Invalid price. Please enter a valid number.', 'en-US'),
                                                              ('invalid_quantity', 'Invalid quantity. Please enter a valid integer.', 'en-US'),
                                                              ('empty_cart', 'Your shopping cart is empty.', 'en-US'),
                                                              ('small_total', 'You have a small purchase. Consider adding more items!', 'en-US'),
                                                              ('medium_total', 'Good selection of items!', 'en-US'),
                                                              ('large_total', 'Wow! That''s a large purchase. Thank you for your business!', 'en-US'),
                                                              ('thank_you', 'Thank you for shopping with us!', 'en-US'),
                                                              ('error', 'Error', 'en-US');