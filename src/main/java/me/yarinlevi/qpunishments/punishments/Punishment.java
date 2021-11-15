package me.yarinlevi.qpunishments.punishments;

import lombok.Getter;
import lombok.Setter;
import me.yarinlevi.qpunishments.exceptions.PlayerNotFoundException;
import me.yarinlevi.qpunishments.exceptions.PlayerPunishedException;
import me.yarinlevi.qpunishments.exceptions.ServerNotExistException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import me.yarinlevi.qpunishments.utilities.MojangAccountUtils;
import me.yarinlevi.qpunishments.utilities.RedisHandler;
import me.yarinlevi.qpunishments.utilities.Utilities;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author YarinQuapi
 */
public class Punishment {
    @Getter @Setter private int id;
    @Getter private final boolean ipPunishment;
    @Getter private String rawIpAddress;
    @Getter @Nullable private UUID punished_player_uuid;
    @Getter private final PunishmentType punishmentType;
    @Getter @Nullable private final UUID punished_by_player_uuid;
    @Getter private final String punished_by_name;
    @Getter private final String server;
    @Getter private final long duration;
    @Getter @Nullable private final String reason;
    @Getter private final boolean perm;
    @Getter private final boolean silent;

    @Getter private UUID redisUUID;

    public Punishment(String punishedPlayerUUIDOrIp, PunishmentType type, @Nullable UUID punishedByPlayerUUID, String punishedByPlayerName, String server, @Nullable String reason, long duration, boolean permanent, boolean silent, boolean ipPunishment) {
        if (ipPunishment) {
            this.rawIpAddress = punishedPlayerUUIDOrIp;
        } else {
            this.punished_player_uuid = UUID.fromString(punishedPlayerUUIDOrIp);
        }

        this.punishmentType = type;
        this.punished_by_player_uuid = punishedByPlayerUUID;
        this.punished_by_name = punishedByPlayerName;
        this.server = server;
        this.reason = reason;
        this.duration = duration;
        this.perm = permanent;
        this.silent = silent;
        this.ipPunishment = ipPunishment;
    }

    public Punishment(String punishedPlayerUUIDOrIp, PunishmentType type, @Nullable UUID punishedByPlayerUUID, String punishedByPlayerName, String server, @Nullable String reason, long duration, boolean permanent, boolean silent, boolean ipPunishment, UUID redisUUID) {
        if (ipPunishment) {
            this.rawIpAddress = punishedPlayerUUIDOrIp;
        } else {
            this.punished_player_uuid = UUID.fromString(punishedPlayerUUIDOrIp);
        }

        this.punishmentType = type;
        this.punished_by_player_uuid = punishedByPlayerUUID;
        this.punished_by_name = punishedByPlayerName;
        this.server = server;
        this.reason = reason;
        this.duration = duration;
        this.perm = permanent;
        this.silent = silent;
        this.ipPunishment = ipPunishment;

        this.redisUUID = redisUUID;
    }

    public void execute(boolean fromRedis) {
        if (!ipPunishment && QBungeePunishments.getInstance().getProxy().getPlayer(punished_player_uuid) != null) {
            ProxiedPlayer player = QBungeePunishments.getInstance().getProxy().getPlayer(punished_player_uuid);

            switch (punishmentType) {
                case BAN -> player.disconnect(MessagesUtils.getMessage("you_have_been_banned"));
                case KICK -> player.disconnect(MessagesUtils.getMessage("you_have_been_kicked"));
                case MUTE -> player.sendMessage(MessagesUtils.getMessage("you_have_been_muted"));
            }
        }

        if (ipPunishment) {
            this.addToMySQL();

            List<String> sqlQueue = new ArrayList<>();

            List<ProxiedPlayer> executedPlayers = new ArrayList<>();

            for (ProxiedPlayer player : QBungeePunishments.getInstance().getProxy().getPlayers().stream().filter(x -> x.getAddress().getAddress().getHostAddress().equals(rawIpAddress)).collect(Collectors.toList())) {
                sqlQueue.add(this.addToExecuteQueue(player.getUniqueId()));

                executedPlayers.add(player);
            }

            if (!sqlQueue.isEmpty()) {
                QBungeePunishments.getInstance().getMysql().insertLarge(sqlQueue);
            }

            for (ProxiedPlayer player : executedPlayers) {
                if (player.isConnected()) {
                    switch (punishmentType) {
                        case BAN -> player.disconnect(MessagesUtils.getMessage("you_have_been_banned"));
                        case MUTE -> player.sendMessage(MessagesUtils.getMessage("you_have_been_muted"));
                    }
                }
            }

        } else {
            this.addToMySQL();
        }

        if (RedisHandler.isRedis() && !fromRedis) {
            QBungeePunishments.getInstance().getRedis().postPunishment(this);
        }

        if (!silent && QBungeePunishments.getInstance().getConfig().getBoolean("announcements.punishments." + punishmentType.getKey())) {
            try {
                this.broadcast();
            } catch (PlayerNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast() throws PlayerNotFoundException {
        if (!ipPunishment) {
            if (punished_player_uuid != null) {
                switch (punishmentType) {
                    case BAN -> {
                        if (duration == 0) {
                            Utilities.broadcast(MessagesUtils.getMessage("perm_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                        } else {
                            Utilities.broadcast(MessagesUtils.getMessage("temp_ban", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                        }
                    }
                    case MUTE -> {
                        if (duration == 0) {
                            Utilities.broadcast(MessagesUtils.getMessage("perm_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                        } else {
                            Utilities.broadcast(MessagesUtils.getMessage("temp_mute", MojangAccountUtils.getName(punished_player_uuid.toString()), reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                        }
                    }
                    case KICK -> Utilities.broadcast(MessagesUtils.getMessage("kick", MojangAccountUtils.getName(punished_player_uuid.toString()), reason));
                }
            }
        } else {
            switch (punishmentType) {
                case BAN -> {
                    if (duration == 0) {
                        Utilities.broadcast(MessagesUtils.getMessage("perm_ip_ban", reason));
                    } else {
                        Utilities.broadcast(MessagesUtils.getMessage("temp_ip_ban", reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                    }
                }
                case MUTE -> {
                    if (duration == 0) {
                        Utilities.broadcast(MessagesUtils.getMessage("perm_ip_mute", reason));
                    } else {
                        Utilities.broadcast(MessagesUtils.getMessage("temp_ip_mute", reason, new SimpleDateFormat(MessagesUtils.getRawString("date_format")).format(new Date(duration))));
                    }
                }
            }
        }
    }

    private void addToMySQL() {
        String sql;
        if (punishmentType != PunishmentType.KICK) {
            if (ipPunishment) {
                sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        this.rawIpAddress,
                        this.punished_by_player_uuid,
                        this.punished_by_name,
                        this.duration,
                        this.reason,
                        this.punishmentType.getKey(),
                        System.currentTimeMillis(),
                        this.server
                );
            } else {
                sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                                "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                        this.punished_player_uuid,
                        this.punished_by_player_uuid,
                        this.punished_by_name,
                        this.duration,
                        this.reason,
                        this.punishmentType.getKey(),
                        System.currentTimeMillis(),
                        this.server
                );
            }
        } else {
            sql = String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                            "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                    this.punished_player_uuid,
                    this.punished_by_player_uuid,
                    this.punished_by_name,
                    System.currentTimeMillis(),
                    this.reason,
                    this.punishmentType.getKey(),
                    System.currentTimeMillis(),
                    "global"
            );

        }
        QBungeePunishments.getInstance().getMysql().insert(sql);
    }

    /**
     * Specifically for Ip Punishments!
     * @param uuid to add to queue
     */
    private String addToExecuteQueue(UUID uuid) {
        return String.format("INSERT INTO `punishments`(`punished_uuid`, `punished_by_uuid`, `punished_by_name`, `expire_date`, `reason`, `punishment_type`, `date_added`, `server`) " +
                        "VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",
                uuid,
                this.punished_by_player_uuid,
                this.punished_by_name,
                this.duration,
                this.reason,
                this.punishmentType.getKey(),
                System.currentTimeMillis(),
                this.server);
    }

    @Override
    public String toString() {
        String punished = ipPunishment ? "@ipPunishment=true" + "@rawIpAddress=" + rawIpAddress : "@ipPunishments=false@punished_player_uuid=" + punished_player_uuid;

        return "id=" + UUID.randomUUID() +
                punished +
                "@punishmentType=" + punishmentType +
                "@punished_by_player_uuid=" + (punished_by_player_uuid == null ? "Console" : punished_by_player_uuid) +
                "@punished_by_name=" + punished_by_name +
                "@server=" + server +
                "@duration=" + duration +
                "@reason=" + reason +
                "@perm=" + perm +
                "@silent=" + silent;
    }


    public static Punishment fromString(String query) throws ServerNotExistException, SQLException, PlayerPunishedException {
        String[] args = query.split("@");

        UUID id = UUID.fromString(args[0].split("id=")[1]);
        PunishmentType type = Arrays.stream(PunishmentType.values()).filter(x -> x.getKey().toLowerCase().startsWith(args[3].split("punishmentType=")[1].toLowerCase())).findFirst().get();

        String punished_thing;

        boolean ipPunishment = args[1].split("ipPunishments=")[1].equalsIgnoreCase("true");

        if (ipPunishment) {
            punished_thing = args[2].split("rawIpAddress=")[1];
        } else {
            punished_thing = args[2].split("punished_player_uuid=")[1];
        }

        UUID punished_by_uuid = null;

        if (!args[4].split("punished_by_player_uuid=")[1].equalsIgnoreCase("Console")) {
            punished_by_uuid = UUID.fromString(args[4].split("punished_by_player_uuid=")[1]);
        }

        String punished_by_name = args[5].split("punished_by_name=")[1];

        String server = args[6].split("server=")[1];
        long duration = Long.parseLong(args[7].split("duration=")[1]);
        String reason = args[8].split("reason=")[1];
        boolean perm = args[9].split("perm=")[1].equalsIgnoreCase("true");
        boolean silent = args[10].split("silent=")[1].equalsIgnoreCase("true");

        if (!ipPunishment) {
            return new PunishmentBuilder()
                    .setTargetUUID(UUID.fromString(punished_thing))
                    .setPunishmentType(type)
                    .setModeratorName(punished_by_name)
                    .setModeratorUUID(punished_by_uuid)
                    .setServer(server)
                    .setDuration(duration)
                    .setPermanent(perm)
                    .setReason(reason)
                    .setSilent(silent).build();
        } else {
            return new PunishmentBuilder()
                    .setTargetIpAddress(punished_thing)
                    .setIpPunishment(true)
                    .setPunishmentType(type)
                    .setModeratorName(punished_by_name)
                    .setModeratorUUID(punished_by_uuid)
                    .setServer(server)
                    .setDuration(duration)
                    .setPermanent(perm)
                    .setReason(reason)
                    .setSilent(silent).build();
        }
    }
}
