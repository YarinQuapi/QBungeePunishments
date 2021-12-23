package me.yarinlevi.qpunishments.support.bungee.listeners;

import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.net.InetSocketAddress;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * @author YarinQuapi
 */
public class PlayerConnectListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(PostLoginEvent event) {

        QBungeePunishments.getInstance().getProxy().getScheduler().runAsync(QBungeePunishments.getInstance(), new Runnable() {
            @Override
            public void run() {
                try {
                    String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"ban\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"ban\" ORDER BY id DESC;",
                            event.getPlayer().getUniqueId().toString(), System.currentTimeMillis(), event.getPlayer().getUniqueId().toString());

                    ResultSet rs = QBungeePunishments.getInstance().getDatabase().get(sql);


                    if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date") && rs.getString("server").equalsIgnoreCase("global")) {
                        long timestamp = rs.getLong("expire_date");

                        String formattedDate;
                        if (timestamp != 0) {
                            formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                        } else formattedDate = "Never";

                        //String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format((rs.getTimestamp("timestamp")));
                        String reason = rs.getString("reason");

                        event.getPlayer().disconnect(MessagesUtils.getMessage("you_are_banned_disconnect", reason, formattedDate));
                        return;
                    }

                    String ipSql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"ban\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"ban\" ORDER BY id DESC;",
                            ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress(), System.currentTimeMillis(), ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress());

                    ResultSet rsIp = QBungeePunishments.getInstance().getDatabase().get(ipSql);

                    if (rsIp != null && rsIp.next() && !rsIp.getBoolean("bypass_expire_date") && rsIp.getString("server").equalsIgnoreCase("global")) {
                        long timestamp = rsIp.getLong("expire_date");

                        String formattedDate;
                        if (timestamp != 0) {
                            formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));
                        } else formattedDate = "Never";

                        //String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format((rs.getTimestamp("timestamp")));
                        String reason = rsIp.getString("reason");

                        String sql2 = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                        "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                                event.getPlayer().getUniqueId(),
                                rsIp.getString("punished_by_uuid"),
                                rsIp.getString("punished_by_name"),
                                rsIp.getString("expire_date"),
                                rsIp.getString("reason"),
                                rsIp.getString("punishment_type"),
                                System.currentTimeMillis(),
                                rsIp.getString("server")
                        );

                        QBungeePunishments.getInstance().getDatabase().insert(sql2);

                        event.getPlayer().disconnect(MessagesUtils.getMessage("you_are_banned_disconnect", reason, formattedDate));
                        return;
                    }

                    // PlayerData Construction

                    String sql1 = String.format("SELECT * FROM playerData WHERE `uuid`=\"%s\";", event.getPlayer().getUniqueId());

                    ResultSet rs1 = QBungeePunishments.getInstance().getDatabase().get(sql1);

                    if (rs1 != null && rs1.next()) {
                        String sql2 = String.format("UPDATE `playerData` set `lastLogin`=\"%s\", `name`=\"%s\", `ip`=\"%s\" WHERE `uuid`=\"%s\";",
                                System.currentTimeMillis(),
                                event.getPlayer().getName(),
                                ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress(),
                                event.getPlayer().getUniqueId());

                        QBungeePunishments.getInstance().getProxy().getScheduler().runAsync(QBungeePunishments.getInstance(), () -> QBungeePunishments.getInstance().getDatabase().update(sql2));
                    } else {
                        String sql2 = String.format("INSERT INTO `playerData`(`uuid`, `name`, `ip`, `firstLogin`, `lastLogin`) VALUES(\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                                event.getPlayer().getUniqueId(),
                                event.getPlayer().getName(),
                                ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress(),
                                System.currentTimeMillis(),
                                System.currentTimeMillis());

                        QBungeePunishments.getInstance().getProxy().getScheduler().runAsync(QBungeePunishments.getInstance(), () -> QBungeePunishments.getInstance().getDatabase().update(sql2));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    QBungeePunishments.getInstance().getLogger().severe("Hey! an SQL error was detected! please check your configuration files");
                }
            }
        });
    }
}
