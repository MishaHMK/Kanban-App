UPDATE kanban_columns kc
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY board_id ORDER BY position) AS new_position
    FROM kanban_columns
) ranked ON kc.id = ranked.id
SET kc.position = -ranked.new_position;

UPDATE kanban_columns
SET position = -position;

UPDATE tasks t
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY column_id ORDER BY position) AS new_position
    FROM tasks
) ranked ON t.id = ranked.id
SET t.position = -ranked.new_position;

UPDATE tasks
SET position = -position;