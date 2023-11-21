package com.ediattah.yakko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ediattah.yakko.Adapter.CategoryListAdapter;
import com.ediattah.yakko.Model.BotCnt;
import com.ediattah.yakko.Model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class CategoryActivity extends BaseActivity {
    ArrayList<Category> arrayList = new ArrayList();
    ListView listView;
    String type;
    CategoryListAdapter adapter;
    ImageView img_bot;
    int cnt = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        type = getIntent().getStringExtra("type");
        setTitle(type + "  Categories");

        img_bot = findViewById(R.id.img_bot);
        listView = findViewById(R.id.listView);
        adapter = new CategoryListAdapter(this, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (arrayList.size() == 0) {
                    return;
                }
                Category category = arrayList.get(position);
                Intent intent = new Intent(CategoryActivity.this, QAActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }
        });
        loadSubCategory();
        loadBotUrl();
        loadBotToken();

        findViewById(R.id.btn_enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyUtils.cur_user.membership == 0) {
                    MyUtils.mDatabase.child(MyUtils.tbl_botcnt).orderByChild("patient_id").equalTo(MyUtils.cur_user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot datas:snapshot.getChildren()) {
                                    BotCnt botCnt = datas.getValue(BotCnt.class);
                                    botCnt._id = datas.getKey();
                                    if (botCnt.cnt == 0) {
                                        MyUtils.showAlert(CategoryActivity.this, "Warning", "Trial period has been expired.\n Please buy a membership in settings");
                                        return;
                                    }
                                    MyUtils.mDatabase.child(MyUtils.tbl_botcnt).child(botCnt._id).child("cnt").setValue(botCnt.cnt-1);
                                    Intent intent = new Intent(CategoryActivity.this, BotActivity.class);
                                    intent.putExtra("categories", arrayList);
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Date expiry = MyUtils.getDateFromString(MyUtils.cur_user.expiry);
                    Date today = new Date();
                    if (expiry.getTime() < today.getTime()) {
                        MyUtils.showAlert(CategoryActivity.this, "Warning", "Your membership period has been expired.\n Please renew your membership in settings");
                        return;
                    }
                    Intent intent = new Intent(CategoryActivity.this, BotActivity.class);
                    intent.putExtra("categories", arrayList);
                    startActivity(intent);
                }
            }
        });

        animation();
    }
    void loadBotUrl() {
        MyUtils.mDatabase.child(MyUtils.tbl_admin).child("healthBotUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    App.healthBotUrl = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    void loadBotToken() {
        MyUtils.mDatabase.child(MyUtils.tbl_admin).child("healthBotToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    App.healthBotToken = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    void animation() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                img_bot.animate().scaleY(1)
                        //just wanted to show you possible methods you can add more
                        .translationX(randomXPosition())
                        .alpha((float)(Math.random()))
                        .setStartDelay(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                animation();
                            }
                        })
                        .translationY(randomYPosition())
                        .setDuration(1000).start();
            }
        }, 100);
    }

    float randomXPosition() {
        return (float)(800*Math.random());
    }
    float randomYPosition() {
        return (float)(200*Math.random());
    }

    void loadSubCategory() {
        MyUtils.mDatabase.child(MyUtils.tbl_category).orderByChild("type").equalTo(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot datas:dataSnapshot.getChildren()) {
                        Category category = datas.getValue(Category.class);
                        category._id = datas.getKey();
                        arrayList.add(category);
                    }

                }
                if (arrayList.size() == 0) {
                    String[] listItems = {"No data"};
                    ArrayAdapter simpleAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView textView = (TextView) super.getView(position, convertView, parent);
                            textView.setTextColor(getResources().getColor(R.color.gray));
                            textView.setGravity(Gravity.CENTER);
                            return textView;
                        }
                    };
                    listView.setAdapter(simpleAdapter);
                } else {
                    adapter = new CategoryListAdapter(CategoryActivity.this, arrayList);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w( "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}