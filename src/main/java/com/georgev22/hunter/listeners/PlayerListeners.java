package com.georgev22.hunter.listeners;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.Updater;
import com.georgev22.hunter.utilities.player.UserData;
import com.georgev22.hunter.utilities.player.UserUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

import static com.georgev22.library.utilities.Utils.*;

public class PlayerListeners implements Listener {

    private final HunterPlugin hunterPlugin = HunterPlugin.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (BukkitMinecraftUtils.isLoginDisallowed())
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, BukkitMinecraftUtils.colorize(BukkitMinecraftUtils.getDisallowLoginMessage()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UserData userData = UserData.getUser(event.getPlayer().getUniqueId());
        try {
            userData.load(new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    UserData.getAllUsersMap().append(userData.user().uniqueId(), userData.user());
                    //HOLOGRAMS
                    if (hunterPlugin.getHolograms().isHooked()) {
                        if (!hunterPlugin.getHolograms().getHolograms().isEmpty()) {
                            for (Object hologram : hunterPlugin.getHolograms().getHolograms()) {
                                hunterPlugin.getHolograms().show(hologram, event.getPlayer());
                            }

                            hunterPlugin.getHolograms().updateAll();
                        }
                    }
                    return true;
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    return false;
                }

                @Override
                public Boolean onFailure() {
                    return false;
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
        userData.save(true, new Callback<>() {
            @Override
            public Boolean onSuccess() {
                UserData.getAllUsersMap().append(userData.user().uniqueId(), userData.user());
                return true;
            }

            @Override
            public Boolean onFailure(Throwable throwable) {
                throwable.printStackTrace();
                return onFailure();
            }

            @Override
            public Boolean onFailure() {
                return false;
            }
        });

    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }
        try {
            UserUtils.processKillerBlessedUser(event.getEntity().getKiller(), event.getEntity());
        } catch (IOException e) {
            if (OptionsUtil.DEBUG_ERROR.getBooleanValue())
                e.printStackTrace();
        }
    }

}
