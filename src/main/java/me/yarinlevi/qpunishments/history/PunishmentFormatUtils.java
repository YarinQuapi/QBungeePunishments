package me.yarinlevi.qpunishments.history;

import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class PunishmentFormatUtils {
    public static TextComponent getLatestPunishmentsOfMember(String uuid, int count) throws PlayerNotFoundException, SQLException {
        ResultSet resultSet = QBungeePunishments.getInstance().getMysql().get(String.format("SELECT * FROM `punishments` WHERE `punished_uuid`=\"%s\" ORDER BY date_added DESC LIMIT " + count + ";", uuid));

        String name;
        if (!Utilities.validIP(uuid)) {
            name = MojangAccountUtils.getName(uuid);
        } else name = uuid;

        if (resultSet != null && resultSet.next()) {
            TextComponent stringBuilder = MessagesUtils.getMessage("last_punishments", count, name);

            stringBuilder.addExtra(format(resultSet));

            return stringBuilder;
        } else {
            return MessagesUtils.getMessage("no_punishments");
        }
    }

    public static TextComponent getLatestSpecificPunishmentsOfMember(String uuid, PunishmentType type, int count) throws PlayerNotFoundException, SQLException {
        ResultSet resultSet = QBungeePunishments.getInstance().getMysql().get(String.format("SELECT * FROM `punishments` WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"%s\" ORDER BY date_added DESC LIMIT " + count, uuid, type.getKey().toLowerCase()));

        String name = MojangAccountUtils.getName(uuid);

        if (resultSet != null && resultSet.next()) {
            TextComponent stringBuilder = MessagesUtils.getMessage("last_punishments_specific", count, type.getKey(), name);

            stringBuilder.addExtra(format(resultSet));

            return stringBuilder;
        } else {
            return MessagesUtils.getMessage("no_punishments");
        }
    }

    private static TextComponent format(ResultSet resultSet) throws SQLException {
        TextComponent stringBuilder = new TextComponent();

        do {
            long timestamp = resultSet.getLong("expire_date");

            String formattedDate;
            if (timestamp != 0) {
                if (resultSet.getBoolean("bypass_expire_date")) {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp)) + MessagesUtils.getRawString("removed");
                } else {
                    formattedDate = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(timestamp));

                }
            } else {
                if (resultSet.getBoolean("bypass_expire_date")) {
                    formattedDate = MessagesUtils.getRawString("permanent") + MessagesUtils.getRawString("removed");
                } else {
                    formattedDate = MessagesUtils.getRawString("permanent");
                }
            }

            TextComponent textComponent = MessagesUtils.getMessage("punishment_format",
                    resultSet.getString("id"),
                    resultSet.getString("punishment_type").toUpperCase(),
                    resultSet.getString("punished_by_name"),
                    resultSet.getString("reason"),
                    formattedDate);

            textComponent.addExtra("\n");

            stringBuilder.addExtra(textComponent);
        } while (resultSet.next());

        return stringBuilder;
    }
}
