package appmain.ui.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Model_ChatData {
    private final String chatLabel; // Puede ser email, nombre de grupo, etc.
    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean hasUnreadMessages;

    public Model_ChatData(String chatLabel) {
        this.chatLabel = chatLabel;
        this.hasUnreadMessages = false;
    }

    public String getChatLabel() {
        return chatLabel;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public boolean hasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        this.hasUnreadMessages = true;
    }
}

