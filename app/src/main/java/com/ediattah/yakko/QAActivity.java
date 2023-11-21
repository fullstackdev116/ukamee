package com.ediattah.yakko;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.Model.Answer;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.Model.Diagnosis;
import com.ediattah.yakko.Model.History;
import com.ediattah.yakko.Model.QA;
import com.ediattah.yakko.Model.Question;
import com.ediattah.yakko.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class QAActivity extends BaseActivity {
    private final static int MY_PERMISSION_STORAGE = 201;
    public final static int MY_PERMISSION_CALL = 301;

    Category category;
    ArrayList<Question> arrayList = new ArrayList<>();
    LinearLayout ly_qa, ly_result, ly_cover;
    TextView txt_question, txt_num;
    RadioGroup rg_answer;
    Button btn_next;
    int cur_order = 1;
    String diagnosis = "", cur_diag = "", cur_answer = "";
    User admin;
    ArrayList<QA> QAs = new ArrayList<>();
    CircleImageView img_photo;
    TextView txt_name, txt_title;
    String call_number;
    RelativeLayout ly_support;
    TextView txt_support;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qaactivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Question and Answer");
        category = (Category) getIntent().getSerializableExtra("category");
        txt_question = findViewById(R.id.txt_question);
        ly_qa = findViewById(R.id.ly_qa);
        ly_cover = findViewById(R.id.ly_result_cover);
        ly_result = findViewById(R.id.ly_result);
        txt_num = findViewById(R.id.txt_num);
        rg_answer = findViewById(R.id.rg_answer);
        btn_next = findViewById(R.id.btn_next);
        txt_name = findViewById(R.id.txt_name);
        txt_title = findViewById(R.id.txt_title);
        img_photo = findViewById(R.id.img_photo);
        ly_support = findViewById(R.id.ly_support);
        txt_support = findViewById(R.id.txt_support);

        if (MyUtils.cur_user.membership == 0) {
            ly_support.setVisibility(View.GONE);
            txt_support.setVisibility(View.VISIBLE);
        } else {
            ly_support.setVisibility(View.VISIBLE);
            txt_support.setVisibility(View.GONE);
        }

        findViewById(R.id.btn_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(QAActivity.this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(CALL_PHONE);
                    ActivityCompat.requestPermissions(QAActivity.this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), MY_PERMISSION_CALL);
                    return;
                } else {
                    // call
                    App.dialNumber(admin.phone, QAActivity.this);
                }
            }
        });
        findViewById(R.id.btn_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(QAActivity.this, READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(QAActivity.this, WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ArrayList<String> arrPermissionRequests = new ArrayList<>();
                    arrPermissionRequests.add(WRITE_EXTERNAL_STORAGE);
                    arrPermissionRequests.add(READ_EXTERNAL_STORAGE);
                    ActivityCompat.requestPermissions(QAActivity.this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), MY_PERMISSION_STORAGE);
                    return;
                } else {
                    // call
                    createDirectory();
                    App.goToChatPage(QAActivity.this, admin.uid);
                }
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cur_order = 1;
                diagnosis = "";
                cur_diag = "";
                cur_answer = "";
                btn_next.setText("Next");
                loadQAs();
                QAs = new ArrayList<>();
                ly_qa.setVisibility(View.VISIBLE);
                ly_result.setVisibility(View.GONE);
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cur_diag.length() > 0) {
                    if (diagnosis.length() == 0) {
                        diagnosis = cur_diag;
                    } else {
                        diagnosis += "," + cur_diag;
                    }
                }
                QAs.add(new QA(txt_question.getText().toString(), cur_answer));

                if (btn_next.getText().toString().equals("Submit")) {
                    ly_qa.setVisibility(View.GONE);
                    ly_result.setVisibility(View.VISIBLE);
                    ly_cover.removeAllViews();
                    String[] separated = diagnosis.split(",");
                    String highest_id = highestRepeated(separated);

                    History history = new History("", category.name, QAs, "", "", MyUtils.getCurrentDateTimeString(), MyUtils.cur_user.uid, admin.uid);
                    if (diagnosis.length() == 0) {
                        TextView txt_name = new TextView(QAActivity.this);
                        txt_name.setText("No result");
                        txt_name.setTextColor(getResources().getColor(R.color.white));
                        txt_name.setTextSize(18);
                        ly_cover.addView(txt_name);
                        history.diagnosis = "";
                        history.treatment = "";
                        MyUtils.mDatabase.child(MyUtils.tbl_history).push().setValue(history);
                    } else {
                        MyUtils.mDatabase.child(MyUtils.tbl_diagnosis).child(highest_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Diagnosis diag = snapshot.getValue(Diagnosis.class);
                                    diag._id = snapshot.getKey();
                                    LinearLayout ly = (LinearLayout) LayoutInflater.from(QAActivity.this).inflate(R.layout.cell_qa_result, null);
                                    TextView txt_name = ly.findViewById(R.id.txt_name);
                                    TextView txt_treatment = ly.findViewById(R.id.txt_treatment);
                                    txt_name.setText("- " + diag.name);
                                    txt_treatment.setText(diag.treatment);
                                    ly_cover.addView(ly);
                                    history.diagnosis = diag.name;
                                    history.treatment = diag.treatment;
                                    MyUtils.mDatabase.child(MyUtils.tbl_history).push().setValue(history);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                } else {
                    cur_order ++;
                    showNextQuestion();
                }
            }
        });
        rg_answer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int ii = checkedId;
                RadioButton bb = findViewById(checkedId);
                cur_diag = bb.getTag().toString();
                cur_answer = bb.getText().toString();
                btn_next.setBackground(getResources().getDrawable(R.drawable.round_frame_primary));
                if (cur_order == arrayList.size()) {
                    btn_next.setBackground(getResources().getDrawable(R.drawable.round_frame_orange));
                }
                btn_next.setEnabled(true);
            }
        });
        loadQAs();
        loadAdminInfo();
    }

    void loadAdminInfo() {
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(category.admin_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    admin = snapshot.getValue(User.class);
                    admin.uid = snapshot.getKey();
                    txt_name.setText(admin.firstname + " " + admin.lastname);
                    txt_title.setText(category.name + " " + "Specialist");
                    Glide.with(QAActivity.this).load(admin.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_white).fitCenter()).into(img_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void showNextQuestion() {
        btn_next.setBackground(getResources().getDrawable(R.drawable.round_frame_gray));
        btn_next.setEnabled(false);
        if (cur_order == arrayList.size()) {
            btn_next.setText("Submit");
        }
        txt_num.setText(String.valueOf("Question " + cur_order));
        Question question = arrayList.get(cur_order-1);
        txt_question.setText(question.question);
        showAnswers(question._id);
    }
    void showAnswers(String question_id) {
        rg_answer.removeAllViews();
        MyUtils.mDatabase.child(MyUtils.tbl_answer).orderByChild("question_id").equalTo(question_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas:snapshot.getChildren()) {
                        Answer answer = datas.getValue(Answer.class);
                        answer._id = datas.getKey();
                        RadioButton rb_answer = (RadioButton) LayoutInflater.from(QAActivity.this).inflate(R.layout.cell_answer, null);
//            RadioButton rb_answer = v.findViewById(R.id.rb_answer);
                        rb_answer.setText(answer.answer);
                        rb_answer.setTag(answer.diagnosis);
                        rg_answer.addView(rb_answer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    void loadQAs() {
        MyUtils.mDatabase.child(MyUtils.tbl_question).orderByChild("category_id").equalTo(category._id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot datas:snapshot.getChildren()) {
                        Question question = datas.getValue(Question.class);
                        question._id = datas.getKey();
                        arrayList.add(question);
                    }
                }
                if (arrayList.size() == 0) {
                    DesignToast.makeText(QAActivity.this, "There are no questions in the category.", DesignToast.LENGTH_SHORT, DesignToast.TYPE_INFO).show();
                    finish();
                } else {
                    showNextQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    String highestRepeated(String[] str) {
        String[] sorted = Arrays.copyOf(str, str.length);
        Arrays.sort(sorted, 0, sorted.length, Comparator.reverseOrder());
        String currentString = sorted[0];
        String bestString = sorted[0];
        int maxCount = 1;
        int currentCount = 1;
        for (int i = 1 ; i < sorted.length ; i++) {
            if (currentString.equals(sorted[i])) {
                currentCount++;
            } else {
                if (maxCount < currentCount) {
                    maxCount = currentCount;
                    bestString = currentString;
                }
                currentString = sorted[i];
                currentCount = 1;
            }
        }
        if (currentCount > maxCount) {
            return currentString;
        }
        return bestString;
    }
    void createDirectory() {
        getExternalFilesDir(null);
        File assets = getExternalFilesDir("assets");
        if (!assets.exists()) {
            assets.mkdir();
        }
        App.MY_APP_PATH = assets.getAbsolutePath();
        File f3 = new File(App.MY_APP_PATH, "audio");
        if (!f3.exists()) {
            f3.mkdir();
        }
        App.MY_AUDIO_PATH = f3.getAbsolutePath();

        File pictures = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures", this.getResources().getString(R.string.app_name));
        if (!pictures.exists()) {
            pictures.mkdir();
        }
        App.MY_IMAGE_PATH = pictures.getAbsolutePath();

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] _permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, _permissions, grantResults);
        if (grantResults.length > 0) {
            if  (requestCode == MY_PERMISSION_STORAGE) {
                createDirectory();
                App.goToChatPage(QAActivity.this, admin.uid);

            } else if (requestCode == MY_PERMISSION_CALL) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    App.dialNumber(admin.phone, this);
                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                }
            }
        }
    }
}