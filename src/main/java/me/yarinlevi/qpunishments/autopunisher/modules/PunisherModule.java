package me.yarinlevi.qpunishments.autopunisher.modules;

import lombok.Getter;
import me.yarinlevi.qpunishments.autopunisher.AutoPunisherManager;

/**
 * @author YarinQuapi
 **/
public abstract class PunisherModule {
    @Getter private final String name;
    @Getter private final String description;

    public PunisherModule(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public abstract void register(AutoPunisherManager manager);
}
