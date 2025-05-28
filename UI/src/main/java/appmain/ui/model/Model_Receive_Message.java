package appmain.ui.model;

import java.io.Serializable;
import java.util.Date;

public class Model_Receive_Message implements ChatMessage{
    private int id;
    private String message;
    private Date timestamp;
    private String senderEmail;
    private String groupName;

    public Model_Receive_Message(int id, String message, Date timestamp, String senderEmail, String groupName) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.senderEmail = senderEmail;
        this.groupName = groupName;
    }

    public Model_Receive_Message() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public Date getTimestampForChatMessage(){
        return timestamp;
    }

    @Override
    public boolean isSent(){
        return false;
    }
}

