package dev.cobblesword.skyblock.library.modules.remoteworlds.databases;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.modules.remoteworlds.models.SoftWorld;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLWorldDatabase implements IWorldDatabase
{
    public MySQLWorldDatabase() {
        createTables();
    }

    private Connection getConnection() throws SQLException
    {
        return SkyblockLibrary.get().getDatabase().getConnection();
    }

    public void createTables() {
        System.out.println("creating world_data table");
        String createTableQuery = "CREATE TABLE IF NOT EXISTS world_data (" +
                "world_id SERIAL PRIMARY KEY, " +
                "world_name VARCHAR(255) UNIQUE NOT NULL, " +
                "world_zip BYTEA)";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableQuery);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    public void uploadWorldFile(int worldId, File fileToUpload)
//    {
//        System.out.println("Upload world " + fileToUpload);
//        try (Connection conn = getConnection();
//             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO world_data (world_name, world_zip) VALUES (?, ?)")) {
//            pstmt.setString(1, worldId + "test");
//            pstmt.setBlob(2, new FileInputStream(fileToUpload));
//            pstmt.executeUpdate();
//        } catch (SQLException | FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
    @Override
    public void createWorldFile(String worldName, File file) {
        String insertQuery = "INSERT INTO world_data (world_name, world_zip) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery);
             FileInputStream fis = new FileInputStream(file)) {

            // Set the world name
            pstmt.setString(1, worldName);

            // Stream the file directly into the PreparedStatement
            pstmt.setBinaryStream(2, fis, (int) file.length());

            // Execute the insert command
            pstmt.executeUpdate();
            System.out.println("World file inserted successfully");

        } catch (SQLException | IOException e) {
            System.err.println("Error inserting world file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateWorldFile(String worldName, File fileToUpload)
    {
        String updateQuery = "UPDATE world_data SET world_zip = ? WHERE world_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery);
             FileInputStream fis = new FileInputStream(fileToUpload)) {

            // Stream the new file content directly into the PreparedStatement
            pstmt.setBinaryStream(1, fis, (int) fileToUpload.length());

            // Set the world ID for which the record needs to be updated
            pstmt.setString(2, worldName);

            // Execute the update command
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.out.println("World file update failed: No such world ID found.");
            } else {
                System.out.println("World file updated successfully for ID: " + worldName);
            }

        } catch (SQLException | IOException e) {
            System.err.println("Error updating world file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void downloadWorldFile(String worldName, File distFile) {
        String query = "SELECT world_name, world_zip FROM world_data WHERE world_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, worldName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] worldZipBytes = rs.getBytes("world_zip");

                    try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(distFile))) {
                        fos.write(worldZipBytes);
                    }
                }
            }

        } catch (SQLException | IOException e) {
            System.err.println("Error downloading world file: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInDatabase(String worldName) {
        String query = "SELECT 1 FROM world_data WHERE world_name = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, worldName);

            try (ResultSet rs = pstmt.executeQuery()) {
                // If the result set is not empty, return true
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if world is in database: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<SoftWorld> getRemoteWorlds(int page, int limit) {
        String query = "SELECT world_id, world_name, is_loaded FROM world_data ORDER BY world_id ASC LIMIT ? OFFSET ?";

        List<SoftWorld> worlds = new ArrayList<>();
        int offset = (page - 1) * limit;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SoftWorld world = new SoftWorld();
                    world.setWorldId(rs.getInt("world_id"));
                    world.setWorldName(rs.getString("world_name"));
                    world.setLoaded(rs.getBoolean("is_loaded"));

                    worlds.add(world);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving remote worlds: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return worlds;
    }
}
