package me.yarinlevi.qpunishments.utilities;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author YarinQuapi
 */
public class QDatabase {
    private Connection connection;
    @Getter private static QDatabase instance;
    private final Logger logger = QBungeePunishments.getInstance().getLogger();
    private static boolean sqlite = false;

    public QDatabase(Configuration config) {
        instance = this;

        if (config.getBoolean("mysql.enabled")) {
            if (config.getString("mysql.host") == null
                    || config.getString("mysql.database") == null
                    || config.getString("mysql.port") == null
                    || config.getString("mysql.user") == null
                    || config.getString("mysql.pass") == null) {
                System.out.println("[QDataBase] Hey! you haven't configured your mysql connection! aborting connection!");
                return;
            }
        }

        HikariDataSource dataSource = new HikariDataSource();

        //MYSQL 8.x CONNECTOR - com.mysql.cj.jdbc.MysqlDataSource
        //MYSQL 5.x CONNECTOR - com.mysql.jdbc.jdbc2.optional.MysqlDataSource
        //SQLITE - org.sqlite.JDBC

        if (config.getBoolean("sqlite.enabled")) {
            HikariConfig hc = new HikariConfig();

            hc.setDriverClassName("org.sqlite.JDBC");
            hc.setJdbcUrl("jdbc:sqlite:" + QBungeePunishments.getInstance().getDataFolder() + "/database.db");

            dataSource = new HikariDataSource(hc);

        } else if (config.getBoolean("mysql.enabled")) {

            String hostName = config.getString("mysql.host");
            String database = config.getString("mysql.database");
            int port = config.getInt("mysql.port");
            String user = config.getString("mysql.user");
            String pass = config.getString("mysql.pass");

            dataSource.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            dataSource.addDataSourceProperty("serverName", hostName);
            dataSource.addDataSourceProperty("port", port);
            dataSource.addDataSourceProperty("databaseName", database);
            dataSource.addDataSourceProperty("user", user);
            dataSource.addDataSourceProperty("password", pass);
            dataSource.addDataSourceProperty("useSSL", config.getBoolean("mysql.ssl"));
            dataSource.addDataSourceProperty("autoReconnect", true);
            dataSource.addDataSourceProperty("characterEncoding", "UTF-8");
        }



        String punishmentTableSQL;
        String proofTableSQL;
        String playerDataTableSQL;


        if (config.getBoolean("sqlite.enabled")) { // SQLite syntax
            punishmentTableSQL = "CREATE TABLE IF NOT EXISTS `punishments`(" +
                    "`punished_uuid` VARCHAR(40) NOT NULL," +
                    "`punished_by_uuid` VARCHAR(40) DEFAULT NULL," +
                    "`punished_by_name` VARCHAR(16) NOT NULL," +
                    "`reason` TEXT NOT NULL, " +
                    "`expire_date` TEXT DEFAULT NULL," +
                    "`punishment_type` TEXT NOT NULL," +
                    "`server` TEXT NOT NULL," +
                    "`date_added` TEXT NOT NULL," +
                    "`bypass_expire_date` BOOLEAN NOT NULL DEFAULT FALSE" +
                    ");";

            proofTableSQL = "CREATE TABLE IF NOT EXISTS `comments` ("
                    + "`punished_uuid` VARCHAR(40) NOT NULL,"
                    + "`content` varchar(255) NOT NULL,"
                    + "`punished_by_uuid` varchar(40) NOT NULL,"
                    + "`punished_by_name` varchar(40) NOT NULL,"
                    + "`date_added` TEXT NOT NULL"
                    + ");";

            playerDataTableSQL = "CREATE TABLE IF NOT EXISTS `playerData` (" +
                    "`uuid` VARCHAR(40) NOT NULL," +
                    "`name` VARCHAR(16) NOT NULL," +
                    "`ip` TEXT NOT NULL," +
                    "`firstLogin` TEXT NOT NULL," +
                    "`lastLogin` TEXT NOT NULL" +
                    ");";

            sqlite = true;
        } else { // MySQL syntax
            punishmentTableSQL = "CREATE TABLE IF NOT EXISTS `punishments`(" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`punished_uuid` VARCHAR(40) NOT NULL," +
                    "`punished_by_uuid` VARCHAR(40) DEFAULT NULL," +
                    "`punished_by_name` VARCHAR(16) NOT NULL," +
                    "`reason` TEXT NOT NULL, " +
                    "`expire_date` TEXT DEFAULT NULL," +
                    "`punishment_type` TEXT NOT NULL," +
                    "`server` TEXT NOT NULL," +
                    "`date_added` TEXT NOT NULL," +
                    "`bypass_expire_date` BOOLEAN NOT NULL DEFAULT FALSE," +
                    "PRIMARY KEY (`id`)" +
                    ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";

            proofTableSQL = "CREATE TABLE IF NOT EXISTS `comments` ("
                    + "`id` INT NOT NULL AUTO_INCREMENT,"
                    + "`punished_uuid` VARCHAR(40) NOT NULL,"
                    + "`content` varchar(255) NOT NULL,"
                    + "`punished_by_uuid` varchar(40) NOT NULL,"
                    + "`punished_by_name` varchar(40) NOT NULL,"
                    + "`date_added` TEXT NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";

            playerDataTableSQL = "CREATE TABLE IF NOT EXISTS `playerData` (" +
                    "`id` INT NOT NULL AUTO_INCREMENT," +
                    "`uuid` VARCHAR(40) NOT NULL," +
                    "`name` VARCHAR(16) NOT NULL," +
                    "`ip` TEXT NOT NULL," +
                    "`firstLogin` TEXT NOT NULL," +
                    "`lastLogin` TEXT NOT NULL," +
                    "PRIMARY KEY (`id`)"+
                    ") DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;";
        }

        logger.info("Please await database connection..");

        try {
            connection = dataSource.getConnection();

            Statement statement = connection.createStatement();
            {
                statement.executeUpdate(punishmentTableSQL);
                statement.executeUpdate(proofTableSQL);
                statement.executeUpdate(playerDataTableSQL);
                statement.closeOnCompletion();
                logger.info("Successfully established connection to database.");
            }

            // Auto checker for mysql to kill any disconnection problems
            if (!config.getBoolean("sqlite.enabled"))
                QBungeePunishments.getInstance().getProxy().getScheduler().schedule(QBungeePunishments.getInstance(), () -> get("SELECT NOW();"), 120L, 120L, TimeUnit.SECONDS);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.severe("Something went horribly wrong! please check your credentials and settings.");
        }
    }

    @Nullable
    public ResultSet get(String query) {

        if (sqlite && query.contains(" id "))
            query = query.replaceAll("( id )", " rowid ");

        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public int update(String query) {
        if (sqlite && query.contains(" id "))
            query = query.replaceAll("( id )", " rowid ");

        try {
            return connection.createStatement().executeUpdate(query);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public boolean insert(String query) {
        if (sqlite && query.contains(" id "))
            query = query.replaceAll("( id )", " rowid ");


        try {
            connection.createStatement().execute(query);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean insertLarge(List<String> list) {
        try {
            Statement statement = connection.createStatement();

            for (String s : list) {
                statement.addBatch(s);
            }

            statement.executeBatch();
            statement.closeOnCompletion();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
