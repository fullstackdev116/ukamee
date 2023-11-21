package com.ediattah.yakko.httpsModule;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

public class RestClient {
    private static final String HTTPS_STRING = "https";
    /**
     * https://developers.google.com/maps/documentation/geocoding/
     * https://developers.google.com/url-shortener/v1/getting_started?csw=1#shorten
     */

    private RestClient(){
    }
    private static RestClient instance = null;
    public static RestClient getInstance(){
        if(instance == null) {
            instance = new RestClient();
        }
        return instance;
    }

    public String postRequest1(String url, List<NameValuePair> nameValuePairs) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        HttpResponse response = null;
        String responseString = "";
        InputStream in;
        try {
            // Add your data
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            response = httpclient.execute(httppost);
            in = response.getEntity().getContent();
            responseString = convertStreamToString(in);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return responseString;
    }

    public String postRequest(String url, JSONObject jsonObject) {
        String responseString = "";
        HttpClient httpClient = HTTPUtils.getNewHttpClient(url.startsWith(HTTPS_STRING));
        HttpResponse response = null;
        InputStream in;
        URI newURI = URI.create(url);
        HttpPost postMethod = new HttpPost(newURI);

        try {
//            JSONObject postJSON = new JSONObject();
//            postJSON.put("longUrl", "http://www.google.com/");

            postMethod.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));
            postMethod.setHeader(HTTP.CONTENT_TYPE, "application/json");
//            postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");
//            postMethod.setEntity(new UrlEncodedFormEntity(builder.getNameValuePairs(), "UTF-8"));
            response = httpClient.execute(postMethod);
            in = response.getEntity().getContent();
            responseString = convertStreamToString(in);
        } catch (Exception e) {}
        return responseString;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

}
