ALTER TABLE users
    ADD COLUMN email VARCHAR(255) NULL UNIQUE;

CREATE TABLE password_reset_token (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    email      VARCHAR(255) NOT NULL,
    token      VARCHAR(6)   NOT NULL,
    expiry_date DATETIME    NOT NULL,
    used       BIT(1)       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;