package me.yarinlevi.qpunishments.support.bungee;

import lombok.Getter;
import lombok.SneakyThrows;
import me.yarinlevi.qpunishments.commands.CommentCommand;
import me.yarinlevi.qpunishments.commands.LookupCommand;
import me.yarinlevi.qpunishments.commands.LookupIpCommand;
import me.yarinlevi.qpunishments.commands.executing.*;
import me.yarinlevi.qpunishments.commands.removing.UnBanCommand;
import me.yarinlevi.qpunishments.commands.removing.UnIpBanCommand;
import me.yarinlevi.qpunishments.commands.removing.UnIpMuteCommand;
import me.yarinlevi.qpunishments.commands.removing.UnMuteCommand;
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerChatListener;
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerConnectListener;
import me.yarinlevi.qpunishments.support.bungee.listeners.PlayerSwitchServerListener;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import me.yarinlevi.qpunishments.utilities.RedisHandler;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * @author YarinQuapi
 */
public final class QBungeePunishments extends Plugin {
    @Getter private final String version = "0.1A-PrivateBungee";
    @Getter private static QBungeePunishments instance;
    @Getter private MySQLHandler mysql;
    @Getter private RedisHandler redis;
    @Getter private Configuration config;

    @SneakyThrows
    @Override
    public void onEnable() {
        instance = this;

        if (getJavaVersion() < 16) {
            this.getLogger().severe("QBungeePunishments requires Java 16+ to operate. Please upgrade your software and try again.");
            return;
        }

        if (!getDataFolder().exists())
            //noinspection ResultOfMethodCallIgnored
            getDataFolder().mkdirs();

        File file1 = new File(getDataFolder(), "messages.yml");
        File file2 = new File(getDataFolder(), "config.yml");

        registerFile(file1, "messages.yml");
        registerFile(file2, "config.yml");


        this.config = YamlConfiguration.getProvider(YamlConfiguration.class).load(file2);
        this.mysql = new MySQLHandler(this.config);

        new MessagesUtils();

        PluginManager pluginManager = getProxy().getPluginManager();

        // Punishment commands
        pluginManager.registerCommand(this, new BanCommand("qban", "qpunishments.command.qban","ban", "tempban", "qtempban"));
        pluginManager.registerCommand(this, new MuteCommand("qmute", "qpunishments.command.qmute","temp", "tempmute", "qtempmute"));
        pluginManager.registerCommand(this, new KickCommand("qkick", "qpunishments.command.qkick", "kick"));
        pluginManager.registerCommand(this, new UnBanCommand("qunban", "qpunishments.command.qunban", "unban"));
        pluginManager.registerCommand(this, new UnMuteCommand("qunmute", "qpunishments.command.qunmute", "unmute"));

        // Ip Punishment commands
        pluginManager.registerCommand(this, new IpBanCommand("ipban", "qpunishments.command.qipban", "qipban", "qiptempban"));
        pluginManager.registerCommand(this, new IpMuteCommand("ipmute", "qpunishments.command.qipmute", "qipmute", "qiptempmute"));
        pluginManager.registerCommand(this, new UnIpBanCommand("unipban", "qpunishments.command.qunipban", "qunipban"));
        pluginManager.registerCommand(this, new UnIpMuteCommand("unipmute", "qpunishments.command.qunipmute", "qunipmute"));

        // History and proof commands
        pluginManager.registerCommand(this, new CommentCommand("comment", "qpunishments.command.comment", "addcomment", "qcomment"));
        pluginManager.registerCommand(this, new LookupCommand("lookup", "qpunishemnts.command.lookup"));
        pluginManager.registerCommand(this, new LookupIpCommand("lookupip", "qpunishments.command.lookupip"));
        pluginManager.registerCommand(this, new HistoryCommand("history", "qpunishments.command.historyadmin", "ha", "historyadmin"));


        // Listeners for ban and chat control
        PlayerChatListener chatListener = new PlayerChatListener();

        pluginManager.registerListener(this, new PlayerConnectListener());
        pluginManager.registerListener(this, chatListener);
        pluginManager.registerListener(this, new PlayerSwitchServerListener());

        if (this.getProxy().getPluginManager().getPlugins().stream().anyMatch(x-> x.getDescription().getName().equalsIgnoreCase("qproxyutilities-bungeecord"))) {
            chatListener.setQProxyUtilitiesFound(true);
            QBungeePunishments.getInstance().getLogger().log(Level.WARNING, "QProxyUtilities found! staff chat disabled.");
        }

        if (this.isRedis()) {
            redis = new RedisHandler(this.config);
        }

        // BStats initialization
        // Todo: replace serviceId
        new Metrics(this, 12866);
    }

    private void registerFile(File file, String streamFileName) {
        if (!file.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(streamFileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    public boolean isRedis() {
        return this.config.getBoolean("redis.enabled");
    }
}
