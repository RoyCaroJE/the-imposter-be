-- Create Room Table
CREATE TABLE room (
    id SERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    code VARCHAR(6) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    admin_public_id UUID NOT NULL,
    num_impostors INT NOT NULL,
    word VARCHAR NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW(),
    started_at TIMESTAMP,
    room_name VARCHAR NOT NULL
);

-- Create Player Table
CREATE TABLE player (
    id SERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    admin BOOLEAN NOT NULL DEFAULT FALSE,
    impostor BOOLEAN NOT NULL DEFAULT FALSE,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    connected BOOLEAN NOT NULL DEFAULT TRUE,
    room_public_id UUID NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT NOW()
);