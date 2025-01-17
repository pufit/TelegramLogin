package it.ivirus.telegramlogin.telegram.callbackmanager.textcommand;

import it.ivirus.telegramlogin.telegram.TelegramBot;
import it.ivirus.telegramlogin.telegram.callbackmanager.AbstractUpdate;
import it.ivirus.telegramlogin.util.LangConstants;
import it.ivirus.telegramlogin.util.MessageFactory;
import it.ivirus.telegramlogin.util.PluginMessageAction;
import it.ivirus.telegramlogin.util.Util;
import org.apache.commons.lang3.math.NumberUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.UUID;

public class UnlockTextCommand extends AbstractUpdate {
    @Override
    public void onUpdateCall(TelegramBot bot, Update update, String[] args) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        try {
            if (args.length != 2) {
                bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_UNLOCK_USAGE.getString()));
                return;
            }
            if (!NumberUtils.isDigits(args[1])) {
                bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_INVALID_VALUE.getString()));
                return;
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        int accountId = Integer.parseInt(args[1]);
        plugin.getSql().getTelegramPlayer(chatId, accountId).whenComplete((telegramPlayer, throwable) -> {
            if (throwable != null)
                throwable.printStackTrace();
        }).thenAccept(telegramPlayer -> {
            try {
                if (telegramPlayer == null) {
                    bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_ACCOUNTID_NOT_LINKED.getString()));
                } else {
                    plugin.getSql().setLockPlayer(accountId, false);
                    if (playerData.getPlayerCache().containsKey(UUID.fromString(telegramPlayer.getPlayerUUID()))) {
                        playerData.getPlayerCache().get(UUID.fromString(telegramPlayer.getPlayerUUID())).setLocked(false);
                    }
                    bot.execute(MessageFactory.simpleMessage(chatId, LangConstants.TG_UNLOCKED_MESSAGE.getString()));
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
    }
}
