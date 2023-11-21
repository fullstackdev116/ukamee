package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Friend implements Serializable {
    public String _id;
    public String sender_id;
    public String receiver_id;
    public int state;

    public Friend(String _id, String sender_id, String receiver_id, int state) {
        this._id = _id;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.state = state;
    }
    public Friend() {
        this._id = "";
        this.sender_id = "";
        this.receiver_id = "";
        this.state = 0;
    }
}
