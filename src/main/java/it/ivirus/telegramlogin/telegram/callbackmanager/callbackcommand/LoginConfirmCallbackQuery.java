package it.ivirus.telegramlogin.telegram.callbackmanager.callbackcommand;

import it.ivirus.telegramlogin.util.*;
import it.ivirus.telegramlogin.telegram.TelegramBot;
import it.ivirus.telegramlogin.telegram.callbackmanager.AbstractUpdate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.UUID;

public class LoginConfirmCallbackQuery extends AbstractUpdate {
    @Override
    public void onUpdateCall(TelegramBot bot, Update update, String[] args) {
        String playerUUID = args[1];
        UUID uuid = UUID.fromString(playerUUID);
        Player player = Bukkit.getPlayer(uuid);
        String chatId = args[2];
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        DeleteMessage deleteMessage = new DeleteMessage(chatId, messageId);
        try {
            bot.execute(deleteMessage);
            if (player == null) {
                bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_PLAYER_OFFLINE.getString()));
                return;
            }
            if (plugin.isBungeeEnabled())
                Util.sendPluginMessage(player, PluginMessageAction.REMOVE);
            //playerData.getPlayerCache().get(uuid).setPlayerIp(player.getAddress().getHostString());
            bot.execute((MessageFactory.simpleMessage(chatId, LangConstants.TG_LOGIN_EXECUTED.getString())));
            TelegramPlayer telegramPlayer = playerData.getPlayerInLogin().get(player.getUniqueId());
            playerData.getPlayerCache().put(player.getUniqueId(), telegramPlayer);
            playerData.getPlayerInLogin().remove(uuid);
            player.sendMessage(LangConstants.INGAME_LOGIN_EXECUTED.getFormattedString());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
