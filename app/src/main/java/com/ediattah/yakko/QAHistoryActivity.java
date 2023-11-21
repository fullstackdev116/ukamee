package com.ediattah.yakko;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ediattah.yakko.Model.History;
import com.ediattah.yakko.Model.QA;
import com.ediattah.yakko.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class QAHistoryActivity extends BaseActivity {
    User user;
    LinearLayout ly_cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qahistory);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        user = (User) getIntent().getSerializableExtra("user");
        setTitle(user.firstname + " " + user.lastname + "'s QA History");

        ly_cover = findViewById(R.id.ly_cover);
        loadQAs();

    }
    void loadQAs() {
        ly_cover.removeAllViews();
        MyUtils.mDatabase.child(MyUtils.tbl_history).orderByChild("admin_id").equalTo(MyUtils.cur_user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        History history = datas.getValue(History.class);
                        history._id = datas.getKey();
                        if (history.patient_id.equals(user.uid)) {
                            LayoutInflater inflater = LayoutInflater.from(QAHistoryActivity.this);
                            View view = inflater.inflate(R.layout.cell_qa_history, null);
                            LinearLayout ly_cover_qa = view.findViewById(R.id.ly_cover_qa);
                            TextView txt_date = view.findViewById(R.id.txt_date);
                            txt_date.setText(history.date);
                            TextView txt_diagnosis = view.findViewById(R.id.txt_diagnosis);
                            TextView txt_treatment = view.findViewById(R.id.txt_treatment);
                            txt_diagnosis.setText(history.diagnosis);
                            txt_treatment.setText(history.treatment);
                            ly_cover_qa.removeAllViews();

                            for (QA qa:history.QAs) {
                                LayoutInflater inflater1 = LayoutInflater.from(QAHistoryActivity.this);
                                View view1 = inflater1.inflate(R.layout.cell_qa, null);
                                TextView txt_question = view1.findViewById(R.id.txt_question);
                                TextView txt_answer = view1.findViewById(R.id.txt_answer);
                                txt_question.setText("Q: " + qa.q);
                                txt_answer.setText("A: " + qa.a);
                                ly_cover_qa.addView(view1);
                            }
                            ly_cover.addView(view);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}