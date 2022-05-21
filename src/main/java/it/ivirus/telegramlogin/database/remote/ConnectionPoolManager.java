package it.ivirus.telegramlogin.database.remote;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.ivirus.telegramlogin.TelegramLogin;
import it.ivirus.telegramlogin.database.SqlManager;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ConnectionPoolManager extends SqlManager {
    @Getter
    @Setter
    private HikariDataSource dataSource;

    @Getter
    private final HikariConfig hikariConfig = new HikariConfig();

    public ConnectionPoolManager(TelegramLogin plugin) {
        super(plugin);
        Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.SEVERE);
        Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.SEVERE);
        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.SEVERE);
        Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.SEVERE);
        Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.SEVERE);
        setupPool();
    }

    private void setupPool() {
        hikariConfig.setConnectionTimeout(30000);
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
