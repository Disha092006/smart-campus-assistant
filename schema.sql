-- OPTIONAL: run this in MySQL Workbench if you want to create the tables yourself.
-- (The app also creates them automatically on first start.)
CREATE DATABASE IF NOT EXISTS campus_db;
USE campus_db;

CREATE TABLE IF NOT EXISTS users(
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  salt VARCHAR(64) NOT NULL,
  hash VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS subjects(
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  attended INT DEFAULT 0,
  total INT DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS tasks(
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  title VARCHAR(200) NOT NULL,
  type VARCHAR(30),
  due VARCHAR(20),
  done INT DEFAULT 0,
  reminded INT DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS expenses(
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  description VARCHAR(200),
  paid_by VARCHAR(50),
  amount DOUBLE,
  participants VARCHAR(300),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS exams(
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  subject VARCHAR(100),
  exam_date VARCHAR(12),
  difficulty INT,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Handy queries to show your teacher the data is really stored:
-- SELECT * FROM users;
-- SELECT * FROM subjects;
-- SELECT * FROM tasks;