CREATE TABLE board_collaborators (
    board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (board_id, user_id),
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);