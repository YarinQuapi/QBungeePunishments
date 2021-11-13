package me.yarinlevi.qpunishments.support.bungee.listeners;

import lombok.Setter;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.RedisHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {
    private final boolean staffChatEnabled = QBungeePunishments.getInstance().getConfig().getBoolean("staff-chat.enabled");
    @Setter private boolean isQProxyUtilitiesFound = false;

    @EventHandler
    public void onPlayerChat(ChatEvent event) throws SQLException {
        if (event.getSender() instanceof ProxiedPlayer sender) {
            String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"mute\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"mute\" ORDER BY id DESC;",
                    sender.getUniqueId().toString(), System.currentTimeMillis(), sender.getUniqueId().toString());

            ResultSet rs = QBungeePunishments.getInstance().getMysql().get(sql);

            if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date")) {
                String server = rs.getString("server");

                if (server.equalsIgnoreCase("global") || sender.getServer().getInfo().getName().equals(server)) {
                    event.setCancelled(true);

                    long timestamp = rs.getLong("expire_date");

                    String formattedDate;
                    if (timestamp != 0) {
                        formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                    } else {
                        formattedDate = "forever";
                    }

                    sender.sendMessage(MessagesUtils.getMessage("you_are_muted", formattedDate));
                }
            }

            if (sender.hasPermission("qpunishments.commands.staffchat") && staffChatEnabled) {
                if (event.getMessage().startsWith(QBungeePunishments.getInstance().getConfig().getString("staff-chat.chat-char"))) {
                    if (!isQProxyUtilitiesFound) {
                        event.setCancelled(true);

                        StringBuffer sb = new StringBuffer(event.getMessage());

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
        }
    }
}