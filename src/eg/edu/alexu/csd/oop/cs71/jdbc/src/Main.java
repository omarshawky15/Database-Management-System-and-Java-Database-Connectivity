package eg.edu.alexu.csd.oop.cs71.jdbc.src;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public class Main {
    public static void main(String[] args){
        Driver driver = new Driver();
        Properties info = new Properties();
        File dbDir = new File("Databases");
        info.put("path", dbDir.getAbsoluteFile());
        try {
            Connection connection1 = (Connection) driver.connect("jdbc:xmldb://localhost", info);
            System.out.println("Success?");
            connection1.close();
            System.out.println("Success?");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Success?");
    }
}
