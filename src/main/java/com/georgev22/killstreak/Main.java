package com.georgev22.killstreak;

import com.georgev22.api.database.Database;
import com.georgev22.api.database.mongo.MongoDB;
import com.georgev22.api.database.sql.mysql.MySQL;
import com.georgev22.api.database.sql.postgresql.PostgreSQL;
import com.georgev22.api.database.sql.sqlite.SQLite;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.maven.LibraryLoader;
import com.georgev22.api.maven.MavenLibrary;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.killstreak.commands.KillstreakCommand;
import com.georgev22.killstreak.commands.LevelCommand;
import com.georgev22.killstreak.hooks.PAPI;
import com.georgev22.killstreak.listeners.DeveloperInformListener;
import com.georgev22.killstreak.listeners.PlayerListeners;
import com.georgev22.killstreak.utilities.MessagesUtil;
import com.georgev22.killstreak.utilities.OptionsUtil;
import com.georgev22.killstreak.utilities.Updater;
import com.georgev22.killstreak.utilities.configmanager.FileManager;
import com.georgev22.killstreak.utilities.interfaces.Callback;
import com.georgev22.killstreak.utilities.interfaces.IDatabaseType;
import com.georgev22.killstreak.utilities.player.UserData;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

@MavenLibrary(groupId = "org.mongodb", artifactId = "mongo-java-driver", version = "3.12.7")
@MavenLibrary(groupId = "mysql", artifactId = "mysql-connector-java", version = "8.0.22")
@MavenLibrary(groupId = "org.xerial", artifactId = "sqlite-jdbc", version = "3.34.0")
@MavenLibrary(groupId = "com.google.guava", artifactId = "guava", version = "30.1.1-jre")
@MavenLibrary(groupId = "org.postgresql", artifactId = "postgresql", version = "42.2.18")
@MavenLibrary(groupId = "commons-codec", artifactId = "commons-codec", version = "1.11")
public final class Main extends JavaPlugin {

    private static Main instance = null;

    private Database database = null;
    private IDatabaseType iDatabaseType = null;
    private Connection connection = null;
    private MongoDB mongoDB = null;
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

        MinecraftUtils.registerCommand("killstreak", new KillstreakCommand());
        MinecraftUtils.registerCommand("level", new LevelCommand());

        if (OptionsUtil.UPDATER.getBooleanValue()) {
            new Updater();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholdersAPI = new PAPI();
            if (placeholdersAPI.register())
                MinecraftUtils.debug(this, "[KillStreak] Hooked into PlaceholderAPI!");
        }

        Metrics metrics = new Metrics(this, 0);
        if (YamlConfiguration.loadConfiguration(new File(new File(this.getDataFolder().getParentFile(), "bStats"), "config.yml")).getBoolean("enabled", true)) {
            Bukkit.getLogger().info("[KillStreak] Metrics are enabled!");
        }

        if (MinecraftUtils.MinecraftVersion.getCurrentVersion().isBelow(MinecraftUtils.MinecraftVersion.V1_12_R1)) {
            MinecraftUtils.debug(this, "This version of Minecraft is extremely outdated and support for it has reached its end of life. You will still be able to run KillStreak on this Minecraft version(" + MinecraftUtils.MinecraftVersion.getCurrentVersion().name().toLowerCase() + "). Please consider updating to give your players a better experience and to avoid issues that have long been fixed.");
        }
    }

    @Override
    public void onDisable() {
        MinecraftUtils.unRegisterCommand("killstreak");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if (placeholdersAPI.isRegistered()) {
                if (placeholdersAPI.unregister()) {
                    MinecraftUtils.debug(this, "[KillStreak] Unhooked from PlaceholderAPI!");
                }
            }
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            UserData userData = UserData.getUser(player.getUniqueId());
            userData.save(false, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        });
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (mongoDB != null) {
            mongoDB.getMongoClient().close();
        }
    }

    /**
     * Setup database Values: File, MySQL, SQLite
     *
     * @throws SQLException           When something goes wrong
     * @throws ClassNotFoundException When class is not found
     */
    private void setupDatabase() throws Exception {
        ObjectMap<String, ObjectMap.Pair<String, String>> map = ObjectMap.newHashObjectMap()
                .append("uuid", ObjectMap.Pair.create("VARCHAR(38)", "NULL"))
                .append("name", ObjectMap.Pair.create("VARCHAR(18)", "NULL"))
                .append("kills", ObjectMap.Pair.create("INT(10)", "0"))
                .append("killstreak", ObjectMap.Pair.create("INT(10)", "0"))
                .append("level", ObjectMap.Pair.create("INT(10)", "0"))
                .append("experience", ObjectMap.Pair.create("INT(10)", "0"))
                .append("multiplier", ObjectMap.Pair.create("VARCHAR(5)", "0"));
        switch (OptionsUtil.DATABASE_TYPE.getStringValue()) {
            case "MySQL": {
                if (connection == null || connection.isClosed()) {
                    database = new MySQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: MySQL");
                }
                break;
            }
            case "PostgreSQL": {
                if (connection == null || connection.isClosed()) {
                    database = new PostgreSQL(
                            OptionsUtil.DATABASE_HOST.getStringValue(),
                            OptionsUtil.DATABASE_PORT.getIntValue(),
                            OptionsUtil.DATABASE_DATABASE.getStringValue(),
                            OptionsUtil.DATABASE_USER.getStringValue(),
                            OptionsUtil.DATABASE_PASSWORD.getStringValue());
                    iDatabaseType = new UserData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: PostgreSQL");
                }
                break;
            }
            case "SQLite": {
                if (connection == null || connection.isClosed()) {
                    database = new SQLite(
                            getDataFolder(),
                            OptionsUtil.DATABASE_SQLITE.getStringValue());
                    iDatabaseType = new UserData.SQLUserUtils();
                    connection = database.openConnection();
                    database.createTable(OptionsUtil.DATABASE_TABLE_NAME.getStringValue(), map);
                    MinecraftUtils.debug(this, "Database: SQLite");
                }
                break;
            }
            case "MongoDB": {
                mongoDB = new MongoDB(
                        OptionsUtil.DATABASE_MONGO_HOST.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PORT.getIntValue(),
                        OptionsUtil.DATABASE_MONGO_USER.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_PASSWORD.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue(),
                        OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue());
                database = null;
                iDatabaseType = new UserData.MongoDBUtils();
                MinecraftUtils.debug(this, "Database: MongoDB");
                break;
            }
            case "File": {
                database = null;
                iDatabaseType = new UserData.FileUserUtils();
                MinecraftUtils.debug(this, "Database: File");
                break;
            }
            default: {
                database = null;
                iDatabaseType = null;
                setEnabled(false);
                throw new RuntimeException("Please use one of the available databases\nAvailable databases: File, MySQL, SQLite, PostgreSQL and MongoDB");
            }
        }

        UserData.loadAllUsers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            UserData userData = UserData.getUser(player.getUniqueId());
            try {
                userData.load(new Callback() {
                    @Override
                    public void onSuccess() {
                        UserData.getAllUsersMap().append(userData.user().uniqueId(), userData.user());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * Get Database open connection
     *
     * @return connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get Database class instance
     *
     * @return {@link Database} class instance
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Return Database Type
     *
     * @return Database Type
     */
    public IDatabaseType getIDatabaseType() {
        return iDatabaseType;
    }

    /**
     * Return MongoDB instance when MongoDB is in use.
     * <p>
     * Returns null if MongoDB is not in use
     *
     * @return {@link MongoDB} instance
     */
    public MongoDB getMongoDB() {
        return mongoDB;
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return FileManager.getInstance().getConfig().getFileConfiguration();
    }

    @Override
    public void saveConfig() {
        FileManager.getInstance().getConfig().saveFile();
    }
}
