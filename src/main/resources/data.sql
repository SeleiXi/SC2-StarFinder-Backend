-- Initial seed data
MERGE INTO users (id, name, battle_tag, email, password, role) KEY (id) VALUES (1, 'admin', 'Admin#1234', 'admin@example.com', '123456', 'admin');
