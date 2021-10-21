package it.ivirus.telegramlogin.data;

import it.ivirus.telegramlogin.TelegramLogin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

@RequiredArgsConstructor
public class Task {
    private final TelegramLogin plugin;

    public void startClearCacheTask(){
        long l = 20 * 60 * 20;
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : PlayerData.getInstance().getPlayerCache().keySet()){
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null)
                        PlayerData.getInstance().getPlayerCache().remove(uuid);
                }
            }
        },l,l);
    }


}
