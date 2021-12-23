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
import java.util.Optional;

import static com.georgev22.api.utilities.Utils.Assertions.notNull;

public enum OptionsUtil {

    DEBUG("debug", false, Optional.empty()),

    COMMAND_KILLSTREAK("command.killstreak", true, Optional.empty()),

    COMMAND_KILLSTREAK_MAIN("command.killstreak main", true, Optional.empty()),

    COMMAND_PRESTIGE("command.prestige", true, Optional.empty()),

    COMMAND_LEVEL("command.level", true, Optional.empty()),

    DATABASE_HOST("database.SQL.host", "localhost", Optional.empty()),

    DATABASE_PORT("database.SQL.port", 3306, Optional.empty()),

    DATABASE_USER("database.SQL.user", "youruser", Optional.empty()),

    DATABASE_PASSWORD("database.SQL.password", "yourpassword", Optional.empty()),

    DATABASE_DATABASE("database.SQL.database", "Killstreak", Optional.empty()),

    DATABASE_TABLE_NAME("database.SQL.table name", "killstreak_users", Optional.empty()),

    DATABASE_SQLITE("database.SQLite.file name", "killstreak", Optional.empty()),

    DATABASE_MONGO_HOST("database.MongoDB.host", "localhost", Optional.empty()),

    DATABASE_MONGO_PORT("database.MongoDB.port", 27017, Optional.empty()),

    DATABASE_MONGO_USER("database.MongoDB.user", "youruser", Optional.empty()),

    DATABASE_MONGO_PASSWORD("database.MongoDB.password", "yourpassword", Optional.empty()),

    DATABASE_MONGO_DATABASE("database.MongoDB.database", "KillStreak", Optional.empty()),

    DATABASE_MONGO_COLLECTION("database.MongoDB.collection", "killstreak_users", Optional.empty()),

    DATABASE_TYPE("database.type", "File", Optional.empty()),

    EXPERIMENTAL_FEATURES("experimental features", false, Optional.empty()),

    UPDATER("updater", true, Optional.empty()),

    LEVELS_MESSAGE_RECEIVER("levels.message.receiver", "all", Optional.of("message.level up.receiver")),

    LEVELS_MESSAGE("levels.message.enabled", true, Optional.of("message.level up.enabled")),

    LEVELS_MESSAGE__EVERY("levels.message.every", 5, Optional.of("message.level up.every")),

    KILLSTREAK_MESSAGE_RECEIVER("killstreak.message.receiver", "all", Optional.of("message.killstreak.receiver")),

    KILLSTREAK_MESSAGE("killstreak.message.enabled", true, Optional.of("message.killstreak.enabled")),

    KILLSTREAK_MESSAGE_EVERY("killstreak.message.every", 10, Optional.of("message.killstreak.every")),

    LEVELS_TITLE_RECEIVER("levels.title.receiver", "player", Optional.of("title.level up.receiver")),

    LEVELS_TITLE("levels.title.enabled", true, Optional.of("title.level up.enabled")),

    LEVELS_TITLE_EVERY("levels.title.every", 5, Optional.of("title.level up.every")),

    LEVELS_TITLE_FADE_IN("levels.title.fade in", 10, Optional.of("title.level up.fade in")),

    LEVELS_TITLE__STAY("levels.title.stay", 20, Optional.of("title.level up.stay")),

    LEVELS_TITLE_FADE_OUT("levels.title.fade out", 10, Optional.of("title.level up.fade out")),

    LEVELS_REWARDS("levels.rewards", true, Optional.of("rewards.level up")),

    KILLSTREAK_REWARDS("killstreak.rewards", true, Optional.of("rewards.killstreak")),

    KILLS_REWARDS("kills.rewards", true, Optional.of("rewards.kills.enabled")),

    KILLS_REWARDS_CLOSEST("kills.closest", true, Optional.of("rewards.kills.closest")),

    LEVELS_DISCORD("levels.discord", false, Optional.of("discord.level up")),

    KILLS_DISCORD("kills.discord", false, Optional.of("discord.kills")),

    KILLSTREAK_DISCORD("killstreak.discord", false, Optional.of("discord.killstreak")),

    LEVELS_TOP("levels.top", 5, Optional.of("top.levels")),

    KILLS_TOP("kills.top", 5, Optional.of("top.kills")),

    KILLSTREAK_TOP("killstreak.top", 5, Optional.of("top.killstreak")),


    ;
    private final String pathName;
    private final Object value;
    private final Optional<String>[] oldPaths;
    private static final Main mainPlugin = Main.getInstance();

    @SafeVarargs
    @Contract(pure = true)
    OptionsUtil(final String pathName, final Object value, Optional<String>... oldPaths) {
        this.pathName = pathName;
        this.value = value;
        this.oldPaths = oldPaths;
    }

    public boolean getBooleanValue() {
        return mainPlugin.getConfig().getBoolean(getPath(), true);
    }

    public Object getObjectValue() {
        return mainPlugin.getConfig().get(getPath(), getDefaultValue());
    }

    public String getStringValue() {
        return mainPlugin.getConfig().getString(getPath(), (String) getDefaultValue());
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
     * Converts and return a String List of color codes to a List of Color classes that represent the colors.
     *
     * @return a List of Color classes that represent the colors.
     */
    public @NotNull List<Color> getColors() {
        List<Color> colors = Lists.newArrayList();
        for (String stringColor : getStringList()) {
            colors.add(Color.from(stringColor));
        }

        return colors;
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    public @NotNull String getPath() {
        for (Optional<String> paths : getOldPaths()) {
            if (mainPlugin.getConfig().get("Options." + getDefaultPath()) == null) {
                if (paths.isPresent()) {
                    if (mainPlugin.getConfig().get("Options." + paths.get()) != null) {
                        return "Options." + paths.get();
                    }
                }
            } else {
                return "Options." + getDefaultPath();
            }
        }
        return null;
    }

    /**
     * Returns the default path.
     *
     * @return the default path.
     */
    @Contract(pure = true)
    public @NotNull String getDefaultPath() {
        return this.pathName;
    }

    /**
     * Returns the old path if it exists.
     *
     * @return the old path if it exists.
     */
    public Optional<String>[] getOldPaths() {
        return oldPaths;
    }

    /**
     * Returns the default value if the path have no value.
     *
     * @return the default value if the path have no value.
     */
    public Object getDefaultValue() {
        return value;
    }
}
