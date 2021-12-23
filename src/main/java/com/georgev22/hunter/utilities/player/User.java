package com.georgev22.hunter.utilities.player;

import com.georgev22.api.maps.ConcurrentObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class User extends ConcurrentObjectMap<String, Object> {


    private final UUID uuid;

    /**
     * Creates a User instance.
     *
     * @param uuid Player Unique identifier
     */
    public User(UUID uuid) {
        this.uuid = uuid;
        append("uuid", uuid);
    }

    /**
     * Creates a User instance initialized with the given map.
     *
     * @param uuid User Unique ID
     * @param map  initial map
     * @see User#User(UUID)
     */
    public User(UUID uuid, final @NotNull ObjectMap<String, Object> map) {
        super(map.append("uuid", uuid));
        this.uuid = uuid;
    }

    /**
     * Returns User's Unique ID
     *
     * @return User's Unique ID
     */
    public UUID uniqueId() {
        return uuid;
    }

    /**
     * Gets the player, regardless if they are offline or
     * online.
     *
     * @return an offline player
     */
    @NotNull
    public OfflinePlayer offlinePlayer() {
        return Bukkit.getOfflinePlayer(uniqueId());
    }

    /**
     * Returns the name of this player
     *
     * @return Player name or null
     */
    @Nullable
    public String name() {
        return getString("name", offlinePlayer().getName());
    }

    public int killstreak() {
        return getInteger("killstreak", 0);
    }

    public double experience() {
        return getDouble("experience", 0D);
    }

    public double multiplier() {
        return getDouble("multiplier", 1.0);
    }

    public int level() {
        return getInteger("level", 0);
    }

    public int totalKills() {
        return getInteger("kills", 0);
    }

    public int prestige() {
        return getInteger("prestige", 0);
    }

    public ObjectMap<String, String> placeholders() {
        return ObjectMap.newHashObjectMap()
                .append("%player%", name())
                .append("%kills%", String.valueOf(totalKills()))
                .append("%kills_roman%", Utils.toRoman(totalKills()))
                .append("%killstreak%", String.valueOf(killstreak()))
                .append("%killstreak_roman%", Utils.toRoman(killstreak()))
                .append("%level%", String.valueOf(level()))
                .append("%level_roman%", Utils.toRoman(level()))
                .append("%multiplier%", String.valueOf(multiplier()))
                .append("%experience%", String.valueOf(experience()))
                .append("%prestige%", String.valueOf(prestige()))
                .append("%prestige_roman%", Utils.toRoman(prestige()))
                ;
    }
}
