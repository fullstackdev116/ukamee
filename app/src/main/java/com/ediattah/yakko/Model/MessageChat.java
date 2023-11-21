package com.ediattah.yakko.Model;

import java.io.Serializable;

public class MessageChat implements Serializable {
    public String _id;
    public String sender_id;
    public String receiver_id;
    public String message;
    public String file;
    public String file_type;
    public long timestamp;
    public boolean seen;

    public MessageChat(String _id, String sender_id, String receiver_id, String message, String file, String file_type, long timestamp, boolean seen) {
        this._id = _id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message = message;
        this.file = file;
        this.file_type = file_type;
        this.timestamp = timestamp;
        this.seen = seen;
    }
    public MessageChat() {
        this._id = "";
        this.sender_id = "";
        this.receiver_id = "";
        this.message = "";
        this.file = "";
        this.file_type = "";
        this.timestamp = 0;
        this.seen = false;
    }
}
