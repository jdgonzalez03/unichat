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
    private ServiceGroups serviceGroups;
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
        this.serviceGroups = new ServiceGroups();
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

                    //Enviar las invitaciones
                    List<Model_Join_Group> requests = serviceGroups.getAllPendingRequests((Model_User) response.getData());
                    sendAllPendingRequestToJoinGroup((Model_User) response.getData(), requests);

                    //Enviar todos los grupos a los que pertenece el cliente
                    sendAllGroups((Model_User) response.getData());
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

        server.addEventListener("create_group", Model_Create_Group.class, new DataListener<Model_Create_Group>() {
            @Override
            public void onData(SocketIOClient client, Model_Create_Group data, AckRequest ackRequest){
                logger.log("El usuario " + data.getCreator_email() + " esta tratando de crear un grupo");
                Model_Response response = serviceGroups.createGroup(data);

                for (Model_User_With_Status member: data.getMembers()) {
                    List<Model_Join_Group> requests = serviceGroups.getAllPendingRequests(member.getUser());
                    sendAllPendingRequestToJoinGroup(member.getUser(), requests);
                }
                ackRequest.sendAckData(response.isSuccess(), response.getMessage(), response.getData());

                if(response.isSuccess()){
                    Model_User user = new Model_User();
                    user.setEmail(data.getCreator_email());
                    sendAllGroups(user);
                }
            }
        });

        server.addEventListener("response_join_group", Model_Join_Group_Response.class, new DataListener<Model_Join_Group_Response>() {
            @Override
            public void onData(SocketIOClient client, Model_Join_Group_Response data, AckRequest ackRequest){
                logger.log("El usuario " + data.getInvitedEmail() + " ha respondido la solicitud a unirse al grupo: " + data.getGroupName());
                Model_Response response = serviceGroups.responsePendingRequest(data);
                ackRequest.sendAckData(response.isSuccess(), response.getMessage(), response.getData());

                if(response.isSuccess()){
                    logger.log("Enviando lista de grupos actualizada");
                    broadcastGroupListToAllClients();
                }
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

    private void sendAllGroups(Model_User user){
        for(Model_Client c : localClients){
            Model_User u = c.getUser();
            if(u.getEmail().equals(user.getEmail())){
                List<Model_My_Groups> groups = serviceGroups.getAllMyGroups(user);
                c.getClient().sendEvent("list_groups", groups.toArray());
            }
        }
    }

    private void broadcastGroupListToAllClients() {
        for (Model_Client c : localClients) {
            Model_User u = c.getUser();
            List<Model_My_Groups> groups = serviceGroups.getAllMyGroups(u);
            c.getClient().sendEvent("list_groups", groups.toArray());
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

    private void sendAllPendingRequestToJoinGroup(Model_User user, List<Model_Join_Group> requests){
        for(Model_Client c: localClients){
            if(c.getUser().getEmail().equals(user.getEmail())){
                logger.log("Total de inviaciontes " + requests.size() + " enviadas a: " + user.getEmail());
                c.getClient().sendEvent("request_join_group", requests.toArray());
            }
        }
    }

}
