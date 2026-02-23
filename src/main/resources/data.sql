-- Initial seed data
MERGE INTO users (id, battle_tag, email, password, role) KEY (id) VALUES (1, 'Admin#1234', 'admin@example.com', '123456', 'admin');
