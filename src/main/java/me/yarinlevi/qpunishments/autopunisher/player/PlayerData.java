package me.yarinlevi.qpunishments.autopunisher.player;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YarinQuapi
 **/
public class PlayerData {
    @Getter private final Map<String, Integer> violations = new HashMap<>();
    @Getter @Setter private int timeSinceLastMessage;
}
