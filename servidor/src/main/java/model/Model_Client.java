package model;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

public class Model_Client {
    private SocketIOClient client;
    private Model_User user;

    public SocketIOClient getClient() {
        return client;
    }

    public void setClient(SocketIOClient client) {
        this.client = client;
    }

    public Model_User getUser() {
        return user;
    }

    public void setUser(Model_User user) {
        this.user = user;
    }

    public Model_Client(SocketIOClient client, Model_User user) {
        this.client = client;
        this.user = user;
    }

    public Model_Client() {}
}
