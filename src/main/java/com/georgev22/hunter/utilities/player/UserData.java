package com.georgev22.hunter.utilities.player;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.HashObjectMap;
import com.georgev22.library.maps.LinkedObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.utilities.Utils.Callback;
import com.georgev22.hunter.HunterPlugin;
import com.georgev22.hunter.utilities.OptionsUtil;
import com.georgev22.hunter.utilities.interfaces.IDatabaseType;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to handle all user's data and anything related to them.
 */
public record UserData(User user) {
    private static final HunterPlugin hunterPlugin = HunterPlugin.getInstance();

    private static final ObjectMap<UUID, User> allUsersMap = new ConcurrentObjectMap<>();

    /**
     * Returns all the players in a map
     *
     * @return all the players
     */
    public static ObjectMap<UUID, User> getAllUsersMap() {
        return allUsersMap;
    }

    public static @NotNull ObjectMap<String, User> getAllUsersMapWithName() {
        ObjectMap<String, User> objectMap = new HashObjectMap<>();
        for (Map.Entry<UUID, User> entry : allUsersMap.entrySet()) {
            objectMap.append(entry.getValue().name(), entry.getValue());
        }
        return objectMap;
    }

    /**
     * Load all users
     *
     * @throws Exception When something goes wrong
     */
    public static void loadAllUsers() throws Exception {
        allUsersMap.putAll(hunterPlugin.getIDatabaseType().getAllUsers());
    }

    /**
     * Returns a copy of this UserData class for a specific user.
     *
     * @param offlinePlayer Offline player object
     * @return a copy of this UserData class for a specific user.
     * @since v4.7.0
     */
    @Contract("_ -> new")
    public static @NotNull UserData getUser(@NotNull OfflinePlayer offlinePlayer) {
        return getUser(offlinePlayer.getUniqueId());
    }

    /**
     * Returns a copy of this UserData class for a specific user.
     *
     * @param uuid Player's Unique identifier
     * @return a copy of this UserData class for a specific user.
     */
    @Contract("_ -> new")
    public static @NotNull UserData getUser(UUID uuid) {
        if (!allUsersMap.containsKey(uuid)) {
            allUsersMap.append(uuid, new User(uuid));
        }
        return new UserData(allUsersMap.get(uuid));
    }

    /**
     * Set player name
     */
    public UserData setName(String name) {
        user.append("name", name);
        return this;
    }

    public UserData setKills(int kills) {
        user.append("kills", kills);
        return this;
    }

    public UserData setKillstreak(int killstreak) {
        user.append("killstreak", killstreak);
        return this;
    }

    public UserData setLevel(int level) {
        user.append("level", level);
        return this;
    }

    public UserData setMultiplier(double multiplier) {
        user.append("multiplier", multiplier);
        return this;
    }

    public UserData setExperience(double experience) {
        user.append("experience", experience);
        return this;
    }

    public UserData setPrestige(int prestige) {
        user.append("prestige", prestige);
        return this;
    }

    public UserData setBounty(double bounty) {
        user.append("bounty", bounty);
        return this;
    }

    public int getKills() {
        return user.totalKills();
    }

    public int getKillStreak() {
        return user.killstreak();
    }

    public int getLevel() {
        return user.level();
    }

    public double getMultiplier() {
        return user.multiplier();
    }

    public double getExperience() {
        return user.experience();
    }

    public int getPrestige() {
        return user.prestige();
    }

    public double getBounty() {
        return user.bounty();
    }

    public boolean haveBounty() {
        return user.bounty() > 0;
    }

    /**
     * Check if the user exists
     *
     * @return true if user exists or false when is not
     */
    public boolean playerExists() {
        return getAllUsersMap().containsKey(user.uniqueId());
    }

    /**
     * Load user data
     *
     * @param callback Callback
     * @throws Exception When something goes wrong
     */
    public UserData load(Callback<Boolean> callback) throws Exception {
        hunterPlugin.getIDatabaseType().load(user, callback);
        return this;
    }

    /**
     * Save all user's data
     *
     * @param async True if you want to save async
     */
    public UserData save(boolean async, Callback<Boolean> callback) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(hunterPlugin, () -> {
                try {
                    hunterPlugin.getIDatabaseType().save(user);
                    callback.onSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e.getCause());
                }
            });
        } else {
            try {
                hunterPlugin.getIDatabaseType().save(user);
                callback.onSuccess();
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e.getCause());
            }
        }
        return this;
    }

    /**
     * Reset user's stats
     */
    public UserData reset() {
        Bukkit.getScheduler().runTaskAsynchronously(hunterPlugin, () -> {
            try {
                hunterPlugin.getIDatabaseType().reset(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    /**
     * Delete user from database
     */
    public UserData delete() {
        Bukkit.getScheduler().runTaskAsynchronously(hunterPlugin, () -> {
            try {
                hunterPlugin.getIDatabaseType().delete(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return this;
    }

    /**
     * @param limit number of top players by level in a Map.
     * @return a {@link LinkedObjectMap} with {@param limit} top players.
     */
    public static LinkedObjectMap<String, Integer> getTopPlayersByLevels(int limit) {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : UserData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("level"));
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @param limit number of top players by kills in a Map.
     * @return a {@link LinkedObjectMap} with {@param limit} top players.
     */
    public static LinkedObjectMap<String, Integer> getTopPlayersByKills(int limit) {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : UserData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("kills"));
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * @param limit number of top players by killstreak in a Map.
     * @return a {@link LinkedObjectMap} with {@param limit} top players.
     */
    public static LinkedObjectMap<String, Integer> getTopPlayersByKillstreak(int limit) {
        ObjectMap<String, Integer> objectMap = new LinkedObjectMap<>();

        for (Map.Entry<UUID, User> entry : UserData.getAllUsersMap().entrySet()) {
            objectMap.append(entry.getValue().getString("name"), entry.getValue().getInteger("killstreak"));
        }

        return objectMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(limit).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedObjectMap::new));
    }

    /**
     * All SQL Utils for the user
     * Everything here must run asynchronously
     */
    public static class SQLUserUtils implements IDatabaseType {

        /**
         * Save all user's data
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class isn't found
         */
        public void save(@NotNull User user) throws SQLException, ClassNotFoundException {
            hunterPlugin.getDatabase().updateSQL(
                    "UPDATE `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` " +
                            "SET " +
                            "`name` = '" + user.name() + "', " +
                            "`experience` = '" + user.experience() + "', " +
                            "`killstreak` = '" + user.killstreak() + "', " +
                            "`multiplier` = '" + user.multiplier() + "', " +
                            "`kills` = '" + user.totalKills() + "', " +
                            "`level` = '" + user.level() + "', " +
                            "`prestige` = '" + user.prestige() + "', " +
                            "`bounty` = '" + user.bounty() + "' " +
                            "WHERE `uuid` = '" + user.uniqueId() + "'");
        }

        /**
         * Remove user's data from database.
         *
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When class isn't found
         */
        public void delete(@NotNull User user) throws SQLException, ClassNotFoundException {
            hunterPlugin.getDatabase().updateSQL("DELETE FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.uniqueId().toString() + "';");
            BukkitMinecraftUtils.debug(hunterPlugin, "User " + user.name() + " deleted from the database!");
            allUsersMap.remove(user.uniqueId());
        }

        /**
         * Load all user's data
         *
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    try {
                        ResultSet resultSet = hunterPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` WHERE `uuid` = '" + user.uniqueId().toString() + "'");
                        while (resultSet.next()) {
                            user
                                    .append("name", resultSet.getString("name"))
                                    .append("experience", resultSet.getDouble("experience"))
                                    .append("killstreak", resultSet.getInt("killstreak"))
                                    .append("level", resultSet.getInt("level"))
                                    .append("kills", resultSet.getInt("kills"))
                                    .append("multiplier", resultSet.getDouble("multiplier"))
                                    .append("prestige", resultSet.getInt("prestige"))
                                    .append("bounty", resultSet.getDouble("bounty"))
                            ;
                        }
                        return callback.onSuccess();
                    } catch (SQLException | ClassNotFoundException throwables) {
                        return callback.onFailure(throwables.getCause());
                    }
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    return callback.onFailure(throwable);
                }

                @Override
                public Boolean onFailure() {
                    return false;
                }
            });
        }

        /**
         * Check if the user exists
         *
         * @return true if a user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) throws SQLException, ClassNotFoundException {
            return hunterPlugin.getDatabase().querySQL("SELECT * FROM " + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + " WHERE `uuid` = '" + user.uniqueId().toString() + "'").next();
        }

        /**
         * Set up the user data to the database
         *
         * @param callback Callback
         */
        public void setupUser(User user, Callback<Boolean> callback) {
            try {
                if (!playerExists(user)) {
                    hunterPlugin.getDatabase().updateSQL(
                            "INSERT INTO `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "` (`uuid`, `name`, `experience`, `killstreak`, `level`, `kills` `multiplier`, `prestige`, `bounty`)" +
                                    " VALUES " +
                                    "('" + user.uniqueId().toString() + "', '" + user.offlinePlayer().getName() + "', '0.0', '0', '0', '0', '1.0', '0', '0');");
                }
                callback.onSuccess();
            } catch (SQLException | ClassNotFoundException throwables) {
                callback.onFailure(throwables.getCause());
            }
        }

        /**
         * Get all users from the database
         *
         * @return all the users from the database
         * @throws SQLException           When something goes wrong
         * @throws ClassNotFoundException When the class is not found
         */
        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = new ConcurrentObjectMap<>();
            ResultSet resultSet = hunterPlugin.getDatabase().querySQL("SELECT * FROM `" + OptionsUtil.DATABASE_TABLE_NAME.getStringValue() + "`");
            while (resultSet.next()) {
                UserData userData = UserData.getUser(UUID.fromString(resultSet.getString("uuid")));
                userData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        map.append(userData.user().uniqueId(), userData.user());
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
            }
            return map;
        }
    }

    /**
     * All Mongo Utils for the user
     * Everything here must run asynchronously
     */
    public static class MongoDBUtils implements IDatabaseType {

        /**
         * Save all user's data
         */
        public void save(@NotNull User user) {
            BasicDBObject query = new BasicDBObject();
            query.append("uuid", user.uniqueId().toString());

            BasicDBObject updateObject = new BasicDBObject();
            updateObject.append("$set", new BasicDBObject()
                    .append("uuid", user.uniqueId().toString())
                    .append("name", user.name())
                    .append("experience", user.experience())
                    .append("killstreak", user.killstreak())
                    .append("level", user.level())
                    .append("kills", user.totalKills())
                    .append("multiplier", user.multiplier())
                    .append("prestige", user.prestige())
                    .append("bounty", user.bounty())
            );

            hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).updateOne(query, updateObject);
        }

        /**
         * Load user data
         *
         * @param user     User
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    BasicDBObject searchQuery = new BasicDBObject();
                    searchQuery.append("uuid", user.uniqueId().toString());
                    FindIterable<Document> findIterable = hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).find(searchQuery);
                    Document document = findIterable.first();
                    user
                            .append("name", document.getString("name"))
                            .append("experience", document.getDouble("experience"))
                            .append("killstreak", document.getInteger("killstreak"))
                            .append("level", document.getInteger("level"))
                            .append("kills", document.getInteger("kills"))
                            .append("multiplier", document.getDouble("multiplier"))
                            .append("prestige", document.getInteger("prestige"))
                            .append("bounty", document.getDouble("bounty"))
                    ;
                    return callback.onSuccess();
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    return callback.onFailure(throwable.getCause());
                }

                @Override
                public Boolean onFailure() {
                    return callback.onFailure();
                }
            });
        }

        /**
         * Setup the user
         *
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(User user, Callback<Boolean> callback) {
            if (!playerExists(user)) {
                hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).insertOne(new Document()
                        .append("uuid", user.uniqueId().toString())
                        .append("name", user.offlinePlayer().getName())
                        .append("experience", 0D)
                        .append("killstreak", 0)
                        .append("level", 0)
                        .append("kills", 0)
                        .append("multiplier", 1.0D)
                        .append("prestige", 0)
                        .append("bounty", 0D)
                );
            }
            callback.onSuccess();
        }

        /**
         * Check if the user exists
         *
         * @return true if a user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) {
            long count = hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).countDocuments(new BsonDocument("uuid", new BsonString(user.uniqueId().toString())));
            return count > 0;
        }

        /**
         * Remove user's data from database.
         */
        public void delete(@NotNull User user) {
            BasicDBObject theQuery = new BasicDBObject();
            theQuery.put("uuid", user.uniqueId().toString());
            DeleteResult result = hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).deleteMany(theQuery);
            if (result.getDeletedCount() > 0) {
                allUsersMap.remove(user.uniqueId());
            }
        }

        /**
         * Get all users from the database
         *
         * @return all the users from the database
         */
        public ObjectMap<UUID, User> getAllUsers() {
            ObjectMap<UUID, User> map = new ConcurrentObjectMap<>();
            FindIterable<Document> iterable = hunterPlugin.getMongoClient().getDatabase(OptionsUtil.DATABASE_MONGO_DATABASE.getStringValue()).getCollection(OptionsUtil.DATABASE_MONGO_COLLECTION.getStringValue()).find();
            iterable.forEach((Block<Document>) document -> {
                UserData userData = UserData.getUser(UUID.fromString(document.getString("uuid")));
                try {
                    userData.load(new Callback<>() {
                        @Override
                        public Boolean onSuccess() {
                            map.append(userData.user().uniqueId(), userData.user());
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
            return map;
        }

    }


    /**
     * All User Utils for the user
     */

    public static class FileUserUtils implements IDatabaseType {

        /**
         * Save all user's data
         *
         * @param user User object
         */
        @Override
        public void save(@NotNull User user) {
            File file = new File(hunterPlugin.getDataFolder(),
                    "userdata" + File.separator + user.uniqueId().toString() + ".yml");
            if (!file.exists()) {
                setupUser(user, new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
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
            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
            yamlConfiguration.set("name", user.name());
            yamlConfiguration.set("experience", user.experience());
            yamlConfiguration.set("killstreak", user.killstreak());
            yamlConfiguration.set("level", user.level());
            yamlConfiguration.set("kills", user.totalKills());
            yamlConfiguration.set("multiplier", user.multiplier());
            yamlConfiguration.set("prestige", user.prestige());
            yamlConfiguration.set("bounty", user.bounty());
            try {
                yamlConfiguration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Load user data
         *
         * @param user     User object
         * @param callback Callback
         */
        public void load(User user, Callback<Boolean> callback) {
            setupUser(user, new Callback<>() {
                @Override
                public Boolean onSuccess() {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(new File(hunterPlugin.getDataFolder(),
                            "userdata" + File.separator + user.uniqueId().toString() + ".yml"));
                    user
                            .append("name", yamlConfiguration.getString("name"))
                            .append("kills", yamlConfiguration.getInt("kills"))
                            .append("killstreak", yamlConfiguration.getInt("killstreak"))
                            .append("level", yamlConfiguration.getInt("level"))
                            .append("experience", yamlConfiguration.getDouble("experience"))
                            .append("multiplier", yamlConfiguration.getDouble("multiplier"))
                            .append("prestige", yamlConfiguration.getInt("prestige"))
                            .append("bounty", yamlConfiguration.getDouble("bounty"))
                    ;
                    return callback.onSuccess();
                }

                @Override
                public Boolean onFailure(Throwable throwable) {
                    return callback.onFailure(throwable.getCause());
                }

                @Override
                public Boolean onFailure() {
                    return callback.onFailure();
                }
            });
        }

        /**
         * Setup the user
         *
         * @param user     User object
         * @param callback Callback
         */
        public void setupUser(@NotNull User user, Callback<Boolean> callback) {
            new File(hunterPlugin.getDataFolder(),
                    "userdata").mkdirs();
            File file = new File(hunterPlugin.getDataFolder(),
                    "userdata" + File.separator + user.uniqueId().toString() + ".yml");
            if (!playerExists(user)) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onFailure(e.getCause());
                }
                user
                        .append("name", user.offlinePlayer().getName())
                        .append("experience", 0D)
                        .append("killstreak", 0)
                        .append("level", 0)
                        .append("kills", 0)
                        .append("multiplier", 1.0D)
                        .append("prestige", 0)
                        .append("bounty", 0D)
                ;
                save(user);
            }
            callback.onSuccess();
        }

        /**
         * Remove user's data from file.
         */
        public void delete(@NotNull User user) {
            File file = new File(hunterPlugin.getDataFolder(),
                    "userdata" + File.separator + user.uniqueId().toString() + ".yml");
            if (file.exists() & file.delete()) {
                UserData.getAllUsersMap().remove(user.uniqueId());
            }
        }

        /**
         * Check if the user exists
         *
         * @return true if user exists or false when is not
         */
        public boolean playerExists(@NotNull User user) {
            return new File(hunterPlugin.getDataFolder(),
                    "userdata" + File.separator + user.uniqueId().toString() + ".yml").exists();
        }

        public ObjectMap<UUID, User> getAllUsers() throws Exception {
            ObjectMap<UUID, User> map = new LinkedObjectMap<>();

            File[] files = new File(hunterPlugin.getDataFolder(), "userdata").listFiles((dir, name) -> name.endsWith(".yml"));

            if (files == null) {
                return map;
            }

            for (File file : files) {
                UserData userData = UserData.getUser(UUID.fromString(file.getName().replace(".yml", "")));
                userData.load(new Callback<>() {
                    @Override
                    public Boolean onSuccess() {
                        map.append(userData.user().uniqueId(), userData.user());
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

            return map;
        }
    }
}
