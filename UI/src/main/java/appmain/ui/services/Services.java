package appmain.ui.services;

import appmain.ui.MainViewController;
import appmain.ui.model.Model_Receive_Message;
import appmain.ui.model.Model_User_With_Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.socket.client.Socket;
import io.socket.client.IO;

import io.socket.emitter.Emitter;
import utils.DateDeserializer;
import utils.Logger;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Services {
    private static Services instance;
    private Socket client;
    private MainViewController mainViewController;

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
            client.on("list_users", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Gson gson = new Gson();
                    List<Model_User_With_Status> users = new ArrayList<>();
                    for (Object o : args) {
                        Model_User_With_Status user = gson.fromJson(o.toString(), Model_User_With_Status.class);

                        users.add(user);
                    }
                    //Update UI
                    if (mainViewController != null) {
                        mainViewController.updateUserList(users);
                        logger.log("Lista de usuarios, esta siendo actualizada");
                    }
                }
            });
            client.on("receive_message_from", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Date.class, new DateDeserializer())
                            .create();

                    Model_Receive_Message msg = gson.fromJson(args[0].toString(), Model_Receive_Message.class);

                    if (mainViewController != null) {
                        logger.log("Mensaje recibido de: " + msg.getSenderEmail());
                        logger.log("Mensaje: " + msg.getMessage());
                    }
                }
            });
            client.open();
            logger.log("Conectado al servidor");
        } catch (Exception e) {
            logger.log("Error al crear el servidor: " + e.getMessage());
        }


    }

    public Socket getClient() {
        return client;
    }

    public MainViewController getMainViewController() {
        return mainViewController;
    }

    public void setMainViewController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }
}
