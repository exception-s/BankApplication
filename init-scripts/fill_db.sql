\c localbank_db;

INSERT INTO users (username, password, email) VALUES
('alexander', '$2a$10$ExampleHash1234567890', 'alex@bank.com'),
('maria', '$2a$10$ExampleHash1234567890', 'maria@bank.com'),
('admin', '$2a$10$AdminHash1234567890', 'admin@bank.com');

INSERT INTO user_roles (user_id, role) VALUES
(1, 'USER'),
(2, 'USER'),
(3, 'ADMIN');

INSERT INTO bank_accounts (user_id, account_number, balance, currency) VALUES
(1, 'ACC-1001', 5000.00, 'USD'),
(1, 'ACC-1002', 2500.00, 'EUR'),
(2, 'ACC-2001', 7500.00, 'USD'),
(3, 'ACC-3001', 15000.00, 'USD');

INSERT INTO transactions (from_account_id, to_account_id, amount, type, from_currency, to_currency, description) VALUES
(null, 1, 5000.00, 'DEPOSIT', 'USD', 'USD', '''Initial deposit'),
(null, 2, 2500.00, 'DEPOSIT', 'USD', 'USD', 'Euro account opening'),
(null, 3, 7500.00, 'DEPOSIT', 'USD', 'USD',  'Initial deposit'),
(null, 4, 15000.00, 'DEPOSIT', 'USD', 'USD',  'Admin account funding'),
(1, 3, 500.00, 'TRANSFER', 'USD', 'USD',  'Gift to Maria'),
(3, 1, 250.00, 'TRANSFER', 'USD', 'USD',  'Lunch repayment'),
(1, null, 200.00, 'WITHDRAWAL', 'USD', 'USD',  'ATM withdrawal'),
(3, 4, 1000.00, 'TRANSFER', 'USD', 'USD',  'Service payment');

UPDATE bank_accounts SET balance = 4550.00 WHERE id = 1;
UPDATE bank_accounts SET balance = 2500.00 WHERE id = 2;
UPDATE bank_accounts SET balance = 7750.00 WHERE id = 3;
UPDATE bank_accounts SET balance = 16000.00 WHERE id = 4;