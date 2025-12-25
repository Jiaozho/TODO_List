CREATE TABLE IF NOT EXISTS todo_item (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  category VARCHAR(64),
  completed BOOLEAN NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  INDEX idx_todo_item_created_at (created_at),
  INDEX idx_todo_item_completed (completed),
  INDEX idx_todo_item_category (category)
);

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND COLUMN_NAME = 'category'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE todo_item ADD COLUMN category VARCHAR(64) NULL AFTER description', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND INDEX_NAME = 'idx_todo_item_category'
);
SET @sql2 := IF(@idx_exists = 0, 'CREATE INDEX idx_todo_item_category ON todo_item (category)', 'SELECT 1');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
