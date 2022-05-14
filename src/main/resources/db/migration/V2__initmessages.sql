CREATE TABLE messages
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    external_id VARCHAR(50),
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT ext_messages unique (external_id)
);
