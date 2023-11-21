package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Diagnosis implements Serializable {
    public String _id;
    public String category_id;
    public String name;
    public String treatment;

    public Diagnosis(String _id, String category_id, String name, String treatment) {
        this._id = _id;
        this.category_id = category_id;
        this.name = name;
        this.treatment = treatment;
    }
    public Diagnosis() {
        this._id = "";
        this.category_id = "";
        this.name = "";
        this.treatment = "";
    }
}
