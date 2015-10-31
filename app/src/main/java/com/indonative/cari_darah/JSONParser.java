package com.indonative.cari_darah;

import android.os.Debug;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class JSONParser
{
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
}
