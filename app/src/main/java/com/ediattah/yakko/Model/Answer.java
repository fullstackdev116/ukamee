package com.ediattah.yakko.Model;//package com.example.ujs.afterwork.com.ujs.rezoschool.Model;
//
import java.io.Serializable;

public class Answer implements Serializable {
    public String _id;
    public String question_id;
    public String answer;
    public String diagnosis;

    public Answer(String _id, String question_id, String answer, String diagnosis) {
        this._id = _id;
        this.question_id = question_id;
        this.answer = answer;
        this.diagnosis = diagnosis;
    }
    public Answer() {
        this._id = "";
        this.question_id = "";
        this.answer = "";
        this.diagnosis = "";
    }
}
