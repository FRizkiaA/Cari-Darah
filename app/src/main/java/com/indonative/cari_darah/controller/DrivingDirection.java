package com.indonative.cari_darah.controller;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class DrivingDirection {
    private static final String TAG_ROUTES = "routes";

    private static final String TAG_WAYPOINT_ORDER = "waypoint_order";
    private static final String TAG_SUMMARY = "summary";
    private static final String TAG_BOUNDS = "bounds";
    private static final String TAG_LEGS = "legs";
    private static final String TAG_WARNINGS = "warnings";
    private static final String TAG_OVERVIEW_POLYLINE = "overview_polyline";
    private static final String TAG_COPYRIGHTS = "copyrights";
    private static final String TAG_BOUND_NORTH_EAST = "northeast";
    private static final String TAG_BOUND_SOUTH_WEST = "southwest";

    private String summary;
    private String copyrights;
    private ArrayList<LatLng> overviewPolyline;
    private ArrayList<LatLng> smoothPolyline;
    private ArrayList<String> htmlInstructions;

    private JSONArray warnings;
    private JSONArray waypointOrder;

    private LatLng boundNorthEast;
    private LatLng boundSouthWest;

    private ArrayList<DirectionLeg> directionLegs;

    private String json;

    public DrivingDirection(JSONObject response)
    {
        try
        {
            JSONArray jsonArray = response.getJSONArray(TAG_ROUTES);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            summary = jsonObject.getString(TAG_SUMMARY);
            copyrights = jsonObject.getString(TAG_COPYRIGHTS);
            overviewPolyline = decodePolyline(jsonObject.getJSONObject(TAG_OVERVIEW_POLYLINE).getString("points"));
            warnings = jsonObject.getJSONArray(TAG_WARNINGS);
            waypointOrder = jsonObject.getJSONArray(TAG_WAYPOINT_ORDER);
            JSONObject bounds = jsonObject.getJSONObject(TAG_BOUNDS);
            boundNorthEast = interpretLatLng(bounds.getJSONObject(TAG_BOUND_NORTH_EAST));
            boundSouthWest = interpretLatLng(bounds.getJSONObject(TAG_BOUND_SOUTH_WEST));
            JSONArray legs = jsonObject.getJSONArray(TAG_LEGS);
            directionLegs = new ArrayList<DirectionLeg>(legs.length());
            for(int i=0; i<legs.length(); i++)
            {
                directionLegs.add(new DirectionLeg(legs.getJSONObject(i)));
            }
            json = response.toString();
        }
        catch(JSONException e)
        {
            Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
        }
    }

    public String getSummary()
    {
        return summary;
    }

    public String getCopyrights()
    {
        return copyrights;
    }

    public ArrayList<LatLng> getOverviewPolyline()
    {
        return overviewPolyline;
    }

    public ArrayList<LatLng> getTotalPolyline()
    {
        if(smoothPolyline == null)
        {
            smoothPolyline = new ArrayList<LatLng>();
            for(DirectionLeg directionLeg : directionLegs)
            {
                for(DirectionStep directionStep : directionLeg.getDirectionSteps())
                {
                    smoothPolyline.addAll(directionStep.getPolyline());
                }
            }
        }
        return smoothPolyline;
    }

    public ArrayList<String> getHtmlInstructions()
    {
        if(htmlInstructions == null)
        {
            htmlInstructions = new ArrayList<String>();
            for (DrivingDirection.DirectionLeg directionLeg : directionLegs)
            {
                for (DrivingDirection.DirectionStep directionStep : directionLeg.getDirectionSteps())
                {
                    htmlInstructions.add(directionStep.getHtmlInstruction());
                }
            }
        }
        return htmlInstructions;
    }

    public JSONArray getWarnings()
    {
        return warnings;
    }

    public JSONArray getWaypointOrder()
    {
        return waypointOrder;
    }

    public LatLng getBoundNorthEast()
    {
        return boundNorthEast;
    }

    public LatLng getBoundSouthWest()
    {
        return boundSouthWest;
    }

    public ArrayList<DirectionLeg> getDirectionLegs()
    {
        return directionLegs;
    }

    public long getDistance()
    {
        long distance = 0L;
        for(DirectionLeg directionLeg : directionLegs)
        {
            distance += directionLeg.getDistance();
        }
        return distance;
    }

    public long getDuration()
    {
        long duration = 0L;
        for(DirectionLeg directionLeg : directionLegs)
        {
            duration += directionLeg.getDuration();
        }
        return duration;
    }

    public void addPolyline(Polyline polyline)
    {
        polyline.setPoints(this.overviewPolyline);
    }

    public static ArrayList<LatLng> decodePolyline(String encoded)
    {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len)
        {
            int b, shift = 0, result =0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while(b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while(b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }
        return poly;
    }

    private static LatLng interpretLatLng(JSONObject object)
    {
        try
        {
            return new LatLng(object.getDouble("lat"), object.getDouble("lng"));
        }
        catch(JSONException e)
        {
            Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
        }
        return null;
    }

    public String getJson()
    {
        return json;
    }

    public static class DirectionLeg
    {
        private long duration;
        private long distance;
        private LatLng startLocation;
        private LatLng endLocation;
        private String startAddress;
        private String endAddress;
        private JSONArray viaWaypoint;
        private ArrayList<DirectionStep> directionSteps;

        public DirectionLeg(JSONObject jsonObject)
        {
            try
            {
                distance = jsonObject.getJSONObject("distance").getLong("value");
                duration = jsonObject.getJSONObject("duration").getLong("value");
                startAddress = jsonObject.getString("start_address");
                endAddress = jsonObject.getString("end_address");
                viaWaypoint = jsonObject.getJSONArray("via_waypoint");
                startLocation = DrivingDirection.interpretLatLng(jsonObject.getJSONObject("start_location"));
                endLocation = DrivingDirection.interpretLatLng(jsonObject.getJSONObject("end_location"));
                JSONArray steps = jsonObject.getJSONArray("steps");
                directionSteps = new ArrayList<DirectionStep>(steps.length());
                for(int i=0; i<steps.length(); i++)
                {
                    directionSteps.add(new DirectionStep(steps.getJSONObject(i)));
                }

            }
            catch(JSONException e)
            {
                Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
            }
        }

        public long getDuration()
        {
            return duration;
        }

        public long getDistance()
        {
            return distance;
        }

        public LatLng getStartLocation()
        {
            return startLocation;
        }

        public LatLng getEndLocation()
        {
            return endLocation;
        }

        public String getStartAddress()
        {
            return startAddress;
        }

        public String getEndAddress()
        {
            return endAddress;
        }

        public JSONArray getViaWaypoint()
        {
            return viaWaypoint;
        }

        public ArrayList<DirectionStep> getDirectionSteps()
        {
            return directionSteps;
        }
    }

    public static class DirectionStep
    {
        private String htmlInstruction;
        private long duration;
        private long distance;
        private LatLng startLocation;
        private LatLng endLocation;
        private String travelMode;
        private String polyline;
        private ArrayList<LatLng> polylinePoints;

        public DirectionStep(JSONObject jsonObject)
        {
            try
            {
                htmlInstruction = jsonObject.getString("html_instructions");
                duration = jsonObject.getJSONObject("duration").getLong("value");
                distance = jsonObject.getJSONObject("distance").getLong("value");
                JSONObject startLocation = jsonObject.getJSONObject("start_location");
                this.startLocation = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                JSONObject endLocation = jsonObject.getJSONObject("end_location");
                this.endLocation = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
                travelMode = jsonObject.getString("travel_mode");
                polyline = jsonObject.getJSONObject("polyline").getString("points");
            }
            catch (JSONException e)
            {
                Log.e("ERROR JSON EXCEPTION", e.getMessage(), e);
            }
        }

        public String getHtmlInstruction()
        {
            return htmlInstruction;
        }

        public long getDuration()
        {
            return duration;
        }

        public long getDistance()
        {
            return distance;
        }

        public LatLng getStartLocation()
        {
            return startLocation;
        }

        public LatLng getEndLocation()
        {
            return endLocation;
        }

        public String getTravelMode()
        {
            return travelMode;
        }

        public ArrayList<LatLng> getPolyline()
        {
            if(polylinePoints == null)
            {
                polylinePoints = decodePolyline(polyline);
            }
            return polylinePoints;
        }
    }
}
