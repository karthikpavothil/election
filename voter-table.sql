-- PostgreSQL CREATE TABLE script for voter
CREATE TABLE voter (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(500),
    voted BOOLEAN NOT NULL DEFAULT FALSE,
    party VARCHAR(100),
    confirmed BOOLEAN NOT NULL DEFAULT FALSE
);
