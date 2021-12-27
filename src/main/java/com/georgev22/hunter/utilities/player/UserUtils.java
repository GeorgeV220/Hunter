package com.georgev22.hunter.utilities.player;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.hooks.HolographicDisplays;
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
import java.util.NoSuchElementException;

import static com.georgev22.api.utilities.Utils.placeHolder;
import static com.georgev22.api.utilities.Utils.toRoman;

public class UserUtils {

    private final static Main mainPlugin = Main.getInstance();

    public static void processKillerBlessedUser(@NotNull Player killer, @Nullable Player blessed) throws IOException {
        if (killer == null) {
            return;
        }
        UserData killerUserData = UserData.getUser(killer);

        if (blessed != null) {
            UserData blessedUserData = UserData.getUser(blessed);

            blessedUserData.setKillstreak(0);
            if (OptionsUtil.BOUNTY_ENABLED.getBooleanValue() & Vault.isHooked()) {
                if (blessedUserData.haveBounty()) {
                    Vault.getEconomy().depositPlayer(killer, blessedUserData.getBounty());
                    blessedUserData.setBounty(0.0);
                }
            }
        }

        killerUserData.setExperience(killerUserData.getExperience() + killerUserData.getMultiplier()).setKills(killerUserData.getKills() + 1).setKillstreak(killerUserData.getKillStreak() + 1);

        if (OptionsUtil.BOUNTY_ENABLED.getBooleanValue() & Vault.isHooked()) {
            if (OptionsUtil.BOUNTY_KILLSTREAK_ENABLED.getBooleanValue()) {
                if (killerUserData.getKillStreak() >= OptionsUtil.BOUNTY_KILLSTREAK.getIntValue()) {
                    if (killerUserData.haveBounty()) {
                        killerUserData.setBounty(killerUserData.getBounty() + (killerUserData.getBounty() * (OptionsUtil.BOUNTY_PERCENTAGE.getDoubleValue() / 100)));
                    } else {
                        killerUserData.setBounty(OptionsUtil.BOUNTY_BASE.getDoubleValue());
                    }
                }
            }
        }

        // KILLSTREAK REWARDS
        if (OptionsUtil.KILLSTREAK_REWARDS.getBooleanValue()) {
            if (mainPlugin.getConfig().getString("Rewards.killstreak." + killerUserData.getKillStreak()) != null) {
                mainPlugin.getConfig()
                        .getStringList("Rewards.killstreak." + killerUserData.getKillStreak() + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
            }
        }

        // KILL REWARDS
        if (OptionsUtil.KILLS_REWARDS.getBooleanValue()) {
            if (OptionsUtil.KILLS_REWARDS_CLOSEST.getBooleanValue()) {
                int configRewardsKills = mainPlugin.getConfig().get("Rewards.kills." + killerUserData.getKills()) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Rewards.kills").getKeys(false).stream()
                        .min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - killerUserData.getKills())))
                        .orElseThrow(() -> new NoSuchElementException("No value present"))) : killerUserData.getKills();
                if (mainPlugin.getConfig().getString("Rewards.kills." + configRewardsKills) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.kills." + configRewardsKills + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            } else {
                if (mainPlugin.getConfig().getString("Rewards.killstreak." + killerUserData.getKillStreak()) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.killstreak." + killerUserData.getKillStreak() + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            }
        }

        // DISCORD KILL WEBHOOK
        if (OptionsUtil.KILLS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "kill", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
        }

        // DISCORD KILLSTREAK WEBHOOK
        if (OptionsUtil.KILLSTREAK_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "killstreak", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
        }

        if (OptionsUtil.KILLSTREAK_MESSAGE.getBooleanValue()) {
            if (killerUserData.getKillStreak() % OptionsUtil.KILLSTREAK_MESSAGE_EVERY.getIntValue() == 0) {
                if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                    MessagesUtil.KILLSTREAK.msgAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(killerUserData.getKillStreak())), true);
                } else if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                    MessagesUtil.KILLSTREAK.msg(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(killerUserData.getKillStreak())), true);
                }
            }
        }

        int configLevel = mainPlugin.getConfig().get("Levels." + killerUserData.getLevel() + 1) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Levels").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (killerUserData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : killerUserData.getLevel() + 1;

        int configLevelRewards = mainPlugin.getConfig().get("Rewards.level up." + killerUserData.getLevel() + 1) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Rewards.level up").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (killerUserData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : killerUserData.getLevel() + 1;

        if (OptionsUtil.DEBUG.getBooleanValue()) {
            MinecraftUtils.debug(mainPlugin, "level rewards value: " + configLevelRewards);
            MinecraftUtils.debug(mainPlugin, "config level rewards value: " + mainPlugin.getConfig().getStringList("Rewards.level up." + configLevelRewards + ".commands"));
            MinecraftUtils.debug(mainPlugin, "level value: " + configLevel);
            MinecraftUtils.debug(mainPlugin, "config level value: " + mainPlugin.getConfig().getInt("Levels." + configLevel));
        }

        if (killerUserData.getExperience() >= mainPlugin.getConfig().getInt("Levels." + configLevel)) {
            killerUserData.setLevel(killerUserData.getLevel() + 1).setExperience(0);
            if (OptionsUtil.LEVELS_MESSAGE.getBooleanValue()) {
                if (killerUserData.getLevel() % OptionsUtil.LEVELS_MESSAGE__EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.LEVEL_UP.msgAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    } else if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.LEVEL_UP.msg(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    }
                }
            }
            if (OptionsUtil.LEVELS_TITLE.getBooleanValue()) {
                if (killerUserData.getLevel() % OptionsUtil.LEVELS_TITLE_EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.TITLE_LEVEL_UP.titleAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    } else if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.TITLE_LEVEL_UP.title(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(killerUserData.getLevel())).append("%level_roman%", toRoman(killerUserData.getLevel())), true);
                    }
                }
            }

            // LEVEL UP REWARDS
            if (OptionsUtil.LEVELS_REWARDS.getBooleanValue()) {
                if (mainPlugin.getConfig().get("Rewards.level up." + configLevelRewards) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.level up." + configLevelRewards + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, killerUserData.user().placeholders(), true)));
                }
            }

            // DISCORD LEVEL UP WEBHOOK
            if (OptionsUtil.LEVELS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
                FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
                MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "levelup", killerUserData.user().placeholders(), killerUserData.user().placeholders()).execute();
            }

        }
        // HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            HolographicDisplays.updateAll();
    }

}
