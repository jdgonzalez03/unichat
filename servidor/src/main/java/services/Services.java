package services;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import config.ConfigLoader;
import model.*;
import net.bytebuddy.matcher.StringMatcher;
import utils.Logger;

import services.ServiceUser;
import services.ServiceUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Services {
    private static Services instance;
    private SocketIOServer server;
    private ServiceUser serviceUser;
    private ServiceMessage serviceMessage;
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
        this.serviceMessage = new ServiceMessage();
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

                //Enviar lista de usuarios a cada cliente que esta en localClients
                if (response.isSuccess()) {
                    broadcastUserListToAllClients();
                }
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
                    broadcastUserListToAllClients();
                }
            }
        });

        //TODO: Change when add p2p
        server.addEventListener("list_users", Model_Request_UserList.class, new DataListener<Model_Request_UserList>() {
            @Override
            public void onData(SocketIOClient client, Model_Request_UserList data, AckRequest ackRequest){
                logger.log("El cliente " + client.getRemoteAddress() + " quiere listar todos los usuarios como: " + data.getUsername());
                List<Model_User_With_Status> list_users = serviceUser.getUsers(data, localClients);

                client.sendEvent("list_users", list_users.toArray());
                logger.log("Enviando lista actualizada de usuarios a: " + data.getUsername() + " en " + client.getRemoteAddress());
            }
        });

        server.addEventListener("send_message_to", Model_Message.class, new DataListener<Model_Message>() {
            @Override
            public void onData(SocketIOClient client, Model_Message data, AckRequest ackRequest){
                logger.log("Enviando mensaje de: " + data.getSenderEmail() + " en " + client.getRemoteAddress() + " a: " + data.getReceiverEmail());
                sendMessageToClient(data, ackRequest);
            }
        });

        server.addDisconnectListener(client -> {
            logger.log("Cliente desconectado: " + client.getRemoteAddress());
            // Remover cliente de la lista local
            localClients.removeIf(c -> c.getClient().getSessionId().equals(client.getSessionId()));
            // Enviar nueva lista a todos
            broadcastUserListToAllClients();
        });


        server.start();
        //Inicializo api rest
        serverApi.initAPI();
    }

    public void addClient(SocketIOClient client, Model_User user){
        localClients.add(new Model_Client(client, user));
    }

    private void broadcastUserListToAllClients() {
        for (Model_Client c : localClients) {
            Model_User user = c.getUser();
            Model_Request_UserList request = new Model_Request_UserList(
                    user.getUsername(), user.getEmail()
            );
            List<Model_User_With_Status> personalizedList = serviceUser.getUsers(request, localClients);
            c.getClient().sendEvent("list_users", personalizedList.toArray());
        }
    }

    private void sendMessageToClient(Model_Message message, AckRequest ackRequest){
        //TODO: Implementar tipo de mensaje para enviar documentos
        //TODO: Modificar lógica para el p2p
        for (Model_Client c : localClients) {
            if (c.getUser().getEmail().equals(message.getReceiverEmail()) ) {
                Model_Receive_Message msg = serviceMessage.saveMessageAndFormat(message);
                c.getClient().sendEvent("receive_message_from", msg);
            }
        }
    }

}
