package me.yarinlevi.qpunishments.support.bungee.listeners;

import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

public class PlayerSwitchServerListener implements Listener {
    @EventHandler
    public void onSwitchServer(ServerSwitchEvent event) {
        QBungeePunishments.getInstance().getProxy().getScheduler().runAsync(QBungeePunishments.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    if (event.getPlayer().isConnected()) {
                        String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"ban\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"ban\"",
                                event.getPlayer().getUniqueId().toString(), System.currentTimeMillis(), event.getPlayer().getUniqueId().toString());

                        ResultSet rs = QBungeePunishments.getInstance().getDatabase().get(sql);

                        try {
                            if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date") && rs.getString("server").equals(event.getPlayer().getServer().getInfo().getName())) {
                                long timestamp = rs.getLong("expire_date");

                                String formattedDate;
                                if (timestamp != 0) {
                                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                                } else formattedDate = "Never";

                                //String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format((rs.getTimestamp("timestamp")));
                                String reason = rs.getString("reason");

                                event.getPlayer().sendMessage(MessagesUtils.getMessage("you_are_banned_chat", reason, formattedDate));
                            }
                        } catch (NoSuchElementException ignored) {
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    QBungeePunishments.getInstance().getLogger().severe("Hey! an SQL error was detected! please check your configuration files");
                }
            }
        });
    }
}
