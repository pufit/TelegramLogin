package it.ivirus.telegramlogin.telegram.callbackmanager.textcommand;

import it.ivirus.telegramlogin.telegram.TelegramBot;
import it.ivirus.telegramlogin.telegram.callbackmanager.AbstractUpdate;
import it.ivirus.telegramlogin.util.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.UUID;

public class ConfirmTextCommand extends AbstractUpdate {
    @Override
    public void onUpdateCall(TelegramBot bot, Update update, String[] args) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        try {
            int accountId;
            if (update.getMessage().isReply()) {
                String message = update.getMessage().getReplyToMessage().getText();
                String accountIdString = message.split(" ")[0]
                        .replaceAll("[^a-zA-Z0-9]", "");

                if (!NumberUtils.isDigits(accountIdString)) {
                    throw new IllegalArgumentException("Invalid AccountId");
                }

                int messageRepliedId = update.getMessage().getReplyToMessage().getMessageId();
                DeleteMessage deleteMessage = new DeleteMessage(chatId, messageRepliedId);
                bot.execute(deleteMessage);

                accountId = Integer.parseInt(accountIdString);

            } else if (args.length == 2) {
                accountId = Integer.parseInt(args[1]);
            } else {
                bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_CONFIRM_USAGE.getString()));
                return;
            }

            UUID playerUUID = null;
            boolean found = false;
            for (Map.Entry<UUID, TelegramPlayer> entry : playerData.getPlayerInLogin().entrySet()) {
                if (entry.getValue().getAccountId() == accountId && entry.getValue().getChatID().equals(chatId)) {
                    playerUUID = entry.getKey();
                    found = true;
                    break;
                }
            }

            Player player = Bukkit.getPlayer(playerUUID);

            if (!found || playerUUID == null || player == null) {
                bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_ACCOUNTID_NOT_LINKED.getString()));
                return;
            }

            playerData.getPlayerInLogin().remove(playerUUID);

            if (plugin.isBungeeEnabled())
                Util.sendPluginMessage(player, PluginMessageAction.REMOVE);
            playerData.getPlayerCache().get(playerUUID).setPlayerIp(player.getAddress().getHostString());
            bot.execute((MessageFactory.simpleMessage(chatId, LangConstants.TG_LOGIN_EXECUTED.getString())));
            player.sendMessage(LangConstants.INGAME_LOGIN_EXECUTED.getFormattedString());


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
