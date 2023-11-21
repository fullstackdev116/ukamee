package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class BotCnt implements Serializable {
    public String _id;
    public String patient_id;
    public int cnt;

    public BotCnt(String _id, String patient_id, int cnt) {
        this._id = _id;
        this.patient_id = patient_id;
        this.cnt = cnt;
    }
    public BotCnt() {
        this._id = "";
        this.patient_id = "";
        this.cnt = 0;
    }
}
