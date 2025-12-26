CREATE TABLE IF NOT EXISTS todo_item (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  category VARCHAR(64),
  priority TINYINT NOT NULL DEFAULT 2,
  due_date DATETIME(3),
  completed BOOLEAN NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL
);
