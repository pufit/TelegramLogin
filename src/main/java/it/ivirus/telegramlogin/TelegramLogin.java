package it.ivirus.telegramlogin;

import it.ivirus.telegramlogin.data.Task;
import it.ivirus.telegramlogin.database.SQLite;
import it.ivirus.telegramlogin.database.SqlManager;
import it.ivirus.telegramlogin.database.remote.MySQL;
import it.ivirus.telegramlogin.spigot.command.TelegramCommandHandler;
import it.ivirus.telegramlogin.spigot.command.TgCommandTabCompleter;
import it.ivirus.telegramlogin.telegram.TelegramBot;
import it.ivirus.telegramlogin.util.UpdateChecker;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executor;

@Getter
public class TelegramLogin extends JavaPlugin {
    @Getter
    private static TelegramLogin instance;
    private Thread botThread;
    private TelegramBotsApi botsApi;
    private TelegramBot bot;
    private SqlManager sql;
    private File langFile;
    private FileConfiguration langConfig;
    private boolean bungeeEnabled = false;
    private boolean loginSessionEnabled = false;
    private int maxAccountsCount = 0;
    private final Executor executor = runnable -> Bukkit.getScheduler().runTaskAsynchronously(this, runnable);

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.createLangFile("en_US", "it_IT", "ru_RU");
        this.loadLangConfig();

        this.setupDb();

        getCommand("telegramlogin").setExecutor(new TelegramCommandHandler(this));
        getCommand("telegramlogin").setTabCompleter(new TgCommandTabCompleter(this));
        if (this.getConfig().getBoolean("bungee")) {
            this.bungeeEnabled = true;
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "hxj:telegramlogin");
        }
        if (this.getConfig().getBoolean("login-session"))
            this.loginSessionEnabled = true;

        maxAccountsCount = this.getConfig().getInt("max-accounts-count");

        this.startBot();
        new Task(this).startClearCacheTask();

        int pluginId = 13305;
        new Metrics(this, pluginId);

        getLogger().info("---------------------------------------");
        getLogger().info("TelegramLogin by iVirus_");
        getLogger().info("Version: " + this.getDescription().getVersion());
        getLogger().info("Discord support: https://discord.io/hoxija");
        getLogger().info("Telegram channel: https://t.me/HoxijaChannel");
        getLogger().info("Plugin is ready!");
        getLogger().info("---------------------------------------");
        this.versionCheck();
    }

    private void setupDb() {
        String database = getConfig().getString("MySQL.Database");
        String username = getConfig().getString("MySQL.Username");
        String password = getConfig().getString("MySQL.Password");
        String host = getConfig().getString("MySQL.Host");
        int port = getConfig().getInt("MySQL.Port");
        boolean ssl = getConfig().getBoolean("MySQL.SSL");
        sql = getConfig().getBoolean("MySQL.Enable") ? new MySQL(this, database, username, password, host, port, ssl) : new SQLite(this);
        try {
            sql.setup();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("MySQL.Enable")) {
            MySQL mysql = (MySQL) sql;
            mysql.closePool();
        }
        botThread.stop();
        Bukkit.getScheduler().cancelTasks(this);
    }

    private void versionCheck(){
        new UpdateChecker(this, 97563).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("The plugin is up to date.");
            } else {
                getLogger().info("There is a new update available.");
                getLogger().info("Download it from: https://www.spigotmc.org/resources/telegramlogin.97563/");
            }
        });
    }

    private void startBot() {
        botThread = new Thread(() -> {
            try {
                botsApi = new TelegramBotsApi(DefaultBotSession.class);
                bot = new TelegramBot(this);
                botsApi.registerBot(bot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        });
        botThread.start();
    }

    private void createLangFile(String... names) {
        for (String name : names) {
            if (!new File(getDataFolder(), "languages" + File.separator + name + ".yml").exists()) {
                saveResource("languages" + File.separator + name + ".yml", false);
            }
        }
    }

    public void loadLangConfig() {
        langFile = new File(getDataFolder(), "languages" + File.separator + getConfig().getString("language") + ".yml");
        if (!langFile.exists()) {
            langFile = new File(getDataFolder(), "languages" + File.separator + "en_US.yml");
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }
}
