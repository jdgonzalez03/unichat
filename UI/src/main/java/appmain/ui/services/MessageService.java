package appmain.ui.services;

import appmain.ui.model.Model_Message;
import com.google.gson.Gson;
import io.socket.client.Ack;
import io.socket.client.Socket;

public class MessageService {
    private final Socket socket;
    private final Gson gson = new Gson();

    public MessageService(){
        this.socket = Services.getInstance().getClient();
    }

    public void sendMessage(Model_Message message, Ack callback){
        String json = gson.toJson(message);
        socket.emit("send_message_to", gson.fromJson(json, Object.class), callback);
    }
}
