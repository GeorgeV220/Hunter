package com.georgev22.hunter.listeners;

import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.hooks.HolographicDisplays;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.Updater;
import com.georgev22.hunter.utilities.player.UserData;
import com.georgev22.hunter.utilities.player.UserUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;

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
