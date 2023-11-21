package com.ediattah.yakko;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.ediattah.yakko.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyUtils {
    public static String fbServerKey = "AAAA_plEUrw:APA91bFelSJki_HT44IiTQlJ4pKHSfhAkBrdTKYcxM5L2mKjUf5DvsTC_6camDqJudQ3cfUPeMdJEN1cHbv-BRGSdtpiazqCzL5dUxYBTGg_sQpdU5C1CJ5A8WqdrX5e0VX1AnUqy48W";

    public static FirebaseAuth auth = FirebaseAuth.getInstance();
    public static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    public static StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    //    public static FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    public static FirebaseUser mUser = auth.getCurrentUser();
    public static String tbl_user = "tbl_user";
    public static String tbl_category = "tbl_category";
    public static String tbl_question = "tbl_question";
    public static String tbl_answer = "tbl_answer";
    public static String tbl_diagnosis = "tbl_diagnosis";
    public static String tbl_history = "tbl_history";
    public static String tbl_chat = "tbl_chat";
    public static String tbl_admin = "tbl_admin";
    public static String tbl_friend = "tbl_friend";
    public static String tbl_transaction = "tbl_transaction";
    public static String tbl_botcnt = "tbl_botcnt";
    public static String tbl_feedback = "tbl_feedback";
    public static String tbl_vcall = "tbl_vcall";


    public static String storage_user = "user/";
    public static String storage_car = "car/";
    public static String storage_chat = "chat/";
    public static String storage_driving_license = "driving_license/";

    static Pattern emailPattern = Pattern.compile("[a-zA-Z0-9[!#$%&'()*+,/\\-_\\.\"]]+@[a-zA-Z0-9[!#$%&'()*+,/\\-_\"]]+\\.[a-zA-Z0-9[!#$%&'()*+,/\\-_\"\\.]]+");

    public static int geo_radius = 1000; // m
    public static float goal_points = 100;
    public static float driver_fee = 0;
    public static Location cur_location;
    public static String cur_address;
    public static User cur_user;
//    public static Agent cur_agent;
    public static String PHONE = "phone";
    public static String TYPE = "type";
    public static String MERCHANTID = "MERCHANTID";
    public static String type_health = "Health";
    public static String type_mental = "Mental Strength";

    public static String PATIENT = "PATIENT";
    public static String ADMIN = "ADMIN";

    public static String PUSH_CHAT = "PUSH_CHAT";
    public static String PUSH_RIDE = "PUSH_RIDE";


    public static String getFormattedNumberString(float val) {
        String ss = NumberFormat.getNumberInstance(Locale.US).format(val);
//        return String.format("%, f", val);
        return ss;
    }
    public static void sendDriverNotification(Context context, String title, String body, int resID) {
        int NOTIFICATION_ID = 234;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), resID);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setLargeIcon(icon)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.BLUE)
                .setLights(Color.BLUE, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(body);


        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
    public static String getCurrentDateTimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatter.format(new Date(System.currentTimeMillis()));
    }
    public static boolean isValidEmail(String email)
    {
        Matcher m = emailPattern.matcher(email);
        return m.matches();
    }
    public static void showAlert(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
    public static Date getNextMonth(Date date)
    {
        int days = 30;
        switch(date.getMonth()) {
            case 0:
                days = 31;
                break;
            case 2:
                days = 31;
                break;
            case 4:
                days = 31;
                break;
            case 6:
                days = 31;
                break;
            case 7:
                days = 31;
                break;
            case 9:
                days = 31;
                break;
            case 11:
                days = 31;
                break;
            default:
                break;
        }
        return addDays(date, days);
    }

    public static void showTextViewMessage(TextView textView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setVisibility(View.GONE);
            }
        }, 3000);
    }
    public static Boolean isEmptyEditText(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            return true;
        } else {
            return false;
        }
    }
    public static String getDeviceToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("DEVICE_TOKEN", "");
    }
    public Bitmap createCustomMarker(Context context, int layout) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layout, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(20, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);
        return bitmap;
    }


    public static String getDistanceStr(long distance) {
        if (distance > 1000) {
            return String.valueOf((float)distance/1000)+"Km";
        } else {
            return String.valueOf(distance) + "m";
        }
    }
    public static String getTimestampString()
    {
        long tsLong = System.currentTimeMillis();
        String ts =  Long.toString(tsLong);
        return ts;
    }

    public static String getDateString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        String sdt = df.format(date);
        return sdt;
    }
    public static Date getDateFromString(String dateStr) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        try {
            date = df.parse(dateStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date;
    }
    public static String getTimeString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        return formatter.format(date);
    }
    public static String getRandom6NumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
    public static String getRandomStringUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    public static void setAlarm(Context context, Date date, Intent intent) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,intent, PendingIntent.FLAG_ONE_SHOT);

        Calendar c = Calendar.getInstance();//TimeZone.getTimeZone("GMT"));
        date.setHours(7);
        date.setMinutes(0);
        c.setTime(date);
        c.set(Calendar.SECOND, 0);

        am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);

    }
    public static String getChatUserId(String roomId) {
        String user_id;
        int index = roomId.indexOf(MyUtils.cur_user.uid);
        if (index == 0) {
            user_id = roomId.substring(MyUtils.cur_user.uid.length());
        } else {
            user_id = roomId.substring(0, roomId.length()- MyUtils.cur_user.uid.length());
        }
        return user_id;
    }

    public static void copy_text(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
    }
    public static String getDurationStr(long seconds) {
        String str = "";
        int d, h, m, s;
        d = (int)seconds/(3600*24);
        h = ((int)seconds%(3600*24))/3600;
        m = ((int)seconds%(3600*24)%3600)/60+1;
        s = (((int)seconds%(3600*24)%3600))%60;
        if (d > 0) {
            if (d > 1) {
                str += String.valueOf(d)+"days ";
            } else {
                str += String.valueOf(d)+"day ";
            }
        }
        if (h > 0) {
            if (h > 1) {
                str += String.valueOf(h)+"hours ";
            } else {
                str += String.valueOf(h)+"hour ";
            }
        }
        if (m > 0) {
            if (m > 1) {
                str += String.valueOf(m)+"mins";
            } else {
                str += String.valueOf(m)+"min";
            }
        }
//        if (s > 0) str += String.valueOf(s)+"Seconds";
        return str;
    }
    public static String darkMapStyle = "[\n" +
            "  {\n" +
            "    \"featureType\": \"all\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#242f3e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"all\",\n" +
            "    \"elementType\": \"labels.text.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"lightness\": -80\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"administrative\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#746855\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"administrative.locality\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi.park\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#263c3f\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi.park\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#6b9a76\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"geometry.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#2b3544\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#9ca5b3\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.arterial\",\n" +
            "    \"elementType\": \"geometry.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#38414e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.arterial\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#212a37\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"geometry.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#746855\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#1f2835\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#f3d19c\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.local\",\n" +
            "    \"elementType\": \"geometry.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#38414e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.local\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#212a37\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"transit\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#2f3948\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"transit.station\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#17263c\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#515c6d\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"labels.text.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"lightness\": -20\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
    public static String lightMapStyle = " [\n" +
            "  {\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#242f3e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#746855\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"elementType\": \"labels.text.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#242f3e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"administrative.locality\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi.park\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#263c3f\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"poi.park\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#6b9a76\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#38414e\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#212a37\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#9ca5b3\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#746855\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"geometry.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#1f2835\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"road.highway\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#f3d19c\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"transit\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#2f3948\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"transit.station\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#d59563\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"geometry\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#17263c\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"labels.text.fill\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#515c6d\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"featureType\": \"water\",\n" +
            "    \"elementType\": \"labels.text.stroke\",\n" +
            "    \"stylers\": [\n" +
            "      {\n" +
            "        \"color\": \"#17263c\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
}
