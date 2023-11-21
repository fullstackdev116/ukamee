package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//

import java.io.Serializable;
import java.util.ArrayList;

public class History implements Serializable {
    public String _id;
    public String category;
    public ArrayList<QA> QAs;
    public String diagnosis;
    public String treatment;
    public String date;
    public String patient_id;
    public String admin_id;

    public History(String _id, String category, ArrayList<QA> QAs, String diagnosis, String treatment, String date, String patient_id, String admin_id) {
        this._id = _id;
        this.category = category;
        this.QAs = QAs;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.date = date;
        this.patient_id = patient_id;
        this.admin_id = admin_id;
    }
    public History() {
        this._id = "";
        this.category = "";
        this.QAs = new ArrayList<>();
        this.diagnosis = "";
        this.treatment = "";
        this.date = "";
        this.patient_id = "";
        this.admin_id = "";
    }
}
