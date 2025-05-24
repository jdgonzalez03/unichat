package software2;

import UI.ServerUI;
import UI.SplashScreen;
import utils.Logger;
import config.ConfigLoader;
import connection.ConnectionDB;
import services.Services;

import javax.swing.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public class Main {
    public static ServerUI server;
    public static void main(String[] args) throws SQLException {
        ConfigLoader config = ConfigLoader.getInstance();
        config.loadPropertiesFromResources("config.properties");
        Logger logger = Logger.getInstance();
        ConnectionDB.getInstance().openConnection();
        Services services = Services.getInstance();

        logger.log("Inicializando proyecto");
        logger.log("Configuraciones cargadas");

        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);


        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
                server = new ServerUI();
                splash.setVisible(false);
                splash.dispose();
                try {
                    services.startServer();
                } catch (IOException e) {
                    logger.log("Error al iniciar el servidor" + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }).start();
        logger.log("Inicializando proyecto");
    }
}