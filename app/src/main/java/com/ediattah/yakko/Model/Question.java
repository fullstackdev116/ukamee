package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Question implements Serializable {
    public String _id;
    public String category_id;
    public String question;

    public Question(String _id, String category_id, String question) {
        this._id = _id;
        this.category_id = category_id;
        this.question = question;
    }
    public Question() {
        this._id = "";
        this.category_id = "";
        this.question = "";
    }
}
