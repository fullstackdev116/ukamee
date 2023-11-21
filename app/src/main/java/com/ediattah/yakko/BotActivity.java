package com.ediattah.yakko;

import static android.Manifest.permission.RECORD_AUDIO;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.designtoast.DesignToast;
import com.ediattah.yakko.Adapter.BotRecyclerAdapter;
import com.ediattah.yakko.Model.Category;
import com.ediattah.yakko.Model.MessageBot;
import com.ediattah.yakko.Model.User;
import com.ediattah.yakko.Service.ChatGPTTask;
import com.ediattah.yakko.Service.OnBotResult;
import com.ediattah.yakko.Service.getBotRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

public class BotActivity extends BaseActivity implements OnBotResult {
    private ArrayList<MessageBot> messages;
    private RecyclerView recyclerView;
    private BotRecyclerAdapter adapter;
    private ImageButton btn_send, btn_mic;
    private EditText msgInput;
    private getBotRequest request;
    LinearLayout ly_look;
    TextView txt_thinking;
    RecognitionListener listener;
    boolean isRecord = false;
    ArrayList<Category> categories = new ArrayList();
    public final static int MY_PERMISSION_STORAGE = 201;
    public final static int MY_PERMISSION_CALL = 301;
    public final static int MY_PERMISSION_MIC = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.app_name) + " Bot");
        categories = (ArrayList<Category>)getIntent().getSerializableExtra("categories");

        request = new getBotRequest(this);

        txt_thinking = findViewById(R.id.txt_thinking);
        recyclerView = findViewById(R.id.recyclerView);
        // Set RecyclerView layout manager.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // Set an animation
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        messages = new ArrayList<>();
        adapter = new BotRecyclerAdapter(this, messages);
        recyclerView.setAdapter(adapter);

        ly_look = findViewById(R.id.ly_look);
        btn_send = findViewById(R.id.msgButton);
        btn_mic = findViewById(R.id.micButton);
        msgInput = findViewById(R.id.msgInput);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msgInput.getText().toString();
                if(message.length() != 0){
                    messages.add(new MessageBot(true, message, null));
                    int newPosition = messages.size() - 1;
                    adapter.notifyItemInserted(newPosition);
                    recyclerView.scrollToPosition(newPosition);
                    msgInput.setText("");
                    new ChatGPTTask(BotActivity.this, message, BotActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            }
        });
        btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord = !isRecord;
                if (isRecord) {
                    msgInput.setText("");
                    msgInput.setHint("Try saying something");
                    record_start();
                } else {
                    record_stop();
                }
            }
        });
        ly_look.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.hideKeyboard(BotActivity.this);
                openCategoryDialog();
            }
        });
        sendWelcomeMessage();
        animation(0);
    }
    public void createDirectory() {
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
    public void openCategoryDialog() {
        final Dialog dlg = new Dialog(this);
        Window window = dlg.getWindow();
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);
        LinearLayout ly_cover = view.findViewById(R.id.ly_cover);
        ly_cover.removeAllViews();
        for (Category category:categories) {
            LayoutInflater inflater1 = LayoutInflater.from(BotActivity.this);
            View view1 = inflater1.inflate(R.layout.cell_category1, null);
            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(BotActivity.this, category.name, Toast.LENGTH_SHORT).show();
                    sendDoctorLookMessage(category);
                    dlg.dismiss();
                }
            });
            ImageView img_photo = view1.findViewById(R.id.img_photo);
            TextView txt_name = view1.findViewById(R.id.txt_name);
            txt_name.setText(category.name);
            Glide.with(this).load(category.photo).apply(new RequestOptions().placeholder(R.drawable.ic_default_health).fitCenter()).into(img_photo);
            ly_cover.addView(view1);
        }

        dlg.setCancelable(true);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(view);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dlg.show();
    }
    void animation(float alpha) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                ly_look.animate()
                        //just wanted to show you possible methods you can add more
                        .alpha(1-alpha)
                        .setStartDelay(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                animation(1-alpha);
                            }
                        })
                        .setDuration(1000).start();
            }
        }, 100);
    }
    SpeechRecognizer recognizer;
    Handler recog_handler = new Handler(Looper.getMainLooper());
    void init_recognizer() {
        if (recog_handler != null) recog_handler.removeCallbacksAndMessages(null);
        recog_handler = new Handler(Looper.getMainLooper());
        recog_handler.post(new Runnable() {
            @Override
            public void run() {
                // Code here will run in UI thread
                if(!SpeechRecognizer.isRecognitionAvailable(BotActivity.this)) {
                    DesignToast.makeText(BotActivity.this, "SpeechRecognizer is not available", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                    return;
                }
                recognizer = SpeechRecognizer
                        .createSpeechRecognizer(BotActivity.this);
                listener = new RecognitionListener() {
                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> voiceResults = results
                                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        isRecord = false;
                        btn_mic.setColorFilter(getResources().getColor(R.color.white));
                        btn_send.setEnabled(true);
                        if (voiceResults == null) {
                            msgInput.setHint("Didn't catch that. Try speaking again.");
                        } else {
                            msgInput.setText(voiceResults.get(0));
                        }
                    }

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        System.out.println("Ready for speech");
                    }

                    /**
                     *  ERROR_NETWORK_TIMEOUT = 1;
                     *  ERROR_NETWORK = 2;
                     *  ERROR_AUDIO = 3;
                     *  ERROR_SERVER = 4;
                     *  ERROR_CLIENT = 5;
                     *  ERROR_SPEECH_TIMEOUT = 6;
                     *  ERROR_NO_MATCH = 7;
                     *  ERROR_RECOGNIZER_BUSY = 8;
                     *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
                     *
                     * @param error code is defined in SpeechRecognizer
                     */
                    @Override
                    public void onError(int error) {
                        String msg_result = "";
                        System.err.println("Error listening for speech: " + error);
                        switch (error) {
                            case 1:
                                msg_result = "ERROR_NETWORK_TIMEOUT";
                                break;
                            case 2:
                                msg_result = "ERROR_NETWORK";
                                break;
                            case 3:
                                msg_result = "ERROR_AUDIO";
                                break;
                            case 4:
                                msg_result = "ERROR_SERVER";
                                break;
                            case 5:
                                msg_result = "ERROR_CLIENT";
                                break;
                            case 6:
                                msg_result = "ERROR_SPEECH_TIMEOUT";
                                break;
                            case 7:
                                msg_result = "ERROR_NO_MATCH";
                                msg_result = "Didn't catch your voice.";
                                break;
                            case 8:
                                msg_result = "ERROR_RECOGNIZER_BUSY";
                                break;
                            case 9:
                                msg_result = "ERROR_INSUFFICIENT_PERMISSIONS";
                                break;
                        }
                        DesignToast.makeText(BotActivity.this, msg_result, Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
                        msgInput.setHint("Type your message here");
                        isRecord = false;
                        btn_mic.setColorFilter(getResources().getColor(R.color.white));
                        btn_send.setEnabled(true);
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        msgInput.setText("");
                        System.out.println("Speech starting");
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onEndOfSpeech() {
                        // TODO Auto-generated method stub
//                        Toast.makeText(MicActivity.this, "Speech End", Toast.LENGTH_LONG).show();
//                        isRecord = false;
//                        img_mic.setVisibility(View.GONE);
                        btn_mic.setColorFilter(getResources().getColor(R.color.white));
                        btn_send.setEnabled(true);
//                        btn_mic.setBackgroundResource(R.drawable.ic_mic_circle_record);
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        // TODO Auto-generated method stub
                        final ArrayList<String> partialData = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                        if (partialData != null) {
                            String word = partialData.get(partialData.size() - 1);
                            if (word.replaceAll("\\s", "").isEmpty()) {
                                word = " ";
                            }
                            String txt = msgInput.getText().toString();
                            txt += word;
                            msgInput.setText(txt);
                        }


                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                        // TODO Auto-generated method stub

                    }
                };
                recognizer.setRecognitionListener(listener);
            }
        });
    }
    void record_start() {
        if (recognizer == null) {
            DesignToast.makeText(BotActivity.this, "SpeechRecognizer is not available", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.domain.app");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr");
        recognizer.startListening(intent);
        btn_mic.setColorFilter(getResources().getColor(R.color.red));
        btn_send.setEnabled(false);
    }
    void record_stop() {
        if (recognizer == null) {
            DesignToast.makeText(BotActivity.this, "SpeechRecognizer is not available", Toast.LENGTH_SHORT, DesignToast.TYPE_WARNING).show();
            return;
        }
        btn_mic.setColorFilter(getResources().getColor(R.color.white));
        btn_send.setEnabled(true);
    }
    void sendWelcomeMessage() {
        messages.add(new MessageBot(false, "Welcome to UKAMEE Bot!\n How can I help you?", null));
        int newPosition = messages.size() - 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(newPosition);
                recyclerView.scrollToPosition(newPosition);
            }
        });
    }
    void sendDoctorLookMessage(Category category) {
        messages.add(new MessageBot(true, "I am looking for a doctor who specialized in " + category.name, null));
        int newPosition = messages.size() - 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(newPosition);
                recyclerView.scrollToPosition(newPosition);
            }
        });
        txt_thinking.setVisibility(View.VISIBLE);
        txt_thinking.setText(getResources().getString(R.string.app_name) + " is thinking..");
        MyUtils.mDatabase.child(MyUtils.tbl_user).child(category.admin_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txt_thinking.setVisibility(View.GONE);
                MessageBot updateBot;
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    user.uid = snapshot.getKey();
                    String msg = user.firstname + " " + user.lastname + " specialized in " + category.name + ".\nEmail is " + user.email + ",\nPhone number is " + user.phone;
                    updateBot = new MessageBot(false, msg, user);

                } else {
                    String msg = "I am sorry but there is no doctor who specialized in " + category.name + ".";
                    updateBot = new MessageBot(false, msg, null);
                }
                messages.add(updateBot);
                int newPosition1 = messages.size() - 1;
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.smoothScrollToPosition(newPosition1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public boolean setPermission() {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            ArrayList<String> arrPermissionRequests = new ArrayList<>();
            arrPermissionRequests.add(RECORD_AUDIO);
            ActivityCompat.requestPermissions(this, arrPermissionRequests.toArray(new String[arrPermissionRequests.size()]), MY_PERMISSION_MIC);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        String permission_result = "";
        switch (requestCode) {
            case MY_PERMISSION_MIC:
                if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_result = "Microphone Access Allowed!";
                    init_recognizer();
                } else {
                    permission_result = "Microphone Access Denied!";
                }
                break;
            case MY_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createDirectory();
                    permission_result = "Storage Access Allowed! Please try again";
//                App.goToChatPage(QAActivity.this, admin.uid);
                }
                break;
            case MY_PERMISSION_CALL:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_result = "Call Access Allowed! Please try again";
//                    App.dialNumber(admin.phone, this);
                }
                break;
            default:
                permission_result = "Access Denied!";
        }

        DesignToast.makeText(BotActivity.this, permission_result, Toast.LENGTH_SHORT, DesignToast.TYPE_SUCCESS).show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (setPermission()) {
            init_recognizer();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

// ChatBot listener----------------
    String bot_reply = "";
    @Override
    public void onTaskCompleted() {
        bot_reply = "";
        int newPosition = messages.size() - 1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_thinking.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onTaskStarted() {
        bot_reply = "";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt_thinking.setText(getResources().getString(R.string.app_name) + " is thinking..");
                txt_thinking.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onTaskUpdated(String... response) {
        txt_thinking.setText(getResources().getString(R.string.app_name) + " is saying..");

        if (bot_reply.length() == 0) {
            bot_reply = response[0];
            MessageBot updateBot = new MessageBot(false, bot_reply, null);
            messages.add(updateBot);
            int newPosition = messages.size() - 1;
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.smoothScrollToPosition(newPosition);
        } else {
            bot_reply += response[0];
            MessageBot updateBot = new MessageBot(false, bot_reply, null);
            messages.set(messages.size()-1, updateBot);
            int newPosition = messages.size() - 1;
            adapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(newPosition);
            if (bot_reply.length() > 200) {
                if (response[0].endsWith(".")) {
                    bot_reply = "";
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onTaskError(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
    }
}