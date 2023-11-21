package com.ediattah.yakko.Model;
//
import java.io.Serializable;

public class Transaction implements Serializable {
    public String _id;
    public String user_id;
    public String amount;
    public String method;
    public String transactionid;
    public String merchantid;
    public String uniqueid;
    public String pay_link;
    public String status;
    public String date;

    public Transaction(String _id, String user_id, String amount, String method, String transactionid, String merchantid, String uniqueid, String pay_link, String status, String date) {
        this._id = _id;
        this.user_id = user_id;
        this.amount = amount;
        this.method = method;
        this.transactionid = transactionid;
        this.merchantid = merchantid;
        this.uniqueid = uniqueid;
        this.pay_link = pay_link;
        this.status = status;
        this.date = date;
    }
    public Transaction() {
        this._id = "";
        this.user_id = "";
        this.amount = "";
        this.method = "";
        this.transactionid = "";
        this.merchantid = "";
        this.uniqueid = "";
        this.pay_link = "";
        this.status = "";
        this.date = "";
    }
}
