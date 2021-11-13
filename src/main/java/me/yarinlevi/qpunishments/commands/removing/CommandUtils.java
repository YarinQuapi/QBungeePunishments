package me.yarinlevi.qpunishments.commands.removing;

import com.velocitypowered.api.command.CommandSource;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.MySQLHandler;
import me.yarinlevi.qpunishments.utilities.Utilities;

import java.io.IOException;

public class CommandUtils {
    public static void remove(CommandSource sender, String[] args, PunishmentType type, boolean ip) throws PlayerNotFoundException {
        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            String playerNameOrIp;
            boolean silent = false;

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("-s")) {
                    sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                    return;
                } else {
                    if (ip) {
                        if (Utilities.validIP(args[0])) {
                            playerNameOrIp = args[0];
                        } else {
                            playerNameOrIp = Utilities.getIpAddress(args[0]);
                        }
                    } else playerNameOrIp = args[0];
                }
            } else {
                if (args[0].equalsIgnoreCase("-s")) {
                    silent = true;

                    if (ip) {
                        if (Utilities.validIP(args[1])) {
                            playerNameOrIp = args[1];
                        } else {
                            playerNameOrIp = Utilities.getIpAddress(args[1]);
                        }
                    } else playerNameOrIp = args[1];
                } else {
                    if (ip) {
                        if (Utilities.validIP(args[0])) {
                            playerNameOrIp = args[0];
                        } else {
                            playerNameOrIp = Utilities.getIpAddress(args[0]);
                        }
                    } else playerNameOrIp = args[0];
                }
            }

            String punishment = type.getKey();

            if (!ip) {
                try {
                    String uuid = MojangAccountUtils.getUUID(playerNameOrIp);

                    String sql = String.format("UPDATE `punishments` SET `bypass_expire_date`=true WHERE `punished_uuid` = \"%s\" AND `expire_date` > \"%s\" AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" OR `punished_uuid` = \"%s\" AND `expire_date`=0 AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" ORDER BY id DESC;",
                            uuid, System.currentTimeMillis(), punishment, uuid, punishment);

                    if (MySQLHandler.getInstance().update(sql) >= 1) {
                        sender.sendMessage(MessagesUtils.getMessage("pardon_successful", playerNameOrIp, punishment));

                        if (!silent && QBungeePunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishment)) {
                            Utilities.broadcast(MessagesUtils.getMessage("un" + type.getKey().toLowerCase(), playerNameOrIp));
                        }

                    } else {
                        sender.sendMessage(MessagesUtils.getMessage("no_punishment_found"));
                    }

                } catch (UUIDNotFoundException e) {
                    sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String sql = String.format("UPDATE `punishments` SET `bypass_expire_date`=true WHERE `punished_uuid` = \"%s\" AND `expire_date` > \"%s\" AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" OR `punished_uuid` = \"%s\" AND `expire_date`=0 AND `bypass_expire_date`=false AND `punishment_type`=\"%s\" ORDER BY id DESC;",
                        playerNameOrIp, System.currentTimeMillis(), punishment, playerNameOrIp, punishment);

                if (MySQLHandler.getInstance().update(sql) >= 1) {
                    sender.sendMessage(MessagesUtils.getMessage("pardon_successful", playerNameOrIp, punishment));

                    if (!silent && QBungeePunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishment)) {
                        Utilities.broadcast(MessagesUtils.getMessage("unip" + type.getKey().toLowerCase()));
                    }

                } else {
                    sender.sendMessage(MessagesUtils.getMessage("no_punishment_found"));
                }
            }
        }
    }
}
