package me.yarinlevi.qpunishments.history;

public enum QueryMode {
    ALL("all"),
    BAN("ban"),
    MUTE("mute"),
    KICK("kick"),
    COMMENT("comment");

    String key;

    QueryMode(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
