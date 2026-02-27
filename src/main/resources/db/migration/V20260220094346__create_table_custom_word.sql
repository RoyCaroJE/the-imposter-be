-- Create Custom Word Table
CREATE TABLE custom_word (
    id BIGINT PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    room_public_id UUID NOT NULL,
    player_public_id UUID NOT NULL,
    word VARCHAR(50) NOT NULL,
    picked BOOLEAN NOT NULL DEFAULT FALSE
);