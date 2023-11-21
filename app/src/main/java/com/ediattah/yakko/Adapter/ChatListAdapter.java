package com.ediattah.yakko.Adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.designtoast.DesignToast;
import com.cocosw.bottomsheet.BottomSheet;
import com.ediattah.yakko.App;
import com.ediattah.yakko.ChatActivity;
import com.ediattah.yakko.ImageViewerActivity;
import com.ediattah.yakko.Model.MessageChat;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.MyUtils;
import com.ediattah.yakko.R;
import com.ediattah.yakko.Service.DownloadTask;
import com.ediattah.yakko.Service.OnTaskResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;


public class ChatListAdapter extends BaseAdapter {
    ArrayList<MessageChat> arrayList;
    ChatActivity context;
    public int index_update = -1;
    public String roomId;

    public ChatListAdapter(ChatActivity _context, ArrayList<MessageChat> _arrayList) {
        context = _context;
        this.arrayList = _arrayList;
    }
    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final MessageChat message = arrayList.get(i);
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean flag_me = message.sender_id.equals(MyUtils.cur_user.uid);

        if (flag_me) {
            view = inflater.inflate(R.layout.cell_chat_right, null);
        } else {
            view = inflater.inflate(R.layout.cell_chat_left, null);
        }
        RelativeLayout ly_message = view.findViewById(R.id.ly_message);
        RelativeLayout ly_cover = view.findViewById(R.id.ly_cover);
        TextView txt_message = view.findViewById(R.id.txt_message);
        final TextView txt_seen = view.findViewById(R.id.txt_seen);
        final TextView txt_time = view.findViewById(R.id.txt_time);
        final ImageView img_photo = view.findViewById(R.id.img_photo);
        final ImageView img_pic = view.findViewById(R.id.img_pic);
        final VoicePlayerView voicePlayerView = view.findViewById(R.id.voicePlayerView);
        voicePlayerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new BottomSheet.Builder(context,R.style.BottomSheet_StyleDialog).title("Actions").sheet(R.menu.bottom_sheet_audio).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.delete:
                                if (message.sender_id.equals(MyUtils.cur_user.uid)) {
                                    context.delete_message(message);
                                    DesignToast.makeText(context, "Voice message has been deleted", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                } else {
                                    DesignToast.makeText(context, "You are not allowed", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                                }
                                break;
                        }
                    }
                }).show();
                return false;
            }
        });
        if (flag_me && message.seen) {
            txt_seen.setVisibility(View.VISIBLE);
        } else {
            txt_seen.setVisibility(View.INVISIBLE);
        }
        if (!flag_me && !message.seen) {
            txt_seen.setVisibility(View.VISIBLE);
            MyUtils.mDatabase.child(MyUtils.tbl_chat).child(roomId).child("messages").child(message._id).child("seen").setValue(true);
        }
        txt_message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new BottomSheet.Builder(context,R.style.BottomSheet_StyleDialog).title("Actions").sheet(R.menu.bottom_sheet_text).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.copy_text:
                                MyUtils.copy_text(context, message.message);
                                DesignToast.makeText(context, "Message has been copied to clipboard", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                break;
                            case R.id.share_text:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Message from chat");
                                intent.putExtra(Intent.EXTRA_TEXT, message.message);
                                context.startActivity(Intent.createChooser(intent, "Share via"));
                                break;
                            case R.id.delete:
                                if (message.sender_id.equals(MyUtils.cur_user.uid)) {
                                    context.delete_message(message);
                                    DesignToast.makeText(context, "Message has been deleted", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                } else {
                                    DesignToast.makeText(context, "You are not allowed", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                                }
                                break;
                        }
                    }
                }).show();
                return false;
            }
        });
        img_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageViewerActivity.class);
                intent.putExtra("url", message.file);
                context.startActivity(intent);
            }
        });
        img_pic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new BottomSheet.Builder(context,R.style.BottomSheet_StyleDialog).title("Actions").sheet(R.menu.bottom_sheet_img).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.download:
                                do_download(message.file);
                                break;
                            case R.id.copy_url:
                                MyUtils.copy_text(context, message.file);
                                DesignToast.makeText(context, "Image URL has been copied to clipboard", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                break;
                            case R.id.share_url:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Image URL");
                                intent.putExtra(Intent.EXTRA_TEXT, message.file);
                                context.startActivity(Intent.createChooser(intent, "Share via"));
                                break;
                            case R.id.delete:
                                if (message.sender_id.equals(MyUtils.cur_user.uid)) {
                                    context.delete_message(message);
                                    DesignToast.makeText(context, "Image has been deleted", Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
                                } else {
                                    DesignToast.makeText(context, "You are not allowed", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                                }
                                break;
                        }
                    }
                }).show();
                return false;
            }
        });
        if (message.message.length()>0) {
            txt_message.setVisibility(View.VISIBLE);
            img_pic.setVisibility(View.GONE);
            voicePlayerView.setVisibility(View.GONE);
            txt_message.setText(message.message);
        } else {
            if (message.file.length() == 0) {
                ly_cover.setVisibility(View.GONE);
                return view;
            }
            txt_message.setVisibility(View.GONE);
            if (message.file_type.equals("jpeg")) {
                img_pic.setVisibility(View.VISIBLE);
                voicePlayerView.setVisibility(View.GONE);
                Glide.with(context).load(message.file).apply(new RequestOptions()
                        .placeholder(R.drawable.default_pic).centerCrop().dontAnimate()).into(img_pic);
            } else if (message.file_type.equals("wav")) {
                ly_message.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                img_pic.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.VISIBLE);
                voicePlayerView.setTimingVisibility(false);
                SetAudioTask setAudioTask = new SetAudioTask(voicePlayerView);
                setAudioTask.execute(message.file);
            }
        }
        txt_time.setText(MyUtils.getTimeString(new Date(message.timestamp)));
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(message.sender_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()!=null) {
                    User user = dataSnapshot.getValue(User.class);
                    if (context != null) {
                        try {
                            Glide.with(context).load(user.photo).apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_avatar_white).centerCrop().dontAnimate()).into(img_photo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }
    private class SetAudioTask extends AsyncTask<String, Void, String> {
        private VoicePlayerView voicePlayerView;

        public SetAudioTask(VoicePlayerView voicePlayerView) {
            this.voicePlayerView = voicePlayerView;
        }

        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            voicePlayerView.setAudio(path);
            return null;
        }

        @Override
        protected void onPostExecute(final String result) {
            voicePlayerView.setTimingVisibility(true);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    void do_download(final String url) {
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Download");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        final String path = App.MY_IMAGE_PATH + File.separator + App.getTimestampString() + ".jpg";
        final DownloadTask downloadTask = new DownloadTask(context, path, new OnTaskResult() {
            @Override
            public void onTaskCompleted() {
                mProgressDialog.dismiss();
                DesignToast.makeText(context, "Downloaded in " + App.MY_IMAGE_PATH, Toast.LENGTH_LONG, DesignToast.TYPE_SUCCESS).show();
            }

            @Override
            public void onTaskStarted() {
                mProgressDialog.setMessage("Downloading image...");
                mProgressDialog.show();
            }

            @Override
            public void onTaskUpdated(Integer... progress) {

            }

            @SuppressLint("ShowToast")
            @Override
            public void onTaskError(String result) {
                mProgressDialog.dismiss();
                Toast.makeText(context, result, Toast.LENGTH_SHORT);
            }
        });
        downloadTask.execute(url);
    }
}
