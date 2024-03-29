package com.georgev22.hunter.utilities;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.hunter.HunterPlugin;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;

public class Updater {

    private final HunterPlugin hunterPlugin = HunterPlugin.getInstance();
    private final String localVersion = hunterPlugin.getDescription().getVersion();
    private String onlineVersion;

    {
        try {
            onlineVersion = getOnlineVersion();
        } catch (IOException e) {
            BukkitMinecraftUtils.debug(hunterPlugin, "Failed to check for an update on Git.", "Either Git or you are offline or are slow to respond.");
        }
    }

    public Updater() {
        if (onlineVersion == null)
            return;
        Bukkit.getScheduler().runTaskTimerAsynchronously(hunterPlugin, () -> {
            hunterPlugin.getLogger().info("Checking for Updates ... ");
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                BukkitMinecraftUtils.debug(hunterPlugin, "You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                BukkitMinecraftUtils.debug(hunterPlugin,
                        "New stable version available!",
                        "Version: " + onlineVersion + ". You are running version: " + localVersion,
                        OptionsUtil.UPDATER_DOWNLOAD.getBooleanValue() ? "Update at: https://github.com/GeorgeV220/Hunter/releases/" : "The new update will be automatically downloaded!!");
                if (OptionsUtil.UPDATER_DOWNLOAD.getBooleanValue()) {
                    downloadLatest(null);
                }
            } else {
                BukkitMinecraftUtils.debug(hunterPlugin, "You are currently using the " + localVersion + " version which is under development.",
                        "Your version is " + localVersion,
                        "Latest released version is " + onlineVersion,
                        "If you have problems contact me on discord or github. Thank you for testing this version");
            }

        }, 20L, 20 * 7200);
    }

    public Updater(Player player) {
        if (onlineVersion == null)
            return;
        Bukkit.getScheduler().runTaskAsynchronously(hunterPlugin, () -> {
            BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6Checking for Updates ...");
            if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 0) {
                BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6You are running the newest build.");
            } else if (compareVersions(onlineVersion.replace("v", ""), localVersion.replace("v", "")) == 1) {
                BukkitMinecraftUtils.msg(player,
                        "&e&lUpdater &8» &6New version available!");
                BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6Version: &c"
                        + onlineVersion + ". &6You are running version: &c" + localVersion);
                BukkitMinecraftUtils.msg(player, OptionsUtil.UPDATER_DOWNLOAD.getBooleanValue() ? "&e&lUpdater &8» &6Update at: https://github.com/GeorgeV220/Hunter/releases/" : "&e&lUpdater &8» &6The new update will be automatically downloaded!!");
                if (OptionsUtil.UPDATER_DOWNLOAD.getBooleanValue()) {
                    downloadLatest(player);
                }

            } else {
                BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6You are currently using the &c" + localVersion + " &6version which is under development. If you have problems contact me on discord or github");
                BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6Your version is &c" + localVersion);
                BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6Latest released version is &c" + onlineVersion);
            }
        });
    }


    private int compareVersions(@NotNull String version1, @NotNull String version2) {
        if (onlineVersion == null)
            return 0;
        if (version1.contains("alpha") | version1.contains("beta")) {
            return -1;
        }

        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

    private @NotNull String getOnlineVersion() throws IOException {
        System.setProperty("http.agent", "Chrome");
        HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.github.com/repos/GeorgeV220/Hunter/tags").openConnection();

        con.setDoOutput(true);

        con.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        String jsonText = sb.toString();
        JsonElement jsonElement = JsonParser.parseString(jsonText);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        return jsonArray.get(0).getAsJsonObject().get("name").getAsString().replace("\"", "");
    }

    private void downloadLatest(@Nullable Player player) {
        if (player == null)
            BukkitMinecraftUtils.debug(hunterPlugin,
                    "New stable version is downloading (" + onlineVersion + ")!");
        else
            BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6New stable version is downloading (&c" + onlineVersion + "&6)!");
        File tempFile = new File(hunterPlugin.getDataFolder().getParentFile().getAbsolutePath(), "Hunter-" + onlineVersion + ".jar.temp");
        try {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://github.com/GeorgeV220/Hunter/releases/download/" + onlineVersion + "/Hunter-" + onlineVersion.replace("v", "") + ".jar").openConnection();
            ReadableByteChannel rbc = Channels.newChannel(httpsURLConnection.getInputStream());
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
            rbc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File jarFile = new File(hunterPlugin.getDataFolder().getParentFile().getAbsolutePath(), "Hunter-" + onlineVersion + ".jar");
        boolean rename = tempFile.renameTo(jarFile);
        if (rename) {
            try {
                if (player == null)
                    BukkitMinecraftUtils.debug(hunterPlugin,
                            "Version " + onlineVersion + " successfully downloaded!",
                            OptionsUtil.UPDATER_RESTART.getBooleanValue() ? "Server will automatically restart\n" +
                                    "Please keep in mind if you don't have a start.(sh/bat) the server will not automatically start"
                                    : "In order for the changes to take effect, please restart the server");
                else
                    BukkitMinecraftUtils.msg(player, "&e&lUpdater &8» &6Version &c" + onlineVersion + " &6successfully downloaded!", OptionsUtil.UPDATER_RESTART.getBooleanValue() ? "&e&lUpdater &8» &6Server will automatically restart\n" +
                            "&e&lUpdater &8» &6Please keep in mind if you don't have a start.(sh/bat) the server will not automatically start"
                            : "&e&lUpdater &8» &6In order for the changes to take effect, please restart the server");
                Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
                getFileMethod.setAccessible(true);
                File file = (File) getFileMethod.invoke(hunterPlugin);
                file.deleteOnExit();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (OptionsUtil.UPDATER_RESTART.getBooleanValue())
                            Bukkit.spigot().restart();
                    }
                }.runTaskLater(hunterPlugin, 20 * 5L);

            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}