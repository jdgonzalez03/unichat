package appmain.ui.services;

import appmain.ui.model.Model_Create_Group;
import com.google.gson.Gson;
import io.socket.client.Ack;
import io.socket.client.Socket;
import utils.Logger;

public class GroupService {
    private final Socket socket;
    private final Gson gson = new Gson();
    private Logger logger = Logger.getInstance();

    public GroupService() {
        this.socket = Services.getInstance().getClient();
    }

    public void createGroup(Model_Create_Group data, Ack callback) {
        logger.log("Llamando a servicio de crear grupo");
        String json = gson.toJson(data);

        socket.emit("create_group", gson.fromJson(json, Object.class), callback);
    }
}
