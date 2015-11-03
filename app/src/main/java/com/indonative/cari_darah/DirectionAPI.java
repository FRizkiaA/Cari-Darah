package com.indonative.cari_darah;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.indonative.cari_darah.controller.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class DirectionAPI
{
    private static final String URL = "http://maps.googleapis.com/maps/api/directions/";
    private static final boolean USE_SENSOR = true;

    /* Required parameter */

    //Specifies start location
    private static final String PARAM_ORIGIN = "origin";

    //Specifies end location
    private static final String PARAM_DESTIONATION = "destination";

    //Indicates directions request comes from a device with a location sensor
    private static final String PARAM_SENSOR = "sensor";

    /* Optional parameter */

    //Specifies mode of transport
    private static final String PARAM_TRAVEL_MODE = "mode";
    //Travel modes
    public static final String TRAVEL_MODE_DRIVING = "driving";
    public static final String TRAVEL_MODE_BICYCLING = "bicycling";
    public static final String TRAVEL_MODE_TRANSIT = "transit";
    public static final String TRAVEL_MODE_WALKING = "walking";

    //Specifies unit system to use when displaying result
    private static final String PARAM_UNIT_SYSTEM = "units";
    //Unit system
    private static final String UNIT_SYSTEM_METRIC = "metric";
    private static final String UNIT_SYSTEM_IMPERIAL = "imperial";

    //Waypoint alter a route by routing it through the specified location(s)
    private static final String PARAM_WAYPOINTS = "waypoints";

    /*Specifies that the route using the supplied waypoints maybe optimized to provide the
    shortest possible route. If true, the Directions service will return the reordered
    waypoints in an waypoint_order field*/
    private static final String PARAM_OPTIMIZE_WAYPOINTS = "optimize";

    /*When set to true specifies that the Direction service may provide more than one
    route alternative in the response. Note that providing route alternatives may
    increase the response time from the server
     */
    private static final String PARAM_PROVIDE_ROUTE_ALTERNATIVES = "alternatives";

    /*Indicates that the calculated route(s) should avoid the indicated features.
    Currently, this parameter supports the following two arguments :
    tolls indicates that the calculated route should avoid toll roads/bridges.
    highway indicates that the calculated route should avoid highways.
     */
    private static final String PARAM_AVOID = "avoid";
    private static final String AVOID_HIGHWAYS = "highways";
    private static final String AVOID_TOLLS = "tolls";

    /*specifies the region code, specified as a ccTLD ("top-level domain") two-character value
    https://developers.google.com/maps/documentation/javascript/directions#DirectionsRegionBiasing
     */
    private static final String PARAM_REGION = "region";

    private LatLng origin;
    private LatLng destination;

    private Map<LatLng, Boolean> waypoints;
    private Map<String, String> optionalParams;

    public DirectionAPI(LatLng origin, LatLng destination)
    {
        if(origin == null || destination == null)
        {
            throw new NullPointerException("Origin and destination should not be null");
        }
        this.origin = origin;
        this.destination = destination;
        optionalParams = new HashMap<String, String>();
        waypoints = new HashMap<LatLng, Boolean>();
    }

    public void addWaypoint(LatLng waypoint, boolean stopOver)
    {
        waypoints.put(waypoint, stopOver);
    }

    public void removeWaypoint(LatLng waypoint)
    {
        waypoints.remove(waypoint);
    }

    public void setTravelMode(String mode)
    {
        optionalParams.put(PARAM_TRAVEL_MODE, mode);
    }

    public void setUnitSystem(String unitSystem)
    {
        put(PARAM_UNIT_SYSTEM, unitSystem);
    }

    public void optimizeWaypoints(boolean optimize)
    {
        put(PARAM_OPTIMIZE_WAYPOINTS, String.valueOf(optimize));
    }

    public void provideRouteAlternatives(boolean provide)
    {
        put(PARAM_PROVIDE_ROUTE_ALTERNATIVES, String.valueOf(provide));
    }

    public void avoid(boolean highways, boolean tolls)
    {
        if(!highways && !tolls)
        {
            put(PARAM_AVOID, null);
        }
        else if (highways && !tolls)
        {
            put(PARAM_AVOID, AVOID_HIGHWAYS);
        }
        else if(!highways)
        {
            put(PARAM_AVOID, AVOID_TOLLS);
        }
        else
        {
            put(PARAM_AVOID, AVOID_TOLLS + "|" + AVOID_HIGHWAYS);
        }
    }

    public void setRegion(String region)
    {
        put(PARAM_REGION, region);
    }

    private void put(String key, String value)
    {
        if(value == null)
        {
            optionalParams.remove(key);
        }
        else
        {
            optionalParams.put(key, value);
        }
    }

    public String constructQuery()
    {
        StringBuilder query = new StringBuilder(URL);
        query.append("json").append("?");
        query.append(PARAM_ORIGIN).append("=").append(origin.latitude).append(",").append(origin.longitude);
        query.append("&");
        query.append(PARAM_DESTIONATION).append("=").append(destination.latitude).append(",").append(destination.longitude);
        query.append("&");
        query.append(PARAM_SENSOR).append("=").append(USE_SENSOR);
        for(Map.Entry<String, String> entry : optionalParams.entrySet())
        {
            query.append("&");
            query.append(entry.getKey()).append("=").append(entry.getValue());
        }
        if(waypoints.size() > 0)
        {
            query.append("&");
            query.append(PARAM_WAYPOINTS);
            for(Map.Entry<LatLng, Boolean> entry : waypoints.entrySet())
            {
                if(!entry.getValue())
                {
                    query.append("via:");
                }
                LatLng value = entry.getKey();
                query.append(value.latitude).append(",").append(value.longitude);
                query.append("|");
            }
            query.deleteCharAt(query.length() - 1);
        }
        return query.toString();
    }

    public GoogleResponse execute()
    {
        GoogleResponse googleResponse;
        try
        {
            URL url = new URL(constructQuery());
            Log.e("URL GMAPS API JSON", url.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            if(httpURLConnection.getResponseCode() == 200)
            {
                JSONObject jsonObject = JSONParser.parse(httpURLConnection.getInputStream());
                googleResponse = new GoogleResponse(jsonObject.getString("status"));
                if(googleResponse.isOk())
                {
                    googleResponse.setJsonObject(jsonObject);
                }
            }
            else
            {
                googleResponse = new GoogleResponse(httpURLConnection.getResponseCode());
            }
        }
        catch (MalformedURLException e)
        {
            Log.e("ERROR MALFORMED URL EXC", e.getMessage(), e);
            googleResponse = new GoogleResponse(e);
        }
        catch (IOException e)
        {
            Log.e("ERROR IO EXCEPTION", e.getMessage(), e);
            googleResponse = new GoogleResponse(e);
        }
        catch (JSONException e)
        {
            Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
            googleResponse = new GoogleResponse(e);
        }
        return googleResponse;
    }
}
