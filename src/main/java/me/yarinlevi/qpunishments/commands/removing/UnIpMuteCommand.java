package me.yarinlevi.qpunishments.commands.removing;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.punishments.PunishmentType;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;

public class UnIpMuteCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        try {
            CommandUtils.remove(sender, args, PunishmentType.MUTE, true);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(MessagesUtils.getMessage("player_not_found"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.command.unipmute");
    }
}
