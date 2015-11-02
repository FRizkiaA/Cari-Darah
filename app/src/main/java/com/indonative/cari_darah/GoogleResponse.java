package com.indonative.cari_darah;

import android.os.Debug;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class GoogleResponse
{
    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_ZERO_RESULT = "ZERO_RESULT";
    private static final String RESPONSE_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    private static final String RESPONSE_REQUEST_DENIED = "REQUEST_DENIED";
    private static final String RESPONSE_INVALID_REQUEST = "INVALID_REQUEST";

    public static final int OK = 0;
    public static final int ERROR_ZERO_RESULT = 10;
    public static final int ERROR_OVER_QUERY_LIMIT = 11;
    public static final int ERROR_REQUERST_DENIED = 12;
    public static final int ERROR_INVALID_REQUEST = 13;
    public static final int ERROR_UNKNOWN_RESPONSE = 14;

    public static final int ERROR_MALFORMED_URL = 15;
    public static final int ERROR_IO = 16;
    public static final int ERROR_JSON = 17;

    private int response = -1;
    private JSONObject jsonObject;

    public GoogleResponse(String response)
    {
        if(response.contentEquals(RESPONSE_OK))
        {
            this.response = OK;
        }
        else if(response.contentEquals(RESPONSE_ZERO_RESULT))
        {
            this.response = ERROR_ZERO_RESULT;
        }
        else if(response.contentEquals(RESPONSE_OVER_QUERY_LIMIT))
        {
            this.response = ERROR_OVER_QUERY_LIMIT;
        }
        else if(response.contentEquals(RESPONSE_REQUEST_DENIED))
        {
            this.response = ERROR_REQUERST_DENIED;
        }
        else if(response.contentEquals(RESPONSE_INVALID_REQUEST))
        {
            this.response = ERROR_INVALID_REQUEST;
        }
        else
        {
            Log.e("ERROR UNKNOWN RESPONSE", "Unknown response : " + response);
            this.response = ERROR_UNKNOWN_RESPONSE;
        }
    }

    public GoogleResponse(Exception e)
    {
        if(e instanceof MalformedURLException)
        {
            response = ERROR_MALFORMED_URL;
        }
        else if(e instanceof IOException)
        {
            response = ERROR_IO;
        }
        else if(e instanceof JSONException)
        {
            response = ERROR_JSON;
        }
    }

    public GoogleResponse(int response)
    {
        this.response = response;
    }

    public int getResponse()
    {
        return response;
    }

    public boolean isOk()
    {
        return response < 10;
    }

    public JSONObject getJsonObject()
    {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }
}
