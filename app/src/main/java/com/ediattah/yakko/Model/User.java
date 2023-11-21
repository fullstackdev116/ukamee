package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class User implements Serializable {
    public String uid;
    public String firstname;
    public String lastname;
    public String phone;
    public String email;
    public String photo;
    public String type;
    public int state;
    public String token;
    public int status;
    public String date;
    public String key;
    public String ID;
    public String address;
    public int membership;
    public String expiry;
    public float rate;

    public User(String uid, String firstname, String lastname, String phone, String email, String photo, String type, int state, String token, int status, String date, String key, String ID, String address, int membership, String expiry, float rate) {
        this.uid = uid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photo = photo;
        this.phone = phone;
        this.email = email;
        this.type = type;
        this.state = state;
        this.token = token;
        this.status = status;
        this.date = date;
        this.key = key;
        this.ID = ID;
        this.address = address;
        this.membership = membership;
        this.expiry = expiry;
        this.rate = rate;
    }
    public User() {
        this.uid = "";
        this.firstname = "";
        this.lastname = "";
        this.photo = "";
        this.phone = "";
        this.email = "";
        this.type = "";
        this.token = "";
        this.state = 0;
        this.status = 0;
        this.date = "";
        this.key = "";
        this.ID = "";
        this.address = "";
        this.membership = 0;
        this.expiry = "";
        this.rate = 0;
    }
}
