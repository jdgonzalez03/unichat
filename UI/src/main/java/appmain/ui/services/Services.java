package appmain.ui.services;

import io.socket.client.Socket;
import io.socket.client.IO;

import utils.Logger;

import java.net.URISyntaxException;

public class Services {
    private static Services instance;
    private Socket client;

    //TODO: Cambiar esto al final, para pode usar los peer to peer
    private final int PORT_NUMBER = 9999;
    private final String ip = "localhost";

    Logger logger = Logger.getInstance();

    public static Services getInstance() {
        if (instance == null) {
            instance = new Services();
        }
        return instance;
    }

    private Services() {}

    public void startServer() throws URISyntaxException {

        try {
            client = IO.socket("http://" + ip + ":" + PORT_NUMBER);
            client.open();
            logger.log("Conectado al servidor");
        } catch (Exception e) {
            logger.log("Error al crear el servidor: " + e.getMessage());
        }


    }

    public Socket getClient() {
        return client;
    }
}
