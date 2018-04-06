package edu.temple.mapchat;

/**
 * Created by Jessica on 4/4/2018.
 */

public class ChatMessage {
    private String message;
    private String sender;
    private String receiver;

    public ChatMessage() {

    }

    public ChatMessage(String message, String sender, String receiver) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String senderId) {
        this.sender = senderId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiverId) {
        this.receiver = receiverId;
    }
}
