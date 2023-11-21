package com.ediattah.yakko.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatRoom implements Serializable {
    public String _id;
    public ArrayList<MessageChat> messages;
    public boolean isTyping;

    public ChatRoom(String _id, ArrayList<MessageChat> messages, boolean isTyping) {
        this._id = _id;
        this.messages = messages;
        this.isTyping = isTyping;
    }
    public ChatRoom() {
        this._id = "";
        this.messages = new ArrayList<>();
        this.isTyping = false;
    }
}
