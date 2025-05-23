package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import config.ConfigLoader;
import org.slf4j.LoggerFactory;
import utils.Logger;

public class ConnectionDB {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ConnectionDB.class);
    private static ConnectionDB instance;
    private Connection connection;

    ConfigLoader config = ConfigLoader.getInstance();
    Logger logger = Logger.getInstance();

    public static ConnectionDB getInstance() {
        if (instance == null) {
            instance = new ConnectionDB();
        }
        return instance;
    }

    private ConnectionDB() {}

    public void openConnection() throws SQLException {
        String url = config.getURLDb();
        String user = config.getUser();
        String password = config.getPassword();

        logger.log("Connecting to " + url);
        logger.log("User: " + user);
        logger.log("Password: " + password);

        connection = DriverManager.getConnection(url, user, password);
        logger.log("Conección con la base de datos establecida.");
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
        } catch (SQLException e) {
            logger.log("Error al verificar o abrir la conexión: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }



    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                logger.log("Conexión cerrada");
            } catch (SQLException e) {
                logger.log("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Crea una tabla de prueba en la base de datos
     * @return boolean indicando si la operación fue exitosa
     */
    public boolean createTestTable() {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                logger.log("No hay conexión disponible para crear la tabla");
                return false;
            }

            Statement stmt = conn.createStatement();

            // Primero intentamos eliminar la tabla si ya existe
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS test_table");
                logger.log("Tabla antigua eliminada (si existía)");
            } catch (SQLException e) {
                logger.log("Nota: " + e.getMessage());
                // Continuamos aunque falle este paso
            }

            // Creamos la tabla
            String createTableSQL =
                    "CREATE TABLE test_table (" +
                            "  id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  nombre VARCHAR(100) NOT NULL," +
                            "  email VARCHAR(100)," +
                            "  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ")";

            stmt.executeUpdate(createTableSQL);
            logger.log("Tabla test_table creada exitosamente");

            // Insertamos algunos datos de prueba
            String insertSQL =
                    "INSERT INTO test_table (nombre, email) VALUES " +
                            "('Usuario Test 1', 'test1@example.com'), " +
                            "('Usuario Test 2', 'test2@example.com'), " +
                            "('Usuario Test 3', 'test3@example.com')";

            int rowsAffected = stmt.executeUpdate(insertSQL);
            logger.log("Datos insertados: " + rowsAffected + " filas");

            stmt.close();
            return true;
        } catch (SQLException e) {
            logger.log("Error al crear la tabla de prueba: " + e.getMessage());
            return false;
        }
    }

}
