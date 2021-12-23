package me.yarinlevi.qpunishments.commands.administration;

import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author YarinQuapi
 **/
public class ReloadMessages extends Command {
    public ReloadMessages(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        MessagesUtils.reload();

        commandSender.sendMessage(new TextComponent("Reloaded all messages data!"));
    }
}
