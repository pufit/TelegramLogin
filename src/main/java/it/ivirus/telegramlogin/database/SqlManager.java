package it.ivirus.telegramlogin.database;

import it.ivirus.telegramlogin.TelegramLogin;
import it.ivirus.telegramlogin.util.TelegramPlayer;
import it.ivirus.telegramlogin.util.TelegramPlayerInfo;
import lombok.Getter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class SqlManager {
    protected final TelegramLogin plugin;
    protected final String TABLE_PLAYERS = "Player";
    protected Connection connection;

    public SqlManager(TelegramLogin plugin) {
        this.plugin = plugin;
    }

    public void setup() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            setConnection(getJdbcUrl());
            this.createTables();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public abstract void createTables() throws SQLException;

    public abstract Connection getConnection() throws SQLException, ClassNotFoundException;

    public void addPlayerLogin(String playerUUID, String playerName, String chatId, Date registrationDate) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + TABLE_PLAYERS + " (PlayerUUID, PlayerName, ChatID, Locked, RegistrationDate)" +
                " values (?,?,?,?,?)")) {
            statement.setString(1, playerUUID);
            statement.setString(2, playerName);
            statement.setString(3, chatId);
            statement.setBoolean(4, false);
            statement.setDate(5, registrationDate);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public CompletableFuture<TelegramPlayer> getTelegramPlayer(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + " WHERE PlayerUUID=?")) {
                statement.setString(1, playerUUID);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return new TelegramPlayer(resultSet.getString("PlayerUUID"), resultSet.getString("ChatID"), resultSet.getBoolean("Locked"), resultSet.getDate("RegistrationDate"), resultSet.getInt("AccountId"));
                } else {
                    return null;
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return null;
        }, plugin.getExecutor());
    }

    public CompletableFuture<Integer> getTelegramPlayersCount(String chatId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) as accounts_count FROM " + TABLE_PLAYERS + " WHERE chatID=?")) {
                statement.setString(1, chatId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("accounts_count");
                } else {
                    return 0;
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return 0;
        }, plugin.getExecutor());
    }

    public CompletableFuture<TelegramPlayer> getTelegramPlayer(String chatId, int accountId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + " WHERE ChatID=? AND AccountId=?")) {
                statement.setString(1, chatId);
                statement.setInt(2, accountId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return new TelegramPlayer(resultSet.getString("PlayerUUID"), resultSet.getString("ChatID"), resultSet.getBoolean("Locked"), resultSet.getDate("RegistrationDate"), resultSet.getInt("AccountId"));
                } else {
                    return null;
                }
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return null;
        }, plugin.getExecutor());
    }

    public CompletableFuture<List<TelegramPlayerInfo>> getTelegramPlayerInfoList(String chatId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + TABLE_PLAYERS + " WHERE ChatID=?")) {
                statement.setString(1, chatId);
                ResultSet resultSet = statement.executeQuery();
                List<TelegramPlayerInfo> telegramPlayerList = new ArrayList<>();
                while (resultSet.next()){
                    telegramPlayerList.add(new TelegramPlayerInfo(resultSet.getInt("AccountId"), resultSet.getString("PlayerName"),resultSet.getBoolean("Locked")));
                }
                return telegramPlayerList;
            } catch (SQLException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return null;
        }, plugin.getExecutor());
    }

    public void removePlayerLogin(String playerUUID) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + TABLE_PLAYERS + " WHERE PlayerUUID=?")) {
            statement.setString(1, playerUUID);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void setChatId(String playerUUID, String chatId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET ChatId=? WHERE PlayerUUID=?")) {
            statement.setString(1, chatId);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void setLockPlayer(String playerUUID, boolean value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET Locked=? WHERE PlayerUUID=?")) {
            statement.setBoolean(1, value);
            statement.setString(2, playerUUID);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void setLockPlayer(int accountId, boolean value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET Locked=? WHERE AccountId=?")) {
            statement.setBoolean(1, value);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void setLockPlayerByChatId(String chatid, boolean value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE " + TABLE_PLAYERS + " SET Locked=? WHERE ChatId=?")) {
            statement.setBoolean(1, value);
            statement.setString(2, chatid);
            statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected abstract Connection getJdbcUrl() throws SQLException, ClassNotFoundException;
}
