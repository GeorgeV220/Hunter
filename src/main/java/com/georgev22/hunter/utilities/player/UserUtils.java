package com.georgev22.hunter.utilities.player;

import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.georgev22.library.utilities.Utils.placeHolder;
import static com.georgev22.library.utilities.Utils.toRoman;

public class UserUtils {

    private final static HunterPlugin hunterPlugin = HunterPlugin.getInstance();

    public static void processKillerBlessedUser(@NotNull Player killer, @Nullable Player blessed) throws IOException {
        UserData killerUserData = UserData.getUser(killer);

        if (blessed != null) {
            UserData blessedUserData = UserData.getUser(blessed);

            blessedUserData.setKillstreak(0);
            if (OptionsUtil.BOUNTY_ENABLED.getBooleanValue() & Vault.isHooked()) {
                if (blessedUserData.haveBounty()) {
                    Vault.getEconomy().depositPlayer(killer, blessedUserData.getBounty());
                    MessagesUtil.BOUNTY_PLAYER_COLLECT.msgAll(blessedUserData.user().placeholders().append("%player%", killer.getName()).append("%target%", blessed.getName()), true);
                    blessedUserData.setBounty(0.0);
                }
            }
        }

        killerUserData.setExperience(killerUserData.getExperience() + killerUserData.getMultiplier()).setKills(killerUserData.getKills() + 1).setKillstreak(killerUserData.getKillStreak() + 1);

        if (OptionsUtil.BOUNTY_ENABLED.getBooleanValue() & Vault.isHooked()) {
            if (OptionsUtil.BOUNTY_KILLSTREAK_ENABLED.getBooleanValue()) {
                if (killerUserData.getKillStreak() >= OptionsUtil.BOUNTY_KILLSTREAK.getIntValue()) {
                    if (killerUserData.haveBounty()) {
                        killerUserData.setBounty(killerUserData.getBounty() + (OptionsUtil.BOUNTY_PERCENTAGE_ENABLE.getBooleanValue() ? (killerUserData.getBounty() * (OptionsUtil.BOUNTY_PERCENTAGE.getDoubleValue() / 100)) : OptionsUtil.BOUNTY_BASE.getDoubleValue()));
                    } else {
                        killerUserData.setBounty(OptionsUtil.BOUNTY_BASE.getDoubleValue());
                    }
                }
            }
        }

        // KILLSTREAK REWARDS
        if (OptionsUtil.KILLSTREAK_REWARDS.getBooleanValue()) {
            if (hunterPlugin.getConfig().getString("Rewards.killstreak." + killerUserData.getKillStreak()) != null) {
                hunterPlugin.getConfig()
                        .getStringList("Rewards.killstreak." + killerUserData.getKillStreak() + ".commands").forEach(s -> BukkitMinecraftUtils.runCommand(hunterPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
            }
        }

        // KILL REWARDS
        if (OptionsUtil.KILLS_REWARDS.getBooleanValue()) {
            if (OptionsUtil.KILLS_REWARDS_CLOSEST.getBooleanValue()) {
                int configRewardsKills = hunterPlugin.getConfig().get("Rewards.kills." + killerUserData.getKills()) == null ? Integer.parseInt(Objects.requireNonNull(hunterPlugin.getConfig().getConfigurationSection("Rewards.kills")).getKeys(false).stream()
                        .min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - killerUserData.getKills())))
                        .orElseThrow(() -> new NoSuchElementException("No value present"))) : killerUserData.getKills();
                if (hunterPlugin.getConfig().getString("Rewards.kills." + configRewardsKills) != null) {
                    hunterPlugin.getConfig()
                            .getStringList("Rewards.kills." + configRewardsKills + ".commands").forEach(s -> BukkitMinecraftUtils.runCommand(hunterPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            } else {
                if (hunterPlugin.getConfig().getString("Rewards.killstreak." + killerUserData.getKillStreak()) != null) {
                    hunterPlugin.getConfig()
                            .getStringList("Rewards.killstreak." + killerUserData.getKillStreak() + ".commands").forEach(s -> BukkitMinecraftUtils.runCommand(hunterPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            }
        }

        // DISCORD KILL WEBHOOK
        if (OptionsUtil.KILLS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            BukkitMinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "kill", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
        }

        // DISCORD KILLSTREAK WEBHOOK
        if (OptionsUtil.KILLSTREAK_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            BukkitMinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "killstreak", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
        }

        if (OptionsUtil.KILLSTREAK_MESSAGE.getBooleanValue()) {
            if (killerUserData.getKillStreak() % OptionsUtil.KILLSTREAK_MESSAGE_EVERY.getIntValue() == 0) {
                if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                    MessagesUtil.KILLSTREAK.msgAll(new HashObjectMap<String, String>().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(killerUserData.getKillStreak())), true);
                } else if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                    MessagesUtil.KILLSTREAK.msg(killer, new HashObjectMap<String, String>().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(killerUserData.getKillStreak())), true);
                }
            }
        }

        int configLevel = hunterPlugin.getConfig().get("Levels." + killerUserData.getLevel() + 1) == null ? Integer.parseInt(hunterPlugin.getConfig().getConfigurationSection("Levels").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (killerUserData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : killerUserData.getLevel() + 1;

        int configLevelRewards = hunterPlugin.getConfig().get("Rewards.level up." + killerUserData.getLevel() + 1) == null ? Integer.parseInt(hunterPlugin.getConfig().getConfigurationSection("Rewards.level up").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (killerUserData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : killerUserData.getLevel() + 1;

        if (OptionsUtil.DEBUG.getBooleanValue()) {
            BukkitMinecraftUtils.debug(hunterPlugin, "level rewards value: " + configLevelRewards);
            BukkitMinecraftUtils.debug(hunterPlugin, "config level rewards value: " + hunterPlugin.getConfig().getStringList("Rewards.level up." + configLevelRewards + ".commands"));
            BukkitMinecraftUtils.debug(hunterPlugin, "level value: " + configLevel);
            BukkitMinecraftUtils.debug(hunterPlugin, "config level value: " + hunterPlugin.getConfig().getInt("Levels." + configLevel));
        }

        if (killerUserData.getExperience() >= hunterPlugin.getConfig().getInt("Levels." + configLevel)) {
            killerUserData.setLevel(killerUserData.getLevel() + 1).setExperience(0);
            if (OptionsUtil.LEVELS_MESSAGE.getBooleanValue()) {
                if (killerUserData.getLevel() % OptionsUtil.LEVELS_MESSAGE__EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.LEVEL_UP.msgAll(new HashObjectMap<String, String>().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    } else if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.LEVEL_UP.msg(killer, new HashObjectMap<String, String>().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    }
                }
            }
            if (OptionsUtil.LEVELS_TITLE.getBooleanValue()) {
                if (killerUserData.getLevel() % OptionsUtil.LEVELS_TITLE_EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.TITLE_LEVEL_UP.titleAll(
                                OptionsUtil.LEVELS_TITLE_FADE_IN.getIntValue(),
                                OptionsUtil.LEVELS_TITLE_STAY.getIntValue(),
                                OptionsUtil.LEVELS_TITLE_FADE_OUT.getIntValue(),
                                new HashObjectMap<String, String>()
                                        .append("%player%", killer.getName())
                                        .append("%level%", String.valueOf(killerUserData.getLevel()))
                                        .append("%level_roman%", toRoman(killerUserData.getLevel())),
                                true);
                    } else if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.TITLE_LEVEL_UP.title(
                                killer,
                                OptionsUtil.LEVELS_TITLE_FADE_IN.getIntValue(),
                                OptionsUtil.LEVELS_TITLE_STAY.getIntValue(),
                                OptionsUtil.LEVELS_TITLE_FADE_OUT.getIntValue(),
                                new HashObjectMap<String, String>()
                                        .append("%player%", killer.getName())
                                        .append("%level%", String.valueOf(killerUserData.getLevel()))
                                        .append("%level_roman%", toRoman(killerUserData.getLevel())),
                                true);
                    }
                }
            }

            // LEVEL UP REWARDS
            if (OptionsUtil.LEVELS_REWARDS.getBooleanValue()) {
                if (hunterPlugin.getConfig().get("Rewards.level up." + configLevelRewards) != null) {
                    hunterPlugin.getConfig()
                            .getStringList("Rewards.level up." + configLevelRewards + ".commands").forEach(s -> BukkitMinecraftUtils.runCommand(hunterPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            }

            // DISCORD LEVEL UP WEBHOOK
            if (OptionsUtil.LEVELS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
                FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
                BukkitMinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "levelup", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
            }

        }
        // HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            hunterPlugin.getHolograms().updateAll();
    }

    /**
     * A map with all hologram placeholders
     *
     * @return a map with all hologram placeholders
     */
    public static @NotNull ObjectMap<String, String> getPlaceholdersMap() {
        final ObjectMap<String, String> map = new HashObjectMap<>();
        int levelTop = 1;
        for (Map.Entry<String, Integer> b : UserData.getTopPlayersByLevels(OptionsUtil.LEVELS_TOP.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%toplevel-" + levelTop + "%", args[0]).append("%level-" + levelTop + "%", args[1]);
            levelTop++;
        }
        int killsTop = 1;
        for (Map.Entry<String, Integer> b : UserData.getTopPlayersByKills(OptionsUtil.KILLS_TOP.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%topkills-" + killsTop + "%", args[0]).append("%kills-" + killsTop + "%", args[1]);
            killsTop++;
        }
        int killstreakTop = 1;
        for (Map.Entry<String, Integer> b : UserData.getTopPlayersByKillstreak(OptionsUtil.KILLS_TOP.getIntValue()).entrySet()) {
            String[] args = String.valueOf(b).split("=");
            map.append("%topkillstreak-" + killstreakTop + "%", args[0]).append("%killstreak-" + killstreakTop + "%", args[1]);
            killstreakTop++;
        }
        return map;
    }

}
