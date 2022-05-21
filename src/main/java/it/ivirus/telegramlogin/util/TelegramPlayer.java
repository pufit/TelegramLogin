package it.ivirus.telegramlogin.util;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
public class TelegramPlayer {
    private final String playerUUID;
    @Setter
    private String chatID;
    @Setter
    private boolean locked;
    private final Date registrationDate;
    @Setter
    private String playerIp;
    private final int accountId;

    public TelegramPlayer(String playerUUID, String chatID, boolean locked, Date registrationDate, int accountId, String playerIp) {
        this(playerUUID, chatID, locked, registrationDate, accountId);
        this.playerIp = playerIp;
    }

    public TelegramPlayer(String playerUUID, String chatID, boolean locked, Date registrationDate, int accountId) {
        this.playerUUID = playerUUID;
        this.chatID = chatID;
        this.locked = locked;
        this.accountId = accountId;
        this.registrationDate = registrationDate;
    }
}
