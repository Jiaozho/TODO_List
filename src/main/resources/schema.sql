CREATE TABLE IF NOT EXISTS todo_item (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  completed BOOLEAN NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  INDEX idx_todo_item_created_at (created_at),
  INDEX idx_todo_item_completed (completed)
);
