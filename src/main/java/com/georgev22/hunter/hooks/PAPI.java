package com.georgev22.hunter.hooks;

import com.georgev22.api.utilities.Utils;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.utilities.player.UserData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPI extends PlaceholderExpansion {

    HunterPlugin plugin = HunterPlugin.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "hunter";
    }

    @Override
    public String getRequiredPlugin() {
        return "Hunter";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GeorgeV22";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        UserData userData = UserData.getUser(offlinePlayer);
        if (identifier.equalsIgnoreCase("player_level")) {
            return String.valueOf(userData.getLevel());
        }
        if (identifier.equalsIgnoreCase("player_level_roman")) {
            return Utils.toRoman(userData.getKills());
        }
        if (identifier.equalsIgnoreCase("player_kills")) {
            return String.valueOf(userData.getKills());
        }
        if (identifier.equalsIgnoreCase("player_kills_roman")) {
            return Utils.toRoman(userData.getKills());
        }
        if (identifier.equalsIgnoreCase("player_killstreak")) {
            return String.valueOf(userData.getKillStreak());
        }
        if (identifier.equalsIgnoreCase("player_killstreak_roman")) {
            return Utils.toRoman(userData.getKillStreak());
        }
        if (identifier.equalsIgnoreCase("player_multiplier")) {
            return String.valueOf(userData.getMultiplier());
        }
        if (identifier.equalsIgnoreCase("player_prestige")) {
            return String.valueOf(userData.getPrestige());
        }
        if (identifier.equalsIgnoreCase("player_prestige_roman")) {
            return Utils.toRoman(userData.getPrestige());
        }
        return null;
    }

}