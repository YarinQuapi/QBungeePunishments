package me.yarinlevi.qpunishments.commands.administration;

import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * @author YarinQuapi
 **/
public class ReloadConfig extends Command {
    public ReloadConfig(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        File file = new File(QBungeePunishments.getInstance().getDataFolder(), "config.yml");

        Utilities.registerFile(file, "config.yml");

        try {
            QBungeePunishments.getInstance().setConfig(YamlConfiguration.getProvider(YamlConfiguration.class).load(file));

            commandSender.sendMessage(new TextComponent("Reloaded all config values!"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
