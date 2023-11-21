package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class QA implements Serializable {
    public String q;
    public String a;

    public QA(String q, String a) {
        this.q = q;
        this.a = a;
    }
    public QA() {
        this.q = "";
        this.a = "";
    }
}
