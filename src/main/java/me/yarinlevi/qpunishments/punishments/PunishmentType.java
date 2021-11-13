package me.yarinlevi.qpunishments.punishments;

import lombok.Getter;

/**
 * @author YarinQuapi
 */
public enum PunishmentType {
    KICK("kick"),
    MUTE("mute"),
    BAN("ban");

    @Getter String key;

    PunishmentType(String key) {
        this.key = key;
    }
}