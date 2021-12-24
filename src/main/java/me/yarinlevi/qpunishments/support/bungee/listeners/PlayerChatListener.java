package me.yarinlevi.qpunishments.support.bungee.listeners;

import lombok.Setter;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.RedisHandler;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {
    private final boolean staffChatEnabled = QBungeePunishments.getInstance().getConfig().getBoolean("staff-chat.enabled");
    @Setter private boolean isQProxyUtilitiesFound = false;

    private final Map<UUID, PlayerCache> cache = new HashMap<>();

    private final List<String> blockedCommands = new ArrayList<>();
    private final String bypassPermission;

    public PlayerChatListener() {
        QBungeePunishments.getInstance().getProxy().getScheduler().schedule(QBungeePunishments.getInstance(), cache::clear, 10L, TimeUnit.MINUTES);

        blockedCommands.addAll(QBungeePunishments.getInstance().getConfig().getStringList("general-blocked-commands"));
        bypassPermission = QBungeePunishments.getInstance().getConfig().getString("blocked-commands-bypass-permission");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(ChatEvent event) {
        if (event.getSender() instanceof ProxiedPlayer sender) {
            if (cache.containsKey(sender.getUniqueId())) {
                event.setCancelled(this.process(sender, event.getMessage()));

            } else {
                String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"mute\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"mute\" ORDER BY id DESC;",
                        sender.getUniqueId().toString(), System.currentTimeMillis(), sender.getUniqueId().toString());

                ResultSet rs = QBungeePunishments.getInstance().getDatabase().get(sql);

                try {
                    if (rs != null && rs.next()) {
                        String server = rs.getString("server");
                        long timestamp = rs.getLong("expire_date");
                        boolean bypass = rs.getBoolean("bypass_expire_date");

                        PlayerCache playerCache = new PlayerCache(sender.getUniqueId(), true, timestamp, bypass, server);

                        cache.put(playerCache.uuid, playerCache);

                        event.setCancelled(this.process(sender, event.getMessage()));

                    } else {
                        PlayerCache playerCache = new PlayerCache(sender.getUniqueId(), false, 0, false, "dead");

                        cache.put(playerCache.uuid, playerCache);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    QBungeePunishments.getInstance().getLogger().severe("Hey! an SQL error was detected! please check your configuration files");
                }
            }


            if (sender.hasPermission("qpunishments.commands.staffchat") && staffChatEnabled) {
                if (event.getMessage().startsWith(QBungeePunishments.getInstance().getConfig().getString("staff-chat.chat-char"))) {
                    if (!isQProxyUtilitiesFound) {
                        event.setCancelled(true);

                        StringBuilder sb = new StringBuilder(event.getMessage());

                        sb.deleteCharAt(0);

                        if (!RedisHandler.isRedis()) {
                            for (ProxiedPlayer proxiedPlayer : QBungeePunishments.getInstance().getProxy().getPlayers().stream().filter(x -> x.hasPermission("qpunishments.commands.staffchat")).collect(Collectors.toList())) {
                                proxiedPlayer.sendMessage(MessagesUtils.getMessage("staff_chat_message", sender.getName(), sb.toString()));
                            }
                        } else {
                            QBungeePunishments.getInstance().getRedis().postStaffChatMessage(MessagesUtils.getRawFormattedString("staff_chat_message", sender.getName(), sb.toString()));
                        }
                    }
                }
            }

            // General command blocker (Network-wide)
            if (event.isCommand()) {
                String command = new StringBuilder(event.getMessage()).deleteCharAt(0).toString();


                for (String cmd : blockedCommands) {
                    if (command.startsWith(cmd)) {
                        if (!sender.hasPermission(bypassPermission)) {
                            event.setCancelled(true);
                            sender.sendMessage(MessagesUtils.getMessage("general_command_blocked"));
                        }
                        break;
                    }
                }
            }
        }
    }

    private boolean process(ProxiedPlayer player, String args) {
        PlayerCache playerCache = cache.get(player.getUniqueId());

        if (playerCache.isPunished(player.getServer().getInfo().getName())) {
            if (args.charAt(0) == '/') {
                String command = new StringBuilder(args).deleteCharAt(0).toString();

                for (String cmd : QBungeePunishments.getInstance().getConfig().getStringList("blocked-commands")) {
                    if (command.toLowerCase().startsWith(cmd.toLowerCase())) {
                        return true; // Cancels
                    }
                }

                return false; // Allows
            } else {
                String formattedDate;
                if (playerCache.until != 0) {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(playerCache.until));
                } else {
                    formattedDate = "forever";
                }

                player.sendMessage(MessagesUtils.getMessage("you_are_muted", formattedDate));
                return true; // Cancels
            }
        } else return false; // Allows
    }

    private record PlayerCache(UUID uuid, boolean valid, long until, boolean bypass, String server) {
        public boolean isPunished(String server) {
            if (!valid) return false;

            if (bypass) return false;

            if (System.currentTimeMillis() > until && !(until == 0)) return false;

            return this.server.equalsIgnoreCase("global") || this.server.equalsIgnoreCase(server);
        }
    }
}