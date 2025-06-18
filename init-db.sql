CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        name VARCHAR(255),
    description VARCHAR(255),
    price DECIMAL(19, 2),
    image BYTEA
    );

CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGSERIAL PRIMARY KEY,
                                          product_id BIGINT NOT NULL,
                                          quantity INT NOT NULL,
                                          FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      total_amount DECIMAL(19, 2) NOT NULL
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id BIGSERIAL PRIMARY KEY,
                                           product_id BIGINT NOT NULL,
                                           order_id BIGINT NOT NULL,
                                           quantity INT NOT NULL,
                                           price DECIMAL(19, 2) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );