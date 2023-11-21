package com.ediattah.yakko.Model;

public class MessageBot {
    // Type 0 for sent, 1 for received
    private boolean type;
    // Message content
    private String message;
    private User doctor;

    public MessageBot(boolean type, String message, User doctor) {
        this.type = type;
        this.message = message;
        this.doctor = doctor;
    }
    public User getDoctor() {
        return doctor;
    }

    public String getMessage() {
        return message;
    }

    public boolean getType() {
        return this.type;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(boolean type) {
        this.type = type;
    }
}
