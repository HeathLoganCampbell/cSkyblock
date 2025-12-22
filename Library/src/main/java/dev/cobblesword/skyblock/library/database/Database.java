package dev.cobblesword.skyblock.library.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database
{
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/skyblock";
    private static final String DATABASE_USER = "skyblock";
    private static final String DATABASE_PASSWORD = "skyblock";

    private HikariDataSource dataSource;

    public Database()
    {
        this.init();
    }

    public void init()
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DATABASE_URL);
        config.setUsername(DATABASE_USER);
        config.setPassword(DATABASE_PASSWORD);
        config.setDriverClassName("org.postgresql.Driver");

        // Additional HikariCP settings (optional)
        config.setMaximumPoolSize(10); // Example: maximum number of connections in the pool
        config.setMinimumIdle(2);      // Example: minimum number of idle connections maintained by HikariCP

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException
    {
        return this.dataSource.getConnection();
    }
}
