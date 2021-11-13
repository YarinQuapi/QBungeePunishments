package me.yarinlevi.qpunishments.utilities;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.UUIDNotFoundException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YarinQuapi
 * @since 0.0.1
 */
public class MojangAccountUtils {
    /**
     * NOTE: THIS IS RATE-LIMITED (600/10m)
     * @param playerName The player whose uuid is wanted
     * @return player's uuid
     * @throws UUIDNotFoundException The uuid was not found in Mojang's servers
     * @throws IOException Something went wrong with Mojang's servers and a connection was not possible.
     */
    public static String getUUID(String playerName) throws UUIDNotFoundException, IOException {
        return QBungeePunishments.getInstance().getServer().getPlayer(playerName) != null ?
                QBungeePunishments.getInstance().getServer().getPlayer(playerName).getUniqueId().toString() :
                insertDashToUUID(getUUIDOfUsername(playerName));
    }

    /**
     * NOTE: THIS IS NOT RATE-LIMITED
     * @param playerUUID The uuid you want to lookup
     * @return A player name
     * @throws PlayerNotFoundException The uuid doesn't exist in mojang's databases
     */
    public static String getName(String playerUUID) throws PlayerNotFoundException {

        String uuid;
        if (playerUUID.contains("-")) {
            uuid = getDashlessUUID(playerUUID);
        } else uuid = playerUUID;

        String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url));
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);

            String playerSlot = nameValue.get(nameValue.size()-1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);

            return nameObject.get("name").toString();
        } catch (IOException | ParseException e) {
            throw new PlayerNotFoundException();
        }
    }

    public static List<String> getNameHistory(String playerUUID) throws PlayerNotFoundException {

        String uuid;
        if (playerUUID.contains("-")) {
            uuid = getDashlessUUID(playerUUID);
        } else uuid = playerUUID;

        String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try {
            @SuppressWarnings("deprecation")
            String nameJson = IOUtils.toString(new URL(url));
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);
            List<String> nameList = new ArrayList<>();

            String playerSlot;
            JSONObject nameObject;

            for (int i = 0; i < (nameValue.size() - 1); i++) {
                playerSlot = nameValue.get(i).toString();
                nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);

                nameList.add(nameObject.get("name").toString());
            }

            return nameList;
        } catch (IOException | ParseException e) {
            throw new PlayerNotFoundException();
        }
    }

    public static String getUUIDOfUsername(String username) throws IOException, UUIDNotFoundException {
        Gson gson = new Gson();

        InputStream inputStream = new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openStream();

        if (inputStream == null) throw new UUIDNotFoundException();

        Reader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        PlayerProfile playerProfile = gson.fromJson(readAll(in), PlayerProfile.class);

        if (playerProfile != null) {
            return playerProfile.getId();
        } else {
            throw new UUIDNotFoundException();
        }
    }

    public static String insertDashToUUID(String uuid) {
        StringBuffer sb = new StringBuffer(uuid);
        sb.insert(8, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(13, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(18, "-");

        sb = new StringBuffer(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

    public static String getDashlessUUID(String uuid) {
        String[] split = uuid.split("-");

        StringBuilder constructed = new StringBuilder();

        for (String s : split) {
            constructed.append(s);
        }

        return constructed.toString();
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static class PlayerProfile {
        @Getter @Setter String name;
        @Getter @Setter String id;
    }
}
