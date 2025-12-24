package ma.examen.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    private static Connection connection;

    private DBConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // 1. Force the driver to load (Fixes 'No suitable driver found')
                Class.forName("com.mysql.cj.jdbc.Driver");

                // 2. Load Properties
                Properties prop = new Properties();
                InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");

                if (input == null) {
                    input = DBConnection.class.getResourceAsStream("/database.properties");
                }

                if (input != null) {
                    prop.load(input);
                    input.close();

                    // 3. Create Connection
                    connection = DriverManager.getConnection(
                            prop.getProperty("db.url"),
                            prop.getProperty("db.user"),
                            prop.getProperty("db.password")
                    );
                    System.out.println("✅ Database Connected Successfully");
                } else {
                    System.err.println("❌ Error: database.properties not found.");
                }

            } catch (ClassNotFoundException e) {
                System.err.println("❌ Error: MySQL Driver not found. Check pom.xml dependencies.");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}