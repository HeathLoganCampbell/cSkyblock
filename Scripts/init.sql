-- Initialize Skyblock database schema

CREATE TABLE IF NOT EXISTS islands (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    server_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS players (
    id UUID PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    island_id UUID REFERENCES islands(id),
    balance DECIMAL(15, 2) DEFAULT 0,
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_islands_owner ON islands(owner_id);
CREATE INDEX IF NOT EXISTS idx_players_island ON players(island_id);

