package dev.cobblesword.skyblock.library.modules.playersync.database;

import dev.cobblesword.skyblock.library.database.Database;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.UUID;

public class PlayerSyncRepository
{
    private Database database;

    public PlayerSyncRepository(Database database)
    {
        this.database = database;
        createTables();
    }

    private Connection getConnection() throws SQLException
    {
        return this.database.getConnection();
    }

    public void createTables()
    {
        System.out.println("Creating player_data table");
        String createTableQuery = "CREATE TABLE IF NOT EXISTS player_data (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "inventory_base64 TEXT, " +
                "inventory_selected_slot INTEGER, " +
                "gamemode VARCHAR(50), " +  // 'survival', 'creative', 'adventure', etc.
                "health DOUBLE PRECISION, " +  // Health value, can be a decimal
                "food_level INTEGER, " +  // Food level, typically an integer
                "saturation DOUBLE PRECISION, " +  // Saturation level, can be a decimal
                "exp DOUBLE PRECISION, " +  // Total experience points, can be a decimal
                "level INTEGER, " +  // Experience level, typically an integer
                "fire_ticks INTEGER, " +  // Number of ticks a player will be on fire
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();  // Assuming getConnection() is a method that returns a SQL connection
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        } catch (SQLException e) {
            System.err.println("Error creating player_data table: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void savePlayerData(UUID playerUuid, String inventoryBase64, int inventorySeletedSlot, String gamemode, double health, int foodLevel, double saturation, double exp, int level, int fireTicks) {
        String query = "INSERT INTO player_data (player_uuid, inventory_base64, inventory_selected_slot, gamemode, health, food_level, saturation, exp, level, fire_ticks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (player_uuid) DO UPDATE SET " +
                "inventory_base64 = EXCLUDED.inventory_base64, inventory_selected_slot = EXCLUDED.inventory_selected_slot, gamemode = EXCLUDED.gamemode, health = EXCLUDED.health, " +
                "food_level = EXCLUDED.food_level, saturation = EXCLUDED.saturation, exp = EXCLUDED.exp, " +
                "level = EXCLUDED.level, fire_ticks = EXCLUDED.fire_ticks";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, inventoryBase64);
            pstmt.setInt(3, inventorySeletedSlot);
            pstmt.setString(4, gamemode);
            pstmt.setDouble(5, health);
            pstmt.setInt(6, foodLevel);
            pstmt.setDouble(7, saturation);
            pstmt.setDouble(8, exp);
            pstmt.setInt(9, level);
            pstmt.setLong(10, fireTicks);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving player data: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    public class PlayerData {
        private String inventoryBase64;
        private int inventorySelectedSlot;
        private String gamemode;
        private double health;
        private int foodLevel;
        private double saturation;
        private double exp;
        private int level;
        private int fireTicks;

        public PlayerData(String inventoryBase64, int inventorySelectedSlot, String gamemode, double health, int foodLevel, double saturation, double exp, int level, int fireTicks) {
            this.inventoryBase64 = inventoryBase64;
            this.inventorySelectedSlot = inventorySelectedSlot;
            this.gamemode = gamemode;
            this.health = health;
            this.foodLevel = foodLevel;
            this.saturation = saturation;
            this.exp = exp;
            this.level = level;
            this.fireTicks = fireTicks;
        }
    }

    public PlayerData getPlayerData(UUID playerUuid) {
        String query = "SELECT * FROM player_data WHERE player_uuid = ?";
        PlayerData playerData = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerUuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    playerData = new PlayerData(
                            rs.getString("inventory_base64"),
                            rs.getInt("inventory_selected_slot"),
                            rs.getString("gamemode"),
                            rs.getDouble("health"),
                            rs.getInt("food_level"),
                            rs.getDouble("saturation"),
                            rs.getDouble("exp"),
                            rs.getInt("level"),
                            rs.getInt("fire_ticks")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving player data: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return playerData;
    }
}
