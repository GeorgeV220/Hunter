package com.georgev22.killstreak.utilities;

import com.georgev22.api.colors.Color;
import com.georgev22.api.externals.xseries.XMaterial;
import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.killstreak.Main;
import com.google.common.collect.Lists;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.georgev22.api.utilities.Utils.Assertions.notNull;

public enum OptionsUtil {

    COMMANDS_KILLSTREAK("command.killstreak", true),

    COMMANDS_KILLSTREAK_MAIN("command.killstreak main", true),

    COMMANDS_LEVEL("command.level", true),

    DATABASE_HOST("database.SQL.host", "localhost"),

    DATABASE_PORT("database.SQL.port", 3306),

    DATABASE_USER("database.SQL.user", "youruser"),

    DATABASE_PASSWORD("database.SQL.password", "yourpassword"),

    DATABASE_DATABASE("database.SQL.database", "Killstreak"),

    DATABASE_TABLE_NAME("database.SQL.table name", "killstreak_users"),

    DATABASE_SQLITE("database.SQLite.file name", "killstreak"),

    DATABASE_MONGO_HOST("database.MongoDB.host", "localhost"),

    DATABASE_MONGO_PORT("database.MongoDB.port", 27017),

    DATABASE_MONGO_USER("database.MongoDB.user", "youruser"),

    DATABASE_MONGO_PASSWORD("database.MongoDB.password", "yourpassword"),

    DATABASE_MONGO_DATABASE("database.MongoDB.database", "KillStreak"),

    DATABASE_MONGO_COLLECTION("database.MongoDB.collection", "killstreak_users"),

    DATABASE_TYPE("database.type", "File"),

    EXPERIMENTAL_FEATURES("experimental features", false),

    UPDATER("updater", true),

    MESSAGE_LEVEL_UP_RECEIVER("message.level up.receiver", "all"),

    MESSAGE_LEVEL_UP("message.level up.enabled", true),

    MESSAGE_LEVEL_UP_EVERY("message.level up.every", 5),

    MESSAGE_KILLSTREAK_RECEIVER("message.killstreak.receiver", "all"),

    MESSAGE_KILLSTREAK("message.killstreak.enabled", true),

    MESSAGE_KILLSTREAK_EVERY("message.killstreak.every", 10),

    TITLE_LEVEL_UP_RECEIVER("title.level up.receiver", "player"),

    TITLE_LEVEL_UP("title.level up.enabled", true),

    TITLE_LEVEL_UP_EVERY("title.level up.every", 5),

    TITLE_LEVEL_UP_FADE_IN("title.level up.fade in", 10),

    TITLE_LEVEL_UP_STAY("title.level up.stay", 20),

    TITLE_LEVEL_UP_FADE_OUT("title.level up.fade out", 10),

    REWARDS_LEVEL_UP("rewards.level up", true),

    REWARDS_KILLSTREAK("rewards.killstreak", true),

    REWARDS_KILLS("rewards.kills.enabled", true),

    REWARDS_KILLS_CLOSEST("rewards.kills.closest", true),

    DISCORD_LEVEL_UP("discord.level up", false),

    DISCORD_KILL("discord.kill", false),

    DISCORD_KILL_STREAK("discord.killstreak", false),

    TOP_PLAYERS_LEVEL("top.levels", 5),

    TOP_PLAYERS_KILLS("top.kills", 5),

    TOP_PLAYERS_KILLSTREAK("top.killstreak", 5),


    ;
    private final String pathName;
    private final Object value;
    private static final Main mainPlugin = Main.getInstance();

    OptionsUtil(final String pathName, final Object value) {
        this.pathName = pathName;
        this.value = value;
    }

    public boolean getBooleanValue() {
        return mainPlugin.getConfig().getBoolean(getPath(), true);
    }

    public Object getObjectValue() {
        return mainPlugin.getConfig().get(getPath(), getDefaultValue());
    }

    public String getStringValue() {
        return mainPlugin.getConfig().getString(getPath(), String.valueOf(getDefaultValue()));
    }

    public @NotNull Long getLongValue() {
        return mainPlugin.getConfig().getLong(getPath(), (Long) getDefaultValue());
    }

    public @NotNull Integer getIntValue() {
        return mainPlugin.getConfig().getInt(getPath(), (Integer) getDefaultValue());
    }

    public @NotNull List<String> getStringList() {
        return mainPlugin.getConfig().getStringList(getPath());
    }

    public ItemStack getItemStack(boolean isSavedAsItemStack) {
        if (isSavedAsItemStack) {
            return mainPlugin.getConfig().getItemStack(getPath(), (ItemStack) getDefaultValue());
        } else {
            if (mainPlugin.getConfig().get(getPath()) == null) {
                return (ItemStack) getDefaultValue();
            }
            ItemBuilder itemBuilder = new ItemBuilder(
                    notNull("Material", XMaterial.valueOf(mainPlugin.getConfig().getString(getPath() + ".item")).parseMaterial()))
                    .amount(mainPlugin.getConfig().getInt(getPath() + ".amount"))
                    .title(mainPlugin.getConfig().getString(getPath() + ".title"))
                    .lores(mainPlugin.getConfig().getStringList(getPath() + ".lores"))
                    .showAllAttributes(
                            mainPlugin.getConfig().getBoolean(getPath() + ".show all attributes"))
                    .glow(mainPlugin.getConfig().getBoolean(getPath() + ".glow"));
            return itemBuilder.build();
        }
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    @Contract(pure = true)
    public @NotNull String getPath() {
        return "Options." + this.pathName;
    }

    /**
     * Returns the default value if the path have no value.
     *
     * @return the default value if the path have no value.
     */
    public Object getDefaultValue() {
        return value;
    }

    public @NotNull List<Color> getColors() {
        List<Color> colors = Lists.newArrayList();
        for (String stringColor : getStringList()) {
            colors.add(Color.from(stringColor));
        }

        return colors;
    }
}
