DROP TABLE IF EXISTS todo_item;

CREATE TABLE todo_item (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  category VARCHAR(64),
  priority TINYINT NOT NULL DEFAULT 2,
  due_date DATE,
  completed BOOLEAN NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL
);

CREATE INDEX idx_todo_item_created_at ON todo_item (created_at);
CREATE INDEX idx_todo_item_completed ON todo_item (completed);
CREATE INDEX idx_todo_item_category ON todo_item (category);
CREATE INDEX idx_todo_item_priority ON todo_item (priority);
CREATE INDEX idx_todo_item_due_date ON todo_item (due_date);
