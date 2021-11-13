package me.yarinlevi.qpunishments.commands.executing;

import me.yarinlevi.qpunishments.exceptions.*;
import me.yarinlevi.qpunishments.punishments.Punishment;
import me.yarinlevi.qpunishments.punishments.PunishmentBuilder;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.punishments.PunishmentUtils;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

/**
 * @author YarinQuapi
 */
public class BanCommand extends Command {
    public BanCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
        } else {
            PunishmentBuilder punishmentBuilder;

            try {
                punishmentBuilder = PunishmentUtils.createPunishmentBuilder(sender, args, PunishmentType.BAN, false);
            } catch (PlayerNotFoundException e) {

                sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
                return;
            } catch (NotEnoughArgumentsException e) {
                sender.sendMessage(MessagesUtils.getMessage("not_enough_args"));
                return;
            } catch (ServerNotExistException e) {
                sender.sendMessage(MessagesUtils.getMessage("server_not_found"));
                return;
            } catch (NotValidIpException ignored) {
                return;
            }

            try {
                Punishment pun = punishmentBuilder.build();

                pun.execute(false);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (PlayerPunishedException e) {
                sender.sendMessage(MessagesUtils.getMessage("player_punished"));
            }
        }
    }
}
