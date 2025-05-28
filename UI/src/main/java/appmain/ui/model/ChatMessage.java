package appmain.ui.model;

import java.util.Date;

public interface ChatMessage {
    Date getTimestampForChatMessage();
    boolean isSent();

    String getMessage();
}

