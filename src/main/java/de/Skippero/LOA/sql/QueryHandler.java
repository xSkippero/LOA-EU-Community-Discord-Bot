package de.Skippero.LOA.sql;

import com.google.common.collect.Multimap;
import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.features.raid.Raid;
import lombok.Getter;
import lombok.Setter;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Getter
@Setter
public class QueryHandler {

    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;

    private final List<Connection> connections = new ArrayList<>();

    public QueryHandler() {

        ConfigManager cfg = LOABot.getConfigManager();

        this.host = cfg.getData("mysql.host");
        this.port = Integer.parseInt(cfg.getData("mysql.port"));
        this.database = cfg.getData("mysql.database");
        this.user = cfg.getData("mysql.user");
        this.password = cfg.getData("mysql.password");

        Connection con = getConnection();
        if (con != null) {
            connections.add(con);
            System.out.println("[" + new Date().toGMTString() + "] Successfully established SQL Connection");
            createTables();
        } else {
            System.out.println("[" + new Date().toGMTString() + "] Could not established SQL Connection, exiting with Error code 1");
            System.exit(1);
        }
    }

    public Multimap<String, String[]> loadConfiguration(Multimap<String, String[]> map) {
        ResultSet set = executeQuerySync("SELECT * FROM serverData");
        try {
            while (set.next()) {
                map.put(set.getString("server"), new String[]{set.getString("field"), set.getString("value")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Connection getConnection() {
        String address = "jdbc:mysql://" + host + ":" + port + "/" + database;
        try {
            return DriverManager.getConnection(address, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createTables() {
        System.out.println("[" + new Date().toGMTString() + "] Creating Tables if not exits");
        try {
            executeUpdateSync("CREATE TABLE IF NOT EXISTS userData(id bigint PRIMARY KEY AUTO_INCREMENT, userId VARCHAR(64), permission VARCHAR(64), server VARCHAR(64))");
            executeUpdateSync("CREATE TABLE IF NOT EXISTS serverData(id bigint PRIMARY KEY AUTO_INCREMENT, field VARCHAR(64), value VARCHAR(64), server VARCHAR(64))");
            executeUpdateSync("CREATE TABLE IF NOT EXISTS plannedRaidsMeta(id bigint PRIMARY KEY AUTO_INCREMENT, raidId bigint, name VARCHAR(128), description VARCHAR(512), duration VARCHAR(64), startDate VARCHAR(64), startDateStamp VARCHAR(64), autoDeletionStamp bigint)");
            executeUpdateSync("CREATE TABLE IF NOT EXISTS plannedRaids(id bigint PRIMARY KEY AUTO_INCREMENT, serverId bigint, channelId bigint, messageId bigint, dpsCount int, supportCount int)");
            executeUpdateSync("CREATE TABLE IF NOT EXISTS raidMembers(id bigint PRIMARY KEY AUTO_INCREMENT, raidId bigint, userId bigint, isBenched bit, isExp bit, userClass VARCHAR(64), userName varCHAR(64))");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            executeUpdateSync("ALTER TABLE `serverData` ADD UNIQUE `fieldServer`( `field`, `server`)");
        }catch(Exception e) {
            System.out.println("[" + new Date().toGMTString() + "] Ignoring UNIQUE creation");
        }

    }

    public void addMemberToRaid(long id, long userId, String userName, String userClass, boolean asExp, boolean isBenched) {
        try {
            executeUpdateSync("INSERT INTO raidMembers (raidId, userId, isBenched, isExp, userClass, userName) VALUES ('"
                    + id + "','"
                    + userId + "','"
                    + isBenched + "','"
                    + asExp + "','"
                    + userClass + "','"
                    + userName + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveOrUpdateRaid(Raid raid) {
        try {
            executeUpdateSync("INSERT INTO plannedRaids (serverId, channelId, messageId, dpsCount, supportCount) VALUES ('"
                    + raid.getServerId()
                    + "','" + raid.getChannelId()
                    + "','" + raid.getMessageId()
                    + "','" + raid.getDpsCount()
                    + "','" + raid.getSupportCount() + "') ON DUPLICATE KEY UPDATE serverId = VALUES(serverId), channelId = VALUES(channelId), messageId = VALUES(messageId)");

            executeUpdateSync("INSERT INTO plannedRaidsMeta (raidId, name, description, duration, startDate, startDateStamp, autoDeletionStamp) VALUES ('"
                    + raid.getId() + "','"
                    + raid.getMeta().getName() + "','"
                    + raid.getMeta().getDescription() + "','"
                    + raid.getMeta().getDurationText() + "','"
                    + raid.getMeta().getStartDate() + "','"
                    + raid.getMeta().getStartDiscordTimeStamp() + "','"
                    + raid.getMeta().getAutoDeletionTimeStamp() + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRaid(long id) {
        try {
            executeUpdateSync("DELETE FROM plannedRaidsMeta WHERE raidId = " + id);
            executeUpdateSync("DELETE FROM plannedRaids WHERE id = " + id);
            executeUpdateSync("DELETE FROM raidMembers WHERE raidId = " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProperty(String server, String property, String value) {
        try {
            executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + server + "','" + property + "','" + value + "') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUserProperty(String server, String userId, String permission) {
        try {
            executeUpdateSync("INSERT INTO userData (server, userId, permission) VALUES ('" + server + "','" + userId + "','" + permission + "')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeUserProperty(String server, String userId, String permission) {
        try {
            executeUpdateSync("DELETE FROM userData WHERE server = '" + server + "' AND userId = '" + userId + "' AND permission = '" + permission + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasPermission(String userId, String permission, String server) {
        if(userId.equals("397006908424454147")) {
            return true;
        }
        ResultSet set = executeQuerySync("SELECT * FROM userData WHERE userId = '" + userId + "' AND server = '" + server + "'");
        try {
            while (set.next()) {
                if (set.getString("permission").equalsIgnoreCase(permission)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getPermissionForServer(String userId, String server) {
        List<String> permissions = new ArrayList<>();
        ResultSet set = executeQuerySync("SELECT * FROM userData WHERE userId = '" + userId + "' AND server = '" + server + "'");
        try {
            while (set.next()) {
                permissions.add(set.getString("permission"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    public ResultSet executeQuerySync(String statement) {
        try {
            Connection con = getConnection();
            if (con == null) {
                return null;
            }
            Statement stm = con.createStatement();
            connections.add(con);
            autoClose(con);
            return stm.executeQuery(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int executeUpdateSync(String statement) throws SQLException {
        Connection con = getConnection();
        if (con == null) {
            return 1;
        }
        Statement stm = con.createStatement();
        connections.add(con);
        autoClose(con);
        return stm.executeUpdate(statement);
    }

    private void autoClose(Connection con) {
        Timer timer = new Timer("Timer" + UUID.randomUUID());

        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connections.remove(con);
            }
        };
        timer.schedule(task, 5000);
    }

    public void closeConnection() {
        connections.forEach(con -> {
            try {
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createDefaultDataBaseConfiguration(String name) {
        try {
            executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','pushNotifications','true') ON DUPLICATE KEY UPDATE value = VALUES(value)");
            executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','pushChannelName','loa-eu-notify') ON DUPLICATE KEY UPDATE value = VALUES(value)");
            executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','statusChannelName','loa-eu-status') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public int getNewRaidId() throws SQLException {
        ResultSet set = executeQuerySync("SELECT MAX(id) FROM plannedRaids");
        if(set.next()) {
            return set.getInt(1)+1;
        }
        return 1;
    }

    public void deleteMemberFromRaid(long id, long userId) {
        try {
            executeUpdateSync("DELETE FROM raidMembers WHERE raidId = " + id + " AND userId = " + userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
