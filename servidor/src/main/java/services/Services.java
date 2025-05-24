package services;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import config.ConfigLoader;
import model.Model_User;
import utils.Logger;

import model.Model_Client;
import model.Model_Response;
import model.Model_User_Register;
import model.Model_User_Login;

import services.ServiceUser;
import services.ServiceUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Services {
    private static Services instance;
    private SocketIOServer server;
    private ServiceUser serviceUser;
    private List<Model_Client> localClients;

    ConfigLoader configLoader = ConfigLoader.getInstance();
    Logger logger = Logger.getInstance();
    ServicesApi serverApi = ServicesApi.getInstance();

    public static Services getInstance() {
        if (instance == null) {
            instance = new Services();
        }
        return instance;
    }

    private Services() {
        this.serviceUser = new ServiceUser();
        this.localClients = new ArrayList<Model_Client>();
    }

    public void startServer() throws IOException {
        Configuration config = new Configuration();

        String portFromProperties = configLoader.getServerPort();
        config.setPort(Integer.parseInt(portFromProperties));

        server = new SocketIOServer(config);
        logger.log("Cargando eventos del servidor");

        server.addConnectListener(new ConnectListener() {
           @Override
           public void onConnect(SocketIOClient client) {
               logger.log("Nuevo cliente conectado. IP: " + client.getRemoteAddress());
           }
        });

        server.addEventListener("register", Model_User_Register.class, new DataListener<Model_User_Register>() {
            @Override
            public void onData(SocketIOClient client, Model_User_Register data, AckRequest ackRequest){
                logger.log("El cliente" + client.getRemoteAddress() + " se quiere registrar como: " + data.getUsername());
                Model_Response response = serviceUser.register(data);

                ackRequest.sendAckData(response.isSuccess(), response.getMessage(), response.getData());
            }
        });
        
        server.addEventListener("login", Model_User_Login.class, new DataListener<Model_User_Login>() {
            @Override
            public void onData(SocketIOClient client, Model_User_Login data, AckRequest ackRequest){
                logger.log("El cliente" + client.getRemoteAddress() + " quiere iniciar sesión como: " + data.getEmail());
                Model_Response response = serviceUser.login(data);

                ackRequest.sendAckData(response.isSuccess(), response.getMessage(), response.getData());

                if(response.isSuccess()){
                    addClient(client, (Model_User) response.getData());
                }
            }
        });

        server.start();
        //Inicializo api rest
        serverApi.initAPI();
    }

    public void addClient(SocketIOClient client, Model_User user){
        localClients.add(new Model_Client(client, user));
    }
}
