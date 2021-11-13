package me.yarinlevi.qpunishments.support.bungee.messages;

import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class MessagesUtils {
    private static final Map<String, String> messages = new HashMap<>();
    static Pattern urlPattern = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)");

    public MessagesUtils() {
        Configuration messagesData;

        try {
            messagesData = YamlConfiguration.getProvider(YamlConfiguration.class).load(new FileReader(new File(QBungeePunishments.getInstance().getDataFolder(), "messages.yml")));

            messagesData.getKeys().forEach(key -> messages.put(key, messagesData.getString(key)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void reload() {
        messages.clear();

        Configuration messagesData;

        try {
            messagesData = YamlConfiguration.getProvider(YamlConfiguration.class).load(new FileReader(new File(QBungeePunishments.getInstance().getDataFolder(), "messages.yml")));

            messagesData.getKeys().forEach(key -> messages.put(key, messagesData.getString(key)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static TextComponent getMessage(String key, Object... args) {
        return new TextComponent(String.format(messages.get(key).replaceAll("&", "§"), args));
    }

    public static TextComponent getMessageWithClickable(String key, Object... args) {
        String msg = String.format(messages.get(key).replaceAll("&", "§"), args);

        TextComponent textComponent = new TextComponent();

        for (String str : msg.split(" ")) {
            if (urlPattern.matcher(str).matches()) {
                TextComponent uri = new TextComponent(str);

                uri.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, str));
                textComponent.addExtra(uri);
                textComponent.addExtra(" ");
            } else {
                TextComponent noteArg = new TextComponent(str);

                textComponent.addExtra(noteArg);
                textComponent.addExtra(" ");
            }
        }

        return textComponent;
    }

    public static String getRawString(String key) {
        return messages.getOrDefault(key, key).replaceAll("&", "§");
    }

    public static String getRawFormattedString(String key, Object... args) {
        return messages.getOrDefault(key, key).replaceAll("&", "§").formatted(args);
    }
}
