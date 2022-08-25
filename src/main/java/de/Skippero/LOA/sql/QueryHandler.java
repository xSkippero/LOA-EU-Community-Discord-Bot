package de.Skippero.LOA.sql;

import com.google.common.collect.Multimap;
import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.config.ConfigManager;

import java.sql.*;
import java.util.*;

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
        if(con != null) {
            connections.add(con);
            System.out.println("Succesfully established SQL Connection");
            createTables();
        }else{
            System.out.println("Could not established SQL Connection, exiting with Error code 1");
            System.exit(1);
        }
    }

    public Multimap<String, String[]> loadConfiguration(Multimap<String, String[]> map) {
        ResultSet set = executeQuerySync("SELECT * FROM serverData");
        try{
            while(set.next()) {
                map.put(set.getString("server"),new String[]{set.getString("field"),set.getString("value")});
            }
        }catch (SQLException e) {
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
        System.out.println("Creating Tables if not exits");
        executeUpdateSync("CREATE TABLE IF NOT EXISTS serverData(id bigint PRIMARY KEY AUTO_INCREMENT, field VARCHAR(64), value VARCHAR(64), server VARCHAR(64))");
        executeUpdateSync("ALTER TABLE `serverData` ADD UNIQUE( `field`, `server`)");
    }

    public void updateProperty(String server, String property, String value) {
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('"+server+"','"+property+"','"+value+"') ON DUPLICATE KEY UPDATE value = VALUES(value)");
    }

    public ResultSet executeQuerySync(String statement) {
        try {
            Connection con = getConnection();
            if(con == null) {
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
            if(con == null) {
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
        Timer timer = new Timer("Timer"+ UUID.randomUUID());

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
        connections.forEach(con-> {
            try {
                if(!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createDefaultDataBaseConfiguration(String name) {
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('"+name+"','pushNotifications','true') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('"+name+"','pushChannelName','loa-euw-notify') ON DUPLICATE KEY UPDATE value = VALUES(value)");
        executeUpdateSync("INSERT INTO serverData (server, field, value) VALUES ('"+name+"','statusChannelName','loa-euw-status') ON DUPLICATE KEY UPDATE value = VALUES(value)");
    }
}
