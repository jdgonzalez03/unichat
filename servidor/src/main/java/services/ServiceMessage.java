package services;

import connection.ConnectionDB;
import model.Model_Message;
import model.Model_Receive_Message;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ServiceMessage {
    private final Connection connection;
    Logger logger = Logger.getInstance();

    public ServiceMessage() {
        this.connection = ConnectionDB.getInstance().getConnection();
    }

    public Model_Receive_Message saveMessageAndFormat(Model_Message message) {
        Model_Receive_Message msg = new Model_Receive_Message();
        try{
            PreparedStatement checkStmt = connection.prepareStatement(SAVE_MSG);
            checkStmt.setString(1, message.getMessage());
            checkStmt.setTimestamp(2, new java.sql.Timestamp(message.getTimestamp().getTime())); // usa el timestamp del cliente
            checkStmt.setString(3, message.getSenderEmail());

            if (message.getReceiverEmail() != null) {
                checkStmt.setString(4, message.getReceiverEmail());
            }else{
                checkStmt.setNull(4, java.sql.Types.VARCHAR);
            }

            if (message.getSenderEmail() != null) {
                checkStmt.setString(5, message.getSenderEmail());
            }else{
                checkStmt.setNull(5, java.sql.Types.VARCHAR);
            }

            int rowsInserted = checkStmt.executeUpdate();
            if (rowsInserted > 0) {
                msg.setMessage(message.getMessage());
                msg.setTimestamp(message.getTimestamp());
                msg.setSenderEmail(message.getSenderEmail());
                msg.setGroupName(message.getGroupName());
                logger.log("Mensaje guardado y formateado exitosamente.");
            }
        }catch (Exception e) {
            e.printStackTrace();
            logger.log("Error guardando el mensaje. " + e.getMessage());
        }

        return msg;
    }

    private final String SAVE_MSG = "INSERT INTO messages (message, timestamp, sender_email, receiver_email, group_name) VALUES (?, ?, ?, ?, ?)";
}
