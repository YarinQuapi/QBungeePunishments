package me.yarinlevi.qpunishments.commands.administration;

import com.velocitypowered.api.command.RawCommand;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReloadMessagesCommand implements RawCommand {
    @Override
    public void execute(Invocation invocation) {
        MessagesUtils.reload();
        invocation.source().sendMessage(Component.text("§eMessages reloaded!"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return RawCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return RawCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("qpunishments.admin");
    }
}
