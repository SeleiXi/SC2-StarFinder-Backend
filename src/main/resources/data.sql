-- Initial seed data
MERGE INTO users (id, nickname, email, password, role) KEY (id) VALUES (1, 'Admin', 'admin@example.com', '123456', 'admin');
