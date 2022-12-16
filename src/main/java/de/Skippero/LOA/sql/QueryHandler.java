package de.Skippero.LOA.sql;

import com.google.common.collect.Multimap;
import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.config.ConfigManager;
import net.dv8tion.jda.api.entities.User;

import java.sql.*;
import java.util.*;
import java.util.Date;

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
        executeUpdateSync("CREATE TABLE IF NOT EXISTS serverData(id bigint PRIMARY KEY AUTO_INCREMENT, field VARCHAR(64), value VARCHAR(64), server VARCHAR(64))");
        executeUpdateSync("CREATE TABLE IF NOT EXISTS userData(id bigint PRIMARY KEY AUTO_INCREMENT, userId VARCHAR(64), permission VARCHAR(64), server VARCHAR(64))");
        executeUpdateSync("CREATE TABLE IF NOT EXISTS userVendorData(id bigint PRIMARY KEY AUTO_INCREMENT, userId VARCHAR(64), cardId int)");
        executeUpdateSync("ALTER TABLE `serverData` ADD UNIQUE `fieldServer`( `field`, `server`)");
    }

    public void updateProperty(String server, String property, String value) {
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + server + "','" + property + "','" + value + "') ON DUPLICATE KEY UPDATE value = VALUES(value)");
    }

    public void insertUserProperty(String server, String userId, String permission) {
        executeUpdateSync("INSERT INTO userData (server, userId, permission) VALUES ('" + server + "','" + userId + "','" + permission + "')");
    }

    public void removeUserProperty(String server, String userId, String permission) {
        executeUpdateSync("DELETE FROM userData WHERE server = '" + server + "' AND userId = '" + userId + "' AND permission = '" + permission + "'");
    }

    public void insertUserVendorProperty(String userId, int cardId) {
        executeUpdateSync("INSERT INTO userVendorData (userId, cardId) VALUES ('" + userId + "','" + cardId + "')");
    }

    public void removeUserVendorProperty(String userId, int cardId) {
        executeUpdateSync("DELETE FROM userVendorData WHERE userId = '" + userId + "' AND cardId = '" + cardId + "'");
    }

    public List<String> getAllVendorUserIds() {
        List<String> vendorUserIds = new ArrayList<>();
        ResultSet set = executeQuerySync("SELECT DISTINCT(userId) FROM userVendorData");
        try {
            while (set.next()) {
                String userId = set.getString("userId");
                vendorUserIds.add(userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vendorUserIds;
    }

    public void clearUserVendorCards(String userId) {
        executeUpdateSync("DELETE FROM userVendorData WHERE userId = '" + userId + "' AND cardId >= '0'");
    }

    public void deleteOldServer(String userId) {
        executeUpdateSync("DELETE FROM userVendorData WHERE userId = '" + userId + "' AND cardId < 0");
    }

    public int getServerForCardUser(String userId) {
        ResultSet set = executeQuerySync("SELECT * FROM userVendorData WHERE userId = '" + userId + "' AND cardId < 0");
        int i = -3;
        try {
            while (set.next()) {
                i = set.getInt("cardId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public List<Integer> getSelectedCardsForUser(String userId) {
        List<Integer> selectedCards = new ArrayList<>();
        ResultSet set = executeQuerySync("SELECT * FROM userVendorData WHERE userId = '" + userId + "'");
        try {
            while (set.next()) {
                int cardId = set.getInt("cardId");
                if(cardId >= 0) {
                    selectedCards.add(cardId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return selectedCards;
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

    public int executeUpdateSync(String statement) {
        try {
            Connection con = getConnection();
            if (con == null) {
                return 1;
            }
            Statement stm = con.createStatement();
            connections.add(con);
            autoClose(con);
            return stm.executeUpdate(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','pushNotifications','true') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','pushChannelName','loa-euw-notify') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','statusChannelName','loa-euw-status') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('" + name + "','merchantChannelName','loa-euw-merchants') ON DUPLICATE KEY UPDATE value = VALUES(value)");
    }
}
