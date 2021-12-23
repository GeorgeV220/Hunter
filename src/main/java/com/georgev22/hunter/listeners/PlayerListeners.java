package com.georgev22.hunter.listeners;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.hooks.HolographicDisplays;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.Updater;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.player.UserData;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.*;

import static com.georgev22.api.utilities.Utils.*;

public class PlayerListeners implements Listener {

    private final Main mainPlugin = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLogin(PlayerPreLoginEvent event) {
        if (MinecraftUtils.isLoginDisallowed())
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, MinecraftUtils.colorize(MinecraftUtils.getDisallowLoginMessage()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UserData userData = UserData.getUser(event.getPlayer().getUniqueId());
        try {
            userData.load(new Callback() {
                @Override
                public void onSuccess() {
                    UserData.getAllUsersMap().append(userData.user().uniqueId(), userData.user());
                    //HOLOGRAMS
                    if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
                        if (!HolographicDisplays.getHolograms().isEmpty()) {
                            for (Hologram hologram : HolographicDisplays.getHolograms()) {
                                HolographicDisplays.show(hologram, event.getPlayer());
                            }

                            HolographicDisplays.updateAll();
                        }
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        //UPDATER
        if (OptionsUtil.UPDATER.getBooleanValue()) {
            if (event.getPlayer().hasPermission("killstreak.updater") || event.getPlayer().isOp()) {
                new Updater(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserData userData = UserData.getUser(event.getPlayer().getUniqueId());
        userData.save(true, new Callback() {
            @Override
            public void onSuccess() {
                UserData.getAllUsersMap().append(userData.user().uniqueId(), userData.user());
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) throws IOException {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        UserData.getUser(event.getEntity()).setKillstreak(0);
        UserData userData = UserData.getUser(killer.getUniqueId());
        userData.setExperience(userData.getExperience() + userData.getMultiplier()).setKills(userData.getKills() + 1).setKillstreak(userData.getKillStreak() + 1);

        // KILLSTREAK REWARDS
        if (OptionsUtil.KILLSTREAK_REWARDS.getBooleanValue()) {
            if (mainPlugin.getConfig().getString("Rewards.killstreak." + userData.getKillStreak()) != null) {
                mainPlugin.getConfig()
                        .getStringList("Rewards.killstreak." + userData.getKillStreak() + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, userData.user().placeholders(), true)));
            }
        }

        // KILL REWARDS
        if (OptionsUtil.KILLS_REWARDS.getBooleanValue()) {
            if (OptionsUtil.KILLS_REWARDS_CLOSEST.getBooleanValue()) {
                int configRewardsKills = mainPlugin.getConfig().get("Rewards.kills." + userData.getKills()) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Rewards.kills").getKeys(false).stream()
                        .min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - userData.getKills())))
                        .orElseThrow(() -> new NoSuchElementException("No value present"))) : userData.getKills();
                if (mainPlugin.getConfig().getString("Rewards.kills." + configRewardsKills) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.kills." + configRewardsKills + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, userData.user().placeholders(), true)));
                }
            } else {
                if (mainPlugin.getConfig().getString("Rewards.killstreak." + userData.getKillStreak()) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.killstreak." + userData.getKillStreak() + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, userData.user().placeholders(), true)));
                }
            }
        }

        // DISCORD KILL WEBHOOK
        if (OptionsUtil.KILLS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "kill", userData.user().placeholders(), userData.user().placeholders()).execute();
        }

        // DISCORD KILLSTREAK WEBHOOK
        if (OptionsUtil.KILLSTREAK_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
            FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
            MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "killstreak", userData.user().placeholders(), userData.user().placeholders()).execute();
        }

        if (OptionsUtil.KILLSTREAK_MESSAGE.getBooleanValue()) {
            if (userData.getKillStreak() % OptionsUtil.KILLSTREAK_MESSAGE_EVERY.getIntValue() == 0) {
                if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                    MessagesUtil.KILLSTREAK.msgAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(userData.getKillStreak())), true);
                } else if (OptionsUtil.KILLSTREAK_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                    MessagesUtil.KILLSTREAK.msg(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%killstreak%", String.valueOf(userData.getKillStreak())), true);
                }
            }
        }

        int configLevel = mainPlugin.getConfig().get("Levels." + userData.getLevel() + 1) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Levels").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (userData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : userData.getLevel() + 1;

        int configLevelRewards = mainPlugin.getConfig().get("Rewards.level up." + userData.getLevel() + 1) == null ? Integer.parseInt(mainPlugin.getConfig().getConfigurationSection("Rewards.level up").getKeys(false).stream().min(Comparator.comparingInt(i -> Math.abs(Integer.parseInt(i) - (userData.getLevel() + 1)))).orElseThrow(() -> new NoSuchElementException("No value present")))
                : userData.getLevel() + 1;

        if (OptionsUtil.DEBUG.getBooleanValue()) {
            MinecraftUtils.debug(mainPlugin, "level rewards value: " + configLevelRewards);
            MinecraftUtils.debug(mainPlugin, "config level rewards value: " + mainPlugin.getConfig().getStringList("Rewards.level up." + configLevelRewards + ".commands"));
            MinecraftUtils.debug(mainPlugin, "level value: " + configLevel);
            MinecraftUtils.debug(mainPlugin, "config level value: " + mainPlugin.getConfig().getInt("Levels." + configLevel));
        }

        if (userData.getExperience() >= mainPlugin.getConfig().getInt("Levels." + configLevel)) {
            userData.setLevel(userData.getLevel() + 1).setExperience(0);
            if (OptionsUtil.LEVELS_MESSAGE.getBooleanValue()) {
                if (userData.getLevel() % OptionsUtil.LEVELS_MESSAGE__EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.LEVEL_UP.msgAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(userData.getLevel())).append("%level_roman%", toRoman(userData.getLevel())), true);
                    } else if (OptionsUtil.LEVELS_MESSAGE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.LEVEL_UP.msg(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(userData.getLevel())).append("%level_roman%", toRoman(userData.getLevel())), true);
                    }
                }
            }
            if (OptionsUtil.LEVELS_TITLE.getBooleanValue()) {
                if (userData.getLevel() % OptionsUtil.LEVELS_TITLE_EVERY.getIntValue() == 0) {
                    if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("all")) {
                        MessagesUtil.TITLE_LEVEL_UP.titleAll(ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(userData.getLevel())).append("%level_roman%", toRoman(userData.getLevel())), true);
                    } else if (OptionsUtil.LEVELS_TITLE_RECEIVER.getStringValue().equalsIgnoreCase("player")) {
                        MessagesUtil.TITLE_LEVEL_UP.title(killer, ObjectMap.newHashObjectMap().append("%player%", killer.getName()).append("%level%", String.valueOf(userData.getLevel())).append("%level_roman%", toRoman(userData.getLevel())), true);
                    }
                }
            }

            // LEVEL UP REWARDS
            if (OptionsUtil.LEVELS_REWARDS.getBooleanValue()) {
                if (mainPlugin.getConfig().get("Rewards.level up." + configLevelRewards) != null) {
                    mainPlugin.getConfig()
                            .getStringList("Rewards.level up." + configLevelRewards + ".commands").forEach(s -> MinecraftUtils.runCommand(mainPlugin, placeHolder(s, userData.user().placeholders(), true)));
                }
            }

            // PLAY SOUND
            /*if (OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
                if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
                    event.getEntity().playSound(event.getEntity().getLocation(), XSound
                                    .matchXSound(OptionsUtil.SOUND.getStringValue()).get().parseSound(),
                            1000, 1);
                    if (OptionsUtil.DEBUG.getBooleanValue()) {
                        MinecraftUtils.debug(mainPlugin, "========================================================");
                        MinecraftUtils.debug(mainPlugin, "SoundCategory doesn't exist in versions below 1.12");
                        MinecraftUtils.debug(mainPlugin, "SoundCategory doesn't exist in versions below 1.12");
                        MinecraftUtils.debug(mainPlugin, "========================================================");
                    }
                } else {
                    event.getEntity().playSound(event.getEntity().getLocation(), XSound
                                    .matchXSound(OptionsUtil.SOUND.getStringValue()).get().parseSound(),
                            org.bukkit.SoundCategory.valueOf(OptionsUtil.SOUND_CHANNEL.getStringValue()),
                            1000, 1);
                }
            }*/

            // DISCORD LEVEL UP WEBHOOK
            if (OptionsUtil.LEVELS_DISCORD.getBooleanValue() & OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue()) {
                FileConfiguration discordFileConfiguration = FileManager.getInstance().getDiscord().getFileConfiguration();
                MinecraftUtils.buildDiscordWebHookFromConfig(discordFileConfiguration, "levelup", userData.user().placeholders(), userData.user().placeholders()).execute();
            }

        }
        // HOLOGRAM UPDATE
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays"))
            HolographicDisplays.updateAll();
    }

}
