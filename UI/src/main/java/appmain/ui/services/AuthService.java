package appmain.ui.services;

import appmain.ui.model.Model_User_Register;
import com.google.gson.Gson;
import io.socket.client.Ack;
import io.socket.client.Socket;

public class AuthService {

    private final Socket socket;
    private final Gson gson = new Gson();

    public AuthService() {
        this.socket = Services.getInstance().getClient();
    }

    public void registerUser(Model_User_Register user, Ack callback) {
        String json = gson.toJson(user);
        socket.emit("register", gson.fromJson(json, Object.class), callback);
    }
}
