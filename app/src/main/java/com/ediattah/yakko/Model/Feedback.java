package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Feedback implements Serializable {
    public String _id;
    public String user_id;
    public String admin_id;
    public String feedback;
    public float rate;
    public String date;

    public Feedback(String _id, String user_id, String admin_id, String feedback, float rate, String date) {
        this._id = _id;
        this.user_id = user_id;
        this.admin_id = admin_id;
        this.feedback = feedback;
        this.rate = rate;
        this.date = date;
    }
    public Feedback() {
        this._id = "";
        this.user_id = "";
        this.admin_id = "";
        this.feedback = "";
        this.rate = 0;
        this.date = "";
    }
}
