package com.georgev22.hunter;

import com.georgev22.api.database.Database;
import com.georgev22.api.database.DatabaseType;
import com.georgev22.api.database.DatabaseWrapper;
import com.georgev22.api.database.mongo.MongoDB;
import com.georgev22.api.maps.HashObjectMap;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.maven.LibraryLoader;
import com.georgev22.api.maven.MavenLibrary;
import com.georgev22.api.minecraft.MinecraftUtils;
import com.georgev22.api.minecraft.configmanager.CFG;
import com.georgev22.api.minecraft.inventory.PagedInventoryAPI;
import com.georgev22.hunter.commands.*;
import com.georgev22.hunter.hooks.HolographicDisplays;
import com.georgev22.hunter.hooks.PAPI;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.listeners.DeveloperInformListener;
import com.georgev22.hunter.listeners.PlayerListeners;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.Updater;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.interfaces.IDatabaseType;
import com.georgev22.hunter.utilities.player.UserData;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoClient;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static com.georgev22.api.utilities.Utils.*;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.11")
public final class Main extends JavaPlugin {

    private static Main instance = null;

    @Getter
    private DatabaseWrapper databaseWrapper = null;

    /**
     * Return Database Type
     *
     * @return Database Type
     */
    @Getter
    private IDatabaseType iDatabaseType = null;

    /**
     * Get Database open connection
     *
     * @return connection
     */
    @Getter
    private Connection connection = null;

    /**
     * Return MongoDB instance when MongoDB is in use.
     * <p>
     * Returns null if MongoDB is not in use
     *
     * @return {@link MongoDB} instance
     */
    @Getter
    private @Nullable MongoClient mongoClient = null;

    private PagedInventoryAPI api = null;

    private PAPI placeholdersAPI = null;

    /**
     * Return the Main instance
     *
     * @return Main instance
     */
    public static Main getInstance() {
        return instance == null ? instance = Main.getPlugin(Main.class) : instance;
    }

    @Override
    public void onLoad() {
        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_16_R1))
            new LibraryLoader(this.getClass(), this.getDataFolder()).loadAll();
    }

    @Override
    public void onEnable() {
        final FileManager fm = FileManager.getInstance();
        fm.loadFiles(this);
        MessagesUtil.repairPaths(fm.getMessages());
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        if (data.get("cooldowns") != null)
            Cooldown.appendToCooldowns(deserialize(data.getString("cooldowns", ""), new TypeToken<ObjectMap<String, Cooldown>>() {
            }.getType()));
        api = new PagedInventoryAPI(this);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                setupDatabase();
            } catch (Exception throwable) {
                throwable.printStackTrace();
            }
        });

        MinecraftUtils.registerListeners(this,
                new PlayerListeners(),
                new DeveloperInformListener());

        if (OptionsUtil.COMMAND_KILLSTREAK.getBooleanValue())
            MinecraftUtils.registerCommand("killstreak", new KillstreakCommand());
        if (OptionsUtil.COMMAND_LEVEL.getBooleanValue())
            MinecraftUtils.registerCommand("level", new LevelCommand());
        if (OptionsUtil.COMMAND_HUNTER.getBooleanValue())
            MinecraftUtils.registerCommand("hunter", new HunterCommand());
        if (OptionsUtil.COMMAND_PRESTIGE.getBooleanValue())
            MinecraftUtils.registerCommand("prestige", new PrestigeCommand());
        if (OptionsUtil.COMMAND_BOUNTY.getBooleanValue())
            MinecraftUtils.registerCommand("bounty", new BountyCommand());

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            //new Updater();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholdersAPI = new PAPI();
            if (placeholdersAPI.register())
                MinecraftUtils.debug(this, "[Hunter] Hooked into PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            if (data.get("Holograms") != null) {
                data.getConfigurationSection("Holograms").getKeys(false)
                        .forEach(s -> HolographicDisplays.create(s, (Location) data.get("Holograms." + s + ".location"),
                                data.getString("Holograms." + s + ".type"), false));
            }
            Bukkit.getLogger().info("[Hunter] Hooked into HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            if (Vault.hook()) {
                Bukkit.getLogger().info("[Hunter] Hooked into Vault!");
            } else {
                Bukkit.getLogger().warning("[Hunter] Please install an economy plugin. Example: EssentialsX!");
            }
        }

        Metrics metrics = new Metrics(this, 0);
        if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
            Bukkit.getLogger().info("[Hunter] Metrics are enabled!");
        }

        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
            MinecraftUtils.debug(this, "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run Hunter on this Minecraft version(" + MinecraftUtils.MinecraftVersion.getCurrentVersion().name().toLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }
    }

    @Override
    public void onDisable() {
        if (OptionsUtil.COMMAND_KILLSTREAK.getBooleanValue())
            MinecraftUtils.unRegisterCommand("killstreak");
        if (OptionsUtil.COMMAND_LEVEL.getBooleanValue())
            MinecraftUtils.unRegisterCommand("level");
        if (OptionsUtil.COMMAND_HUNTER.getBooleanValue())
            MinecraftUtils.unRegisterCommand("hunter");
        if (OptionsUtil.COMMAND_PRESTIGE.getBooleanValue())
            MinecraftUtils.unRegisterCommand("prestige");
        if (OptionsUtil.COMMAND_BOUNTY.getBooleanValue())
            MinecraftUtils.unRegisterCommand("bounty");
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserData userData = UserData.getUser(player.getUniqueId());
            userData.save(false, new Callback<Boolean>() {
                @Override
                public Boolean onSuccess() {
                    return true;
                }

                @Override
                public Boolean onFailure() {
                    return false;
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    return onFailure();
                }
            });
        });
        final FileManager fm = FileManager.getInstance();
        CFG dataCFG = fm.getData();
        FileConfiguration data = dataCFG.getFileConfiguration();
        if (!Cooldown.getCooldowns().isEmpty())
            data.set("cooldowns", serialize(Cooldown.getCooldowns()));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    MinecraftUtils.debug(this, "[Hunter] Unhooked from PlaceholderAPI!");
                }
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            HolographicDisplays.getHologramMap().forEach((name, hologram) -> HolographicDisplays.remove(name, false));
            MinecraftUtils.debug(this, "[Hunter] Unhooked from HolographicDisplays");
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        ObjectMap<String, ObjectMap.Pair<String, String>> map = new HashObjectMap<String, ObjectMap.Pair<String, String>>()
                .append("uuid", ObjectMap.Pair.create("VARCHAR(38)", "NULL"))
                .append("name", ObjectMap.Pair.create("VARCHAR(18)", "NULL"))
                .append("kills", ObjectMap.Pair.create("INT(10)", "0"))
                .append("killstreak", ObjectMap.Pair.create("INT(10)", "0"))
                .append("level", ObjectMap.Pair.create("INT(10)", "0"))
                .append("experience", ObjectMap.Pair.create("INT(10)", "0"))
                .append("multiplier", ObjectMap.Pair.create("VARCHAR(5)", "0"));
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.MYSQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: MySQL");
                }
            }
            case "PostgreSQL" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.POSTGRESQL,
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: PostgreSQL");
                }
            }
            case "SQLite" -> {
                if (connection == null || connection.isClosed()) {
                    databaseWrapper = new DatabaseWrapper(DatabaseType.SQLITE, getDataFolder().getAbsolutePath(), OptionsUtil.DATABASE_SQLITE.getStringValue());
                    connection = databaseWrapper.connect().getSQLConnection();
                    databaseWrapper.getSQLDatabase().createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    iDatabaseType = new UserData.SQLUserUtils();
                    MinecraftUtils.debug(this, "Database: SQLite");
                }
            }
            case "MongoDB" -> {
                databaseWrapper = new DatabaseWrapper(DatabaseType.MONGO,
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue());
                iDatabaseType = new UserData.MongoDBUtils();
                mongoClient = databaseWrapper.connect().getMongoClient();
                MinecraftUtils.debug(this, "Database: MongoDB");
            }
            case "File" -> {
                databaseWrapper = null;
                iDatabaseType = new UserData.FileUserUtils();
                MinecraftUtils.debug(this, "Database: File");
            }
            default -> {
                setEnabled(false);
                throw new RuntimeException("Please use one of the available databases\nAvailable databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
            }
        }

        UserData.loadAllUsers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UserData userData = UserData.getUser(player.getUniqueId());
            try {
                userData.load(new Callback<>() {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            HolographicDisplays.updateAll();
        }

    }

    /**
     * Get Database class instance
     *
     * @return {@link Database} class instance
     */
    public Database getDatabase() {
        return databaseWrapper.getSQLDatabase();
    }

    public PagedInventoryAPI getInventoryAPI() {
        return api;
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    @Override
    public void saveConfig() {
        FileManager.getInstance().getConfig().saveFile();
    }

    @Override
    public void reloadConfig() {
        FileManager fm = FileManager.getInstance();
        fm.getConfig().reloadFile();
        fm.getMessages().reloadFile();
        MessagesUtil.repairPaths(fm.getMessages());
        fm.getItems().reloadFile();
        fm.getDiscord().reloadFile();
    }
}
