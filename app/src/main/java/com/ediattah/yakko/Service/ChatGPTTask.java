package com.ediattah.yakko.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.ediattah.yakko.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatGPTTask extends AsyncTask<String, String, String> {
    private final String TAG = ChatGPTTask.class.getSimpleName();
    private OnBotResult listener;
    private Context context;
    private String message;

    public ChatGPTTask(Context context, String message, OnBotResult listener) {
        this.context = context;
        this.listener = listener;
        this.message = message;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        listener.onTaskStarted();
    }
    @Override
    protected void onPostExecute(String err) {
        super.onPostExecute(err);
        if (err == null) {
            listener.onTaskCompleted();
        } else {
            listener.onTaskError(err);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
//            String message = strings[0];

            URL url = new URL(App.healthBotUrl);
            Log.d(TAG, "Authorization Header: " +url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "Authorization Header: " +conn);
            conn.setRequestMethod("POST");
            String authHeader = App.healthBotToken;
            conn.setConnectTimeout(60000);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", authHeader);

            conn.setDoOutput(true);
            Log.d(TAG, "Authorization Header: " + authHeader);
            String requestBody = "{\n" +
                    "    \"stream\":true,\n" +
                    "    \"model\":\"gpt-3.5-turbo\",\n" +
                    "    \"messages\":[{\n" +
                    "        \"role\":\"user\",\n" +
                    "        \"content\":\""+message+"\"}]\n" +
                    "    \n" +
                    "}";

            conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                publishProgress(line);
            }

            bufferedReader.close();
            inputStream.close();
            conn.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return e.getMessage();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        String response = values[0];
        if (response.length() > 13) {  // data: [DONE]
            String ss = response.substring(5);
            String res = "";
            try {
                JSONObject jsonObject = new JSONObject(ss);
                JSONArray jarr = jsonObject.getJSONArray("choices");
                JSONObject obj = jarr.getJSONObject(0);
                JSONObject resObj = obj.getJSONObject("delta");
                if (resObj.has("content")) {
                    res = resObj.getString("content");
                    if (res.length() > 0) {
                        listener.onTaskUpdated(res);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                listener.onTaskError(e.getMessage());
            }
        }
    }
}