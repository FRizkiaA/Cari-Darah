package com.indonative.cari_darah.controller;

import android.os.Debug;
import android.util.Log;

/*import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;*/
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URLEncodedUtils;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.client.HttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.util.List;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class JSONParser
{
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    public static JSONObject parse(InputStream inputStream)
    {
        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                sb.append(line).append("\n");
            }
            inputStream.close();
            return new JSONObject(sb.toString());
        }
        catch(UnsupportedEncodingException e)
        {
            Log.e("ERROR UNSUPPORT ENCODE", e.getMessage(), e);
        }
        catch(IOException e)
        {
            Log.e("ERROR IO EXCEPTION", e.getMessage(), e);
        }
        catch(JSONException e)
        {
            Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
        }
        return null;
    }

    public JSONObject makeHttpRequest(String url, String method,  List<NameValuePair> params) {

        // Making HTTP request
        try {
            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                //DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
                //DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpClient httpClient = HttpClientBuilder.create().build();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            Log.e("JSON ERROR 1", "JSON CONTENT : " + json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            Log.e("JSON ERROR 2", "JSON CONTENT : " + json);
        }

        // return JSON String
        return jObj;

    }
}
