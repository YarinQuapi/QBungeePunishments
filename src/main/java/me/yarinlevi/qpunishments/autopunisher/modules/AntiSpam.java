package me.yarinlevi.qpunishments.autopunisher.modules;

import me.yarinlevi.qpunishments.autopunisher.AutoPunisherManager;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author YarinQuapi
 **/
public class AntiSpam extends PunisherModule implements Listener {


    public AntiSpam() {
        super("antispam", "Automatically detects and blocks spammers");
    }

    @Override
    public void register(AutoPunisherManager manager) {
        manager.getPlugin().getProxy().getPluginManager().registerListener(manager.getPlugin(), this);

        QBungeePunishments.getInstance().getProxy().getScheduler().schedule(manager.getPlugin(), new Runnable() {
            @Override
            public void run() {

            }
        }, 1L, TimeUnit.MINUTES);
    }

    @EventHandler
    private void onChat(ChatEvent event) {
        if (event.isCommand()) {

        } else {

        }
    }

    private void clear() {
    }
}
