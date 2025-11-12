package com.chatapp.models;

public class Message {
    private String user;
    private String text;
    private String timestamp; // ISO-8601 string, e.g. "2025-11-11T21:15:00Z"

    public Message() {
    }

    public Message(String user, String text, String timestamp) {
        this.user = user;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}