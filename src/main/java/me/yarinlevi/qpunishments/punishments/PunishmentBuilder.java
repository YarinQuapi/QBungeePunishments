package me.yarinlevi.qpunishments.punishments;

import lombok.Getter;
import me.yarinlevi.qpunishments.exceptions.PlayerPunishedException;
import me.yarinlevi.qpunishments.exceptions.ServerNotExistException;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * @author YarinQuapi
 */
public class PunishmentBuilder {
    @Nullable private UUID targetPlayerUUID;
    @Nullable private String rawIpAddress;
    private PunishmentType type;
    @Nullable private UUID moderatorUUID = null;
    private String moderatorName;
    private long duration;
    private boolean permanent;
    private String server = "global";
    @Nullable private String reason;
    @Getter private boolean silent;
    @Getter private boolean ipPunishment = false;

    public PunishmentBuilder setTargetUUID(UUID targetUUID) {
        this.targetPlayerUUID = targetUUID;

        return this;
    }

    public PunishmentBuilder setTargetIpAddress(String ipAddress) {
        this.rawIpAddress = ipAddress;

        return this;
    }

    /**
     * Set punishment type
     * @param type Type
     * @return Builder
     */
    public PunishmentBuilder setPunishmentType(PunishmentType type) {
        this.type = type;

        return this;
    }

    /**
     * Set the executor's uuid
     * @param moderatorUUID UUID
     * @return Builder
     */
    public PunishmentBuilder setModeratorUUID(@Nullable UUID moderatorUUID) {
        this.moderatorUUID = moderatorUUID;

        return this;
    }

    public PunishmentBuilder setServer(String serverName) throws ServerNotExistException {
        if (!Objects.equals(serverName, "global") && QBungeePunishments.getInstance().getServer().getAllServers().stream().noneMatch(x -> x.getServerInfo().getName().equals(serverName))) throw new ServerNotExistException();

        this.server = serverName;

        return this;
    }

    public PunishmentBuilder setModeratorName(String moderatorName) {
        this.moderatorName = moderatorName;

        return this;
    }

    public PunishmentBuilder setDuration(long duration) {
        this.duration = duration;

        return this;
    }

    public PunishmentBuilder setPermanent(boolean permanent) {
        this.permanent = permanent;

        if (permanent) this.duration = 0;

        return this;
    }

    public PunishmentBuilder setReason(@Nullable String reason) {
        this.reason = reason;

        return this;
    }

    public PunishmentBuilder setSilent(boolean silent) {
        this.silent = silent;

        return this;
    }

    public PunishmentBuilder setIpPunishment(boolean ipPunishment) {
        this.ipPunishment = ipPunishment;

        return this;
    }

    public Punishment build() throws SQLException, PlayerPunishedException {
        String sql = String.format("SELECT * FROM punishments WHERE `punished_uuid`=\"%s\" AND `punishment_type`=\"%s\" AND `expire_date` > \"%s\" OR `punished_uuid`=\"%s\" AND `expire_date`=0 AND `punishment_type`=\"%s\" ORDER BY id DESC;",
                this.targetPlayerUUID, this.type, System.currentTimeMillis(), this.targetPlayerUUID, this.type);

        ResultSet rs = QBungeePunishments.getInstance().getMysql().get(sql);

        if (rs != null && rs.next() && !rs.getBoolean("bypass_expire_date") && rs.getString("server").equalsIgnoreCase("global")) {
            throw new PlayerPunishedException();
        } else {
            return new Punishment(
                    ipPunishment ? rawIpAddress : targetPlayerUUID.toString(),
                    type,
                    moderatorUUID,
                    moderatorName,
                    server,
                    reason,
                    duration,
                    permanent,
                    silent,
                    ipPunishment);
        }
    }
}