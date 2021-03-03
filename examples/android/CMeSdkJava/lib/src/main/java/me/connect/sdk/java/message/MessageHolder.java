package me.connect.sdk.java.message;

public class MessageHolder {
    private final String message;
    private final String messageOptions;

    public MessageHolder(String message, String messageOptions) {
        this.message = message;
        this.messageOptions = messageOptions;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageOptions() {
        return messageOptions;
    }
}
