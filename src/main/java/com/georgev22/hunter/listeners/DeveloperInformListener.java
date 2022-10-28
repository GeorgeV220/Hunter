package com.georgev22.hunter.listeners;

import com.georgev22.api.maps.HashObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;


public class DeveloperInformListener implements Listener {

    private final List<ObjectMap.Pair<String, UUID>> inform = Lists.newArrayList(
            ObjectMap.Pair.create("Shin1gamiX", UUID.fromString("7cc1d444-fe6f-4063-a426-b62fdfea7dab")),
            ObjectMap.Pair.create("GeorgeV22", UUID.fromString("a4f5cd7f-362f-4044-931e-7128b4e6bad9"))
    );

    @EventHandler
    private void onJoin(final PlayerJoinEvent e) {
        final OfflinePlayer player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        final String name = player.getName();

        final ObjectMap.Pair<String, UUID> pair = ObjectMap.Pair.create(name, uuid);

        boolean found = false;

        for (ObjectMap.Pair<String, UUID> loop : this.inform) {
            if (loop.key().equals(pair.key())) {
                found = true;
                break;
            }
            if (loop.value().equals(pair.value())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(HunterPlugin.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }

            MinecraftUtils.msg(player.getPlayer(), joinMessage, new HashObjectMap<String, String>()
                    .append("%player%", e.getPlayer().getName())
                    .append("%version%", HunterPlugin.getInstance().getDescription().getVersion())
                    .append("%package%", HunterPlugin.getInstance().getClass().getPackage().getName())
                    .append("%name%", HunterPlugin.getInstance().getDescription().getName())
                    .append("%author%", String.join(", ", HunterPlugin.getInstance().getDescription().getAuthors()))
                    .append("%main%", HunterPlugin.getInstance().getDescription().getMain())
                    .append("%javaversion%", System.getProperty("java.version"))
                    .append("%serverversion%", MinecraftUtils.MinecraftVersion.getCurrentVersion().name()), false);
        }, 20L * 10L);

    }

    private final static List<String> joinMessage = Lists.newArrayList(

            "",

            "",

            "&7Hey &f%player%&7, details are listed below.",

            "&7Version: &c%version%",

            "&7Java Version: &c%javaversion%",

            "&7Server Version: &c%serverversion%",

            "&7Name: &c%name%",

            "&7Author: &c%author%",

            "&7Main package: &c%package%",

            "&7Main path: &c%main%",

            "&7Experimental Features: &c" + OptionsUtil.EXPERIMENTAL_FEATURES.getBooleanValue(),

            ""

    );

}
