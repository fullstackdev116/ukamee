package com.ediattah.yakko.Service;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.ediattah.yakko.App;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.Sink;
import okio.Timeout;

public class getBotRequest {
    private RequestQueue queue;
    private String APIkey = "c7BlMxKG7wspfGz0";
    private String brainID = "176492";
    private String reply;
    private char[] illegalChars = {'#', '<', '>', '$', '+', '%', '!', '`', '&',
            '*', '\'', '\"', '|', '{', '}', '/', '\\', ':', '@'};

    public getBotRequest(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String reply);
    }

    private String formatMessage(String message){

        message = message.replace(' ', '-');
        for(char illegalChar : illegalChars){
            message = message.replace(illegalChar, '-');
        }
        return message;
    }

    public void getResponse1(String message, final VolleyResponseListener volleyResponseListener){
//        message = formatMessage(message);

        MediaType JSON = MediaType.parse("application/json");
        Map<String, JSONArray> params = new HashMap<String, JSONArray>();
        String[] s = new String[1];
        JSONArray jsonArray = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            object.put("role", "user");
            object.put("content", message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonArray.put(object);
        params.put("messages", jsonArray);
        JSONObject parameter = new JSONObject(params);
        try {
            parameter.put("model", "gpt-3.5-turbo");
        }catch (Exception e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient.Builder()
                //default timeout for not annotated requests
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS).build();

        RequestBody body = RequestBody.create(JSON, parameter.toString());
        okhttp3.Request req = new okhttp3.Request.Builder()
                .url(App.healthBotUrl)
                .post(body)
                .addHeader("Authorization", App.healthBotToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("response", call.request().body().toString());
                volleyResponseListener.onError(call.request().body().toString());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String ss = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(ss);
                    JSONArray jarr = jsonObject.getJSONArray("choices");
                    JSONObject obj = jarr.getJSONObject(0);
                    JSONObject resObj = obj.getJSONObject("message");
                    String res = resObj.getString("content");

                    volleyResponseListener.onResponse(res);

                } catch (JSONException e) {
                    e.printStackTrace();
                    volleyResponseListener.onResponse("I am sorry but I have no answer for that.");
                }
//                Log.e("response", response.body().string());
            }
        });
    }
    public void getResponse(String message, final VolleyResponseListener volleyResponseListener){
        MediaType JSON = MediaType.parse("application/json");
        Map<String, JSONArray> params = new HashMap<String, JSONArray>();
        JSONArray jsonArray = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            object.put("role", "user");
            object.put("content", message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jsonArray.put(object);
        params.put("messages", jsonArray);
        JSONObject parameter = new JSONObject(params);
        try {
            parameter.put("model", "gpt-3.5-turbo");
            parameter.put("stream", true);
        }catch (Exception e) {
            e.printStackTrace();
        }
        ConnectionPool connectionPool = new ConnectionPool(5,
                60 * 60 * 1000, TimeUnit.MILLISECONDS);
        OkHttpClient client = new OkHttpClient.Builder()
                //default timeout for not annotated requests
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .writeTimeout(60000, TimeUnit.MILLISECONDS).build();

        RequestBody body = RequestBody.create(JSON, parameter.toString());
        okhttp3.Request req = new okhttp3.Request.Builder()
                .url(App.healthBotUrl)
                .post(body)
                .addHeader("Authorization", App.healthBotToken)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("response", call.request().body().toString());
                volleyResponseListener.onError(call.request().body().toString());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                String ss = response.body().string().substring(6);
                ss = ss.substring(0, ss.length()-16);
                String[] strArr = ss.split("\n\ndata: ");
                String res = "";
                try {
                    for (String str: strArr) {
                        JSONObject jsonObject = new JSONObject(str);
                        JSONArray jarr = jsonObject.getJSONArray("choices");
                        JSONObject obj = jarr.getJSONObject(0);
                        JSONObject resObj = obj.getJSONObject("delta");
                        if (resObj.has("content")) {
                            res += resObj.getString("content");
                        }
                    }
                    volleyResponseListener.onResponse(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                    volleyResponseListener.onResponse("I am sorry but I have no answer for that.");
                }

//                Log.e("response", response.body().string());
            }
        });
    }
    public void getResponse2(String message, final VolleyResponseListener volleyResponseListener) {
        try {
            URL obj = new URL(App.healthBotUrl);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", App.healthBotToken);
            con.setReadTimeout(60000);

            //Request Parameters you want to send
            String urlParameters = "{\n" +
                    "    \"stream\":true,\n" +
                    "    \"model\":\"gpt-3.5-turbo\",\n" +
                    "    \"messages\":[{\n" +
                    "        \"role\":\"user\",\n" +
                    "        \"content\":\"hi\"}]\n" +
                    "    \n" +
                    "}";

            // Send post request
            con.setDoOutput(true);// Should be part of code only for .Net web-services else no need for PHP
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Your server URL


    }
}
