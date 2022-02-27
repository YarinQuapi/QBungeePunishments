package me.yarinlevi.qpunishments.autopunisher;

import lombok.Getter;
import me.yarinlevi.qpunishments.autopunisher.modules.PunisherModule;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YarinQuapi
 **/
public class AutoPunisherManager {
    private final List<PunisherModule> modules = new ArrayList<>();

    @Getter private final QBungeePunishments plugin;

    public AutoPunisherManager(QBungeePunishments plugin) {
        this.plugin = plugin;

        this.registerModules();

        this.initializeModules();
    }

    private void registerModules() {
    }

    private void initializeModules() {
        modules.forEach(
                module -> module.register(this)
        );
    }

}
