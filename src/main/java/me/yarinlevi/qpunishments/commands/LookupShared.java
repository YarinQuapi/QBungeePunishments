package me.yarinlevi.qpunishments.commands;

import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.history.CommentUtils;
import me.yarinlevi.qpunishments.history.PunishmentFormatUtils;
import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.regex.Pattern;

public class LookupShared {
    private static final Pattern namePattern = Pattern.compile("[A-z0-9]\\w+");

    protected static void printLookup(CommandSender sender, String targetPlayer, QueryMode mode, int limit, boolean debug, boolean ip) {
        try {
            String uuidOrIp;
            if (!ip) {
                uuidOrIp = MojangAccountUtils.getUUID(targetPlayer);
            } else {
                if (Utilities.validIP(targetPlayer)) {
                    uuidOrIp = targetPlayer;
                } else {
                    if (targetPlayer.length() <= 16 && namePattern.matcher(targetPlayer).matches()) {
                        uuidOrIp = Utilities.getIpAddress(targetPlayer);
                    } else throw new PlayerNotFoundException();
                }
            }

            TextComponent textComponent = new TextComponent();
            switch (mode) {
                case ALL -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_all", QBungeePunishments.getInstance().getVersion()));

                    if (!ip) {
                        ResultSet rs = MySQLHandler.getInstance().get("SELECT * FROM `playerData` WHERE `uuid`=\"" + uuidOrIp + "\";");
                        String firstLogin = MessagesUtils.getRawString("never_logged_on");
                        String lastLogin = MessagesUtils.getRawString("never_logged_on");

                        if (rs != null && rs.next()) {
                            firstLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("firstLogin"));
                            lastLogin = new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(rs.getLong("lastLogin"));
                        }

                        textComponent.addExtra(MessagesUtils.getMessage("first_login", firstLogin));
                        textComponent.addExtra(MessagesUtils.getMessage("last_login", lastLogin));

                    } else {

                        ResultSet playersLinked = MySQLHandler.getInstance().get("SELECT * FROM `playerData` WHERE `ip`=\"" + uuidOrIp + "\";");

                        TextComponent ipAddress = new TextComponent();
                        TextComponent playersLinkedComponent = MessagesUtils.getMessage("lookup_ip_players_linked");
                        TextComponent linkedPlayer;

                        if (playersLinked != null && playersLinked.next()) {
                            MessagesUtils.getMessage("lookup_ip_address", playersLinked.getString("ip"));
                            ipAddress.addExtra(playersLinkedComponent);

                            do {
                                linkedPlayer = MessagesUtils.getMessage("lookup_ip_players_linked_format", playersLinked.getString("name"));

                                String uuid = playersLinked.getString("uuid");
                                linkedPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid));
                                linkedPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("&7UUID: &b" + uuid).create()));

                                ipAddress.addExtra(linkedPlayer);
                            } while (playersLinked.next());
                        }

                        textComponent.addExtra(ipAddress + "\n");
                    }


                    ResultSet historyResults = MySQLHandler.getInstance().get("SELECT * FROM `punishments` WHERE `punished_uuid`=\"" + uuidOrIp + "\";");

                    int ban = 0;
                    int mute = 0;
                    int kick = 0;

                    if (historyResults != null) {
                        while (historyResults.next()) {
                            switch (historyResults.getString("punishment_type")) {
                                case "mute" -> mute++;
                                case "kick" -> kick++;
                                case "ban" -> ban++;
                            }
                        }
                    }

                    textComponent.addExtra(MessagesUtils.getMessage("history_count", ban, mute, kick));

                    textComponent.addExtra(new TextComponent("\n\n"));

                    if (!ip) {
                        textComponent.addExtra(CommentUtils.getCommentsOfMember(uuidOrIp, 3, false));

                        textComponent.addExtra(new TextComponent("\n"));
                    }

                    textComponent.addExtra(PunishmentFormatUtils.getLatestPunishmentsOfMember(uuidOrIp, 3));

                    textComponent.addExtra(new TextComponent("\n"));

                    if (!ip) {
                        Iterator<String> namesIterator = MojangAccountUtils.getNameHistory(uuidOrIp).listIterator();

                        TextComponent nameHistory = MessagesUtils.getMessage("name_history");

                        while (namesIterator.hasNext()) {
                            nameHistory.addExtra(MessagesUtils.getMessage("name_history_format", namesIterator.next()));

                            if (namesIterator.hasNext()) {
                                nameHistory.addExtra(new TextComponent(", "));
                            }
                        }

                        textComponent.addExtra(nameHistory);
                    }
                }

                case COMMENT -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getVersion(), "Comment"));
                    textComponent.addExtra(CommentUtils.getCommentsOfMember(uuidOrIp, limit, debug));
                }

                case MUTE -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getVersion(), "Mute"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.MUTE, limit));
                }

                case BAN -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getVersion(), "Ban"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.BAN, limit));
                }

                case KICK -> {
                    textComponent.addExtra(MessagesUtils.getMessage("lookup_mode_specific", QBungeePunishments.getInstance().getVersion(), "Kick"));
                    textComponent.addExtra(PunishmentFormatUtils.getLatestSpecificPunishmentsOfMember(uuidOrIp, PunishmentType.KICK, limit));
                }
            }

            sender.sendMessage(textComponent);

        } catch (UUIDNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        } catch (SQLException | IOException | PlayerNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
}
