package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static ConfigLoader instance;
    private Properties properties;

    private ConfigLoader() {
        properties = new Properties();
    }

    public static synchronized ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    public void loadProperties(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            // Carga el archivo properties
            properties.load(input);
            System.out.println("Archivo de configuración cargado correctamente.");
        } catch (IOException ex) {
            System.err.println("Error al cargar el archivo de configuración: " + ex.getMessage());
        }
    }

    public void loadPropertiesFromResources(String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("No se pudo encontrar el archivo " + fileName);
                return;
            }
            properties.load(input);
            System.out.println("Archivo de configuración cargado correctamente desde resources.");
        } catch (IOException ex) {
            System.err.println("Error al cargar el archivo de configuración: " + ex.getMessage());
        }
    }

    public String getURLDb(){
        return properties.getProperty("server.db.url");
    }

    public String getUser(){
        return properties.getProperty("server.db.user");
    }

    public String getPassword(){
        return properties.getProperty("server.db.password");
    }

    public String getServerName(){
        return properties.getProperty("server.name");
    }

    public String getServerPort(){
        return properties.getProperty("server.port");
    }

    public String getServerApiPort(){
        return properties.getProperty("server.api.port");
    }
}