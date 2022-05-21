package it.ivirus.telegramlogin.database.remote;

import com.zaxxer.hikari.HikariDataSource;
import it.ivirus.telegramlogin.TelegramLogin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQL extends ConnectionPoolManager {
    private final String database, username, password, host;
    private final boolean ssl;
    private final int port;

    public MySQL(TelegramLogin plugin, String database, String username, String password, String host, int port, boolean ssl) {
        super(plugin);
        this.database = database;
        this.host = host;
        this.password = password;
        this.username = username;
        this.port = port;
        this.ssl = ssl;
    }

    @Override
    public Connection getJdbcUrl() throws SQLException {
        getHikariConfig().setJdbcUrl(
                "jdbc:mysql://" +
                        this.host +
                        ":" +
                        this.port +
                        "/" +
                        this.database +
                        "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useSSL=" +
                        this.ssl
        );
        getHikariConfig().setUsername(this.username);
        getHikariConfig().setPassword(this.password);
        setDataSource(new HikariDataSource(getHikariConfig()));

        return getDataSource().getConnection();
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (this.getDataSource() == null) {
            throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
        }

        Connection connection = this.getDataSource().getConnection();
        if (connection == null) {
            throw new SQLException("Unable to get a connection from the pool. (getConnection returned null)");
        }

        return connection;
    }

    @Override
    public void createTables() {
        try (Connection connection = getConnection();
             PreparedStatement data = connection.prepareStatement("create TABLE if not exists " + TABLE_PLAYERS + " " +
                     "(AccountId INT NOT NULL AUTO_INCREMENT, PlayerUUID VARCHAR(100), PlayerName VARCHAR(100), ChatID VARCHAR(100), Locked BOOLEAN NOT NULL," +
                     "RegistrationDate DATETIME NOT NULL, PRIMARY KEY(AccountId))")) {

            data.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
