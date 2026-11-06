
package minute_burger_inventory_system;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/minute_burger_db", "root", "");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Database Connection Failed: " + e.getMessage());
        }
        return con;
    }
}
