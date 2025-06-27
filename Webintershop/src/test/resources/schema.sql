DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(255),
    description VARCHAR(255),
    price DECIMAL(19, 2),
    image BLOB
    );

CREATE TABLE IF NOT EXISTS cart (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    user_name VARCHAR(255) NOT NULL,
    CONSTRAINT unique_username UNIQUE (user_name)
    );

CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          product_id BIGINT NOT NULL,
                                          quantity INT NOT NULL CHECK (quantity > 0),
    cart_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT unique_cart_product UNIQUE (cart_id, product_id)
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      total_amount DECIMAL(19, 2) NOT NULL CHECK (total_amount >= 0),
    user_name VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           product_id BIGINT NOT NULL,
                                           order_id BIGINT NOT NULL,
                                           quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(19, 2) NOT NULL CHECK (price >= 0),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
    );