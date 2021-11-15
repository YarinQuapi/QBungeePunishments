package me.yarinlevi.qpunishments.commands.removing;


import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class UnIpBanCommand extends Command {
    public UnIpBanCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            CommandUtils.remove(sender, args, PunishmentType.BAN, true);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        }
    }
}
