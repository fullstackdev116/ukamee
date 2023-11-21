package com.ediattah.yakko.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CategoryListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Category> arrayList;

    public CategoryListAdapter(Context _context, ArrayList<Category> _arrayList) {
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
            view = inflater.inflate(R.layout.cell_category, null);
        }
        Category category = arrayList.get(i);
        TextView txt_amount = view.findViewById(R.id.txt_amount);
        TextView txt_name = view.findViewById(R.id.txt_name);
        ImageView img_photo = view.findViewById(R.id.img_photo);
        Glide.with(context).load(category.photo).apply(new RequestOptions().placeholder(R.drawable.ic_default_health).fitCenter()).into(img_photo);
        txt_name.setText(category.name);
        View finalView = view;
        MyUtils.mDatabase.child(MyUtils.tbl_question).orderByChild("category_id").equalTo(category._id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int amount = 0;
                if (snapshot.exists()) {
                    amount = (int)snapshot.getChildrenCount();
                }
                txt_amount.setText("[ " + String.valueOf(amount) + " questions ]");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

}
