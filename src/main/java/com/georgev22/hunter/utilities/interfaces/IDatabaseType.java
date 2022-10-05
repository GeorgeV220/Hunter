package com.georgev22.hunter.utilities.interfaces;

import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.utilities.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.georgev22.api.utilities.Utils.*;

public interface IDatabaseType {

    void save(User user) throws Exception;

    void load(User user, Callback<Boolean> callback) throws Exception;

    void setupUser(User user, Callback<Boolean> callback) throws Exception;

    default void reset(@NotNull User user) throws Exception {
        user
                .append("kills", 0)
                .append("killstreak", 0)
                .append("multiplier", 1.0)
                .append("level", 0)
                .append("experience", 0.0D)
                .append("prestige", 0)
        ;
        save(user);
        MinecraftUtils.debug(Main.getInstance(), "User " + user.name() + " has been reset!");
    }

    void delete(User user) throws Exception;

    boolean playerExists(User user) throws Exception;

    ObjectMap<UUID, User> getAllUsers() throws Exception;

}
