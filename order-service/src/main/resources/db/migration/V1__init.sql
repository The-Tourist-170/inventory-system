CREATE TABLE t_orders
(
    id SERIAL PRIMARY KEY,
    order_no VARCHAR(255) DEFAULT NULL,
    sku_code VARCHAR(255),
    price DECIMAL(19,2),
    quantity INT
);
