-- MySQL 8+ initialization script
-- Usage:
--   mysql -u root -p < init-db.sql

DROP DATABASE IF EXISTS messagerie_association;
CREATE DATABASE messagerie_association
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE messagerie_association;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  CONSTRAINT chk_users_role
    CHECK (role IN ('ORGANISATEUR', 'MEMBRE', 'BENEVOLE')),
  CONSTRAINT chk_users_status
    CHECK (status IN ('ONLINE', 'OFFLINE'))
) ENGINE=InnoDB;

CREATE TABLE messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  sender_id BIGINT NOT NULL,
  recipient_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  timestamp DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_messages_sender_timestamp (sender_id, timestamp),
  KEY idx_messages_recipient_timestamp (recipient_id, timestamp),
  KEY idx_messages_status_recipient (status, recipient_id),
  CONSTRAINT fk_messages_sender
    FOREIGN KEY (sender_id) REFERENCES users(id),
  CONSTRAINT fk_messages_recipient
    FOREIGN KEY (recipient_id) REFERENCES users(id),
  CONSTRAINT chk_messages_status
    CHECK (status IN ('ENVOYE', 'RECU', 'LU'))
) ENGINE=InnoDB;
