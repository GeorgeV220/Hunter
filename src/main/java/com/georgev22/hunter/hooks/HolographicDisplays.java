package com.georgev22.hunter.hooks;

import com.georgev22.api.maps.ConcurrentObjectMap;
import com.georgev22.api.maps.HashObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.api.utilities.Utils;
import com.georgev22.hunter.Main;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.player.UserData;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author GeorgeV22
 */
public class HolographicDisplays {

    private final static FileManager fileManager = FileManager.getInstance();
    private final static CFG dataCFG = fileManager.getData();
    private final static FileConfiguration data = dataCFG.getFileConfiguration();
    private final static Main mainPlugin = Main.getInstance();
    private static final ObjectMap<String, Hologram> hologramMap = new ConcurrentObjectMap<>();

    /**
     * Create a hologram
     *
     * @param name     Hologram name.
     * @param location Hologram location.
     * @param type     Hologram type.
     * @param save     Save the hologram in the file.
     * @return {@link Hologram} instance.
     */
    public static Hologram create(String name, Location location, String type, boolean save) {
        Hologram hologram = getHologramMap().get(name);
        if (hologram == null) {
            hologram = HologramsAPI.createHologram(mainPlugin, location);
            getHologramMap().append(name, hologram);
        }

        for (String line : fileManager.getConfig().getFileConfiguration().getStringList("Holograms." + type)) {
            hologram.appendTextLine(MinecraftUtils.colorize(line));
        }

        if (save) {
            data.set("Holograms." + name + ".location", location);
            data.set("Holograms." + name + ".type", type);
            dataCFG.saveFile();
        }
        return hologram;
    }

    /**
     * Remove a hologram.
     *
     * @param name Hologram name.
     * @param save Save the changes in file.
     */
    public static void remove(String name, boolean save) {
        Hologram hologram = getHologramMap().remove(name);

        hologram.delete();

        if (save) {
            data.set("Holograms." + name, null);
            dataCFG.saveFile();
        }
    }

    /**
     * Show a hologram to a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to show the hologram.
     */
    public static void show(String name, Player player) {
        Hologram hologram = getHologramMap().get(name);

        if (hologram == null) {
            MinecraftUtils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }
        hologram.getVisibilityManager().showTo(player);
    }

    /**
     * Hide a hologram from a specific player.
     *
     * @param name   Hologram name.
     * @param player Player to hide the hologram.
     */
    public static void hide(String name, Player player) {
        Hologram hologram = getHologramMap().get(name);

        if (hologram == null) {
            MinecraftUtils.msg(player, "Hologram " + name + " doesn't exist");
            return;
        }

        hologram.getVisibilityManager().hideTo(player);
    }

    /**
     * Show a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    public static void show(@NotNull Hologram hologram, Player player) {
        hologram.getVisibilityManager().showTo(player);
    }

    /**
     * Hide a hologram from a specific player.
     *
     * @param hologram Hologram instance.
     * @param player   Player to hide the hologram.
     */
    public static void hide(@NotNull Hologram hologram, Player player) {
        hologram.getVisibilityManager().hideTo(player);
    }

    /**
     * Return all holograms in a collection.
     *
     * @return all holograms in a collection.
     */
    public static @NotNull Collection<Hologram> getHolograms() {
        return getHologramMap().values();
    }

    /**
     * Return a {@link Hologram} from hologram name.
     *
     * @param name Hologram name
     * @return a {@link Hologram} from hologram name.
     */
    public static Hologram getHologram(String name) {
        return getHologramMap().get(name);
    }

    /**
     * Check if a hologram exists
     *
     * @param name Hologram name.
     * @return if the hologram exists
     */
    public static boolean hologramExists(String name) {
        return getHologramMap().get(name) != null;
    }

    /**
     * Update the lines in a specific hologram
     *
     * @param hologram     {@link Hologram} instance to change the lines.
     * @param lines        The new lines.
     * @param placeholders The placeholders.
     * @return the updated {@link Hologram} instance.
     */
    @Contract("_, _, _ -> param1")
    public static Hologram updateHologram(Hologram hologram, @NotNull List<String> lines, ObjectMap<String, String> placeholders) {
        int i = 0;
        for (final String key : lines) {
            for (String placeholder : placeholders.keySet()) {
                if (key.contains(placeholder)) {
                    TextLine line = (TextLine) hologram.getLine(i);
                    line.setText(Utils.placeHolder(MinecraftUtils.colorize(key), placeholders, true));
                    break;
                }
            }
            ++i;
        }
        return hologram;
    }

    /**
     * Update all {@link Hologram} instances.
     */
    public static void updateAll() {
        if (data.get("Holograms") == null)
            return;
        for (String hologramName : data.getConfigurationSection("Holograms").getKeys(false)) {
            Hologram hologram = getHologram(hologramName);
            HolographicDisplays.updateHologram(hologram, mainPlugin.getConfig().getStringList("Holograms." + data.getString("Holograms." + hologramName + ".type")), getPlaceholderMap());
            getPlaceholderMap().clear();
        }
    }

    /**
     * @return A map with all the holograms.
     */
    public static ObjectMap<String, Hologram> getHologramMap() {
        return hologramMap;
    }

    /**
     * A map with all hologram placeholders
     *
     * @return a map with all hologram placeholders
     */
    public static @NotNull ObjectMap<String, String> getPlaceholderMap() {
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
