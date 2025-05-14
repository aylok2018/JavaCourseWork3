
DROP TABLE IF EXISTS ratings, user_quotes, user_submitted_quotes, quotes, categories, users CASCADE;
SET search_path TO public;

CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE quotes (
                        id SERIAL PRIMARY KEY,
                        text TEXT NOT NULL,
                        author VARCHAR(255),
                        category_id INT REFERENCES categories(id),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        active BOOLEAN DEFAULT TRUE
);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL
);

CREATE TABLE ratings (
                         id SERIAL PRIMARY KEY,
                         user_id INT REFERENCES users(id),
                         quote_id INT REFERENCES quotes(id),
                         rating INT CHECK(rating >= 1 AND rating <= 5)
);

CREATE TABLE user_quotes (
                             id SERIAL PRIMARY KEY,
                             user_id INT REFERENCES users(id),
                             quote_id INT REFERENCES quotes(id)
);

CREATE TABLE user_submitted_quotes (
                                       id SERIAL PRIMARY KEY,
                                       user_id INT REFERENCES users(id),
                                       quote_id INT REFERENCES quotes(id)
);

INSERT INTO categories (name) VALUES
                                  ('Motivation'),
                                  ('Love'),
                                  ('Life');

INSERT INTO users (name, email, password, role) VALUES
                                                    ('Alice', 'alice@example.com', 'password123', 'ROLE_USER'),
                                                    ('Bob', 'bob@example.com', 'password456', 'ROLE_ADMIN'),
                                                    ('Charlie', 'charlie@example.com', 'password789', 'ROLE_USER');

INSERT INTO quotes (text, author, category_id) VALUES
                                                   ('The best way to predict the future is to create it.', 'Peter Drucker', 1),
                                                   ('Love yourself first and everything else falls into line.', 'Lucille Ball', 2),
                                                   ('Life is what happens when you are busy making other plans.', 'John Lennon', 3),
                                                   ('Success is not in what you have, but who you are.', 'Bo Bennett', 1),
                                                   ('Where there is love there is life.', 'Mahatma Gandhi', 2),
                                                   ('I am delete :(', 'Test', 2);


INSERT INTO ratings (user_id, quote_id, rating) VALUES
                                                    (1, 1, 5),
                                                    (2, 2, 4),
                                                    (3, 3, 3);

INSERT INTO user_quotes (user_id, quote_id) VALUES
                                                (1, 1),
                                                (1, 3),
                                                (2, 2),
                                                (3, 5);

INSERT INTO user_submitted_quotes (user_id, quote_id) VALUES
                                                          (1, 4),
                                                          (2, 5);
