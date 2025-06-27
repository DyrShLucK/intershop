CREATE TABLE products (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(255),
                          description VARCHAR(255),
                          price DECIMAL(19, 2),
                          image BLOB
);

CREATE TABLE cart (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      total_amount DECIMAL(19, 2),
                      user_name VARCHAR(255)
);

CREATE TABLE cart_items (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            product_id BIGINT,
                            quantity INT,
                            cart_id BIGINT,
                            FOREIGN KEY (product_id) REFERENCES products(id),
                            FOREIGN KEY (cart_id) REFERENCES cart(id)
);

CREATE TABLE orders (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        order_date TIMESTAMP,
                        total_amount DECIMAL(19, 2),
                        user_name VARCHAR(255)
);

CREATE TABLE order_items (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             product_id BIGINT,
                             order_id BIGINT,
                             quantity INT,
                             price DECIMAL(19, 2),
                             FOREIGN KEY (product_id) REFERENCES products(id),
                             FOREIGN KEY (order_id) REFERENCES orders(id)
);