package me.yarinlevi.qpunishments.utilities;

import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class Utilities {
    private static final Pattern ipPattern = Pattern
            .compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    public static void broadcast(TextComponent message) {
        for (ProxiedPlayer player : QBungeePunishments.getInstance().getProxy().getPlayers()) {
            player.sendMessage(message);
        }
    }

    public static boolean validIP(final String ip) {
        return ipPattern.matcher(ip).matches();
    }

    public static void registerFile(File file, String streamFileName) {
        if (!file.exists()) {
            try (InputStream in = QBungeePunishments.getInstance().getClass().getClassLoader().getResourceAsStream(streamFileName)) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getIpAddress(String playerName) throws PlayerNotFoundException {
        if (QBungeePunishments.getInstance().getProxy().getPlayer(playerName) != null)
            return QBungeePunishments.getInstance().getProxy().getPlayer(playerName).getAddress().getAddress().getHostAddress();

        ResultSet rs = QBungeePunishments.getInstance().getDatabase().get(String.format("SELECT * FROM `playerData` WHERE `name`=\"%s\" ORDER BY lastLogin DESC;",
                playerName));

        try {
            if (rs != null && rs.next()) {
                return rs.getString("ip");
            } else {
                throw new PlayerNotFoundException();
            }
        } catch (SQLException e) {
            throw new PlayerNotFoundException();
        }
    }
}
