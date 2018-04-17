CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE customer (
  id BIGINT NOT NULL,
  created_on TIMESTAMP,
  name VARCHAR(255),
  status VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE customer_notes (
  customer_id BIGINT NOT NULL,
  note VARCHAR(255)
);

ALTER TABLE customer_notes ADD CONSTRAINT FK_CUSTOMER_NOTES_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customer;
