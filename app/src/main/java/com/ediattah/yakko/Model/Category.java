package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Category implements Serializable {
    public String _id;
    public String type;
    public String name;
    public String admin_id;
    public String photo;

    public Category(String _id, String type, String name, String admin_id, String photo) {
        this._id = _id;
        this.type = type;
        this.name = name;
        this.admin_id = admin_id;
        this.photo = photo;
    }
    public Category() {
        this._id = "";
        this.type = "";
        this.name = "";
        this.admin_id = "";
        this.photo = "";
    }
}
