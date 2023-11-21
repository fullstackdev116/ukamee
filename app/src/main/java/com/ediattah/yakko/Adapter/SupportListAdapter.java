package com.ediattah.yakko.Adapter;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.App;
import com.ediattah.yakko.MainActivityPatient;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SupportListAdapter extends BaseAdapter {
    MainActivityPatient context;
    ArrayList<User> arrayList;

    public SupportListAdapter(MainActivityPatient _context, ArrayList<User> _arrayList) {
        context = _context;
        arrayList = _arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.cell_support, null);
        }
        User user = arrayList.get(i);
        TextView txt_title = view.findViewById(R.id.txt_title);
        TextView txt_name = view.findViewById(R.id.txt_name);
        RatingBar ratingBar = view.findViewById(R.id.rate);
        txt_name.setText(user.firstname + " " + user.lastname);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        txt_title.setText("");
        ratingBar.setRating(user.rate);
        Glide.with(context).load(user.photo).apply(new RequestOptions().placeholder(R.drawable.ic_avatar_gray).fitCenter()).into(img_photo);

        MyUtils.mDatabase.child(MyUtils.tbl_category).orderByChild("admin_id").equalTo(user.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot datas: snapshot.getChildren()) {
                        Category category = datas.getValue(Category.class);
                        category._id = datas.getKey();
                        txt_title.setText(category.name + " " + "Specialist");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

}
