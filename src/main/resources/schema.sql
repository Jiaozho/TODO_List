CREATE TABLE IF NOT EXISTS todo_item (
  id VARCHAR(64) PRIMARY KEY,
  title VARCHAR(120) NOT NULL,
  description VARCHAR(500),
  category VARCHAR(64),
  priority TINYINT NOT NULL DEFAULT 2,
  due_date DATETIME(3),
  completed BOOLEAN NOT NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  INDEX idx_todo_item_created_at (created_at),
  INDEX idx_todo_item_completed (completed),
  INDEX idx_todo_item_category (category),
  INDEX idx_todo_item_priority (priority),
  INDEX idx_todo_item_due_date (due_date)
);

SET @col_exists_category := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND COLUMN_NAME = 'category'
);
SET @sql_category := IF(@col_exists_category = 0, 'ALTER TABLE todo_item ADD COLUMN category VARCHAR(64) NULL AFTER description', 'SELECT 1');
PREPARE stmt_category FROM @sql_category;
EXECUTE stmt_category;
DEALLOCATE PREPARE stmt_category;

SET @col_exists_priority := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND COLUMN_NAME = 'priority'
);
SET @sql_priority := IF(@col_exists_priority = 0, 'ALTER TABLE todo_item ADD COLUMN priority TINYINT NOT NULL DEFAULT 2 AFTER category', 'SELECT 1');
PREPARE stmt_priority FROM @sql_priority;
EXECUTE stmt_priority;
DEALLOCATE PREPARE stmt_priority;

SET @col_exists_due_date := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND COLUMN_NAME = 'due_date'
);
SET @sql_due_date := IF(@col_exists_due_date = 0, 'ALTER TABLE todo_item ADD COLUMN due_date DATETIME(3) NULL AFTER priority', 'SELECT 1');
PREPARE stmt_due_date FROM @sql_due_date;
EXECUTE stmt_due_date;
DEALLOCATE PREPARE stmt_due_date;

ALTER TABLE todo_item MODIFY COLUMN due_date DATETIME(3) NULL;

SET @idx_exists_category := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND INDEX_NAME = 'idx_todo_item_category'
);
SET @sql_idx_category := IF(@idx_exists_category = 0, 'CREATE INDEX idx_todo_item_category ON todo_item (category)', 'SELECT 1');
PREPARE stmt_idx_category FROM @sql_idx_category;
EXECUTE stmt_idx_category;
DEALLOCATE PREPARE stmt_idx_category;

SET @idx_exists_priority := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND INDEX_NAME = 'idx_todo_item_priority'
);
SET @sql_idx_priority := IF(@idx_exists_priority = 0, 'CREATE INDEX idx_todo_item_priority ON todo_item (priority)', 'SELECT 1');
PREPARE stmt_idx_priority FROM @sql_idx_priority;
EXECUTE stmt_idx_priority;
DEALLOCATE PREPARE stmt_idx_priority;

SET @idx_exists_due_date := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'todo_item'
    AND INDEX_NAME = 'idx_todo_item_due_date'
);
SET @sql_idx_due_date := IF(@idx_exists_due_date = 0, 'CREATE INDEX idx_todo_item_due_date ON todo_item (due_date)', 'SELECT 1');
PREPARE stmt_idx_due_date FROM @sql_idx_due_date;
EXECUTE stmt_idx_due_date;
DEALLOCATE PREPARE stmt_idx_due_date;
