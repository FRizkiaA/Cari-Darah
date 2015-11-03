package com.indonative.cari_darah;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.indonative.cari_darah.controller.DrivingDirection;

import java.util.ArrayList;

/**
 * Created by Rizkia on 25/10/2015.
 */
public class DirectionsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener
{
    private GoogleMap mMap;
    private Marker mMarker;
    private LatLng dest;
    private DirectionTask dt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.directions_map);
        mapFragment.getMapAsync(this);

        dest = getIntent().getExtras().getParcelable("destination");

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            //Enabling MyLocation Layer of Google Map
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            //Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null)
            {
                onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 10000, 20, this);

            LatLng start = new LatLng(location.getLatitude(), location.getLongitude());
            addMarkerCurrent(start);
            addMarkerPMI(dest);

            //Showing the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

            //Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            dt = new DirectionTask();
            dt.execute(dest);
        }
    }

    public void addMarkerCurrent(LatLng start)
    {
        if(mMarker != null)
        {
            mMarker.remove();
        }
        Log.e("ERROR", "INSIDE onMapReady() before add the first marker");
        mMarker = mMap.addMarker(new MarkerOptions().position(start).title("Your First Marker"));
        Log.e("ERROR", "INSIDE onMapReady() after adding MARKER");
    }

    public void addMarkerPMI(LatLng dest)
    {
        String nama_cabang = "PMI DKI Jakarta";
        String alamat = "Jl. Kramat Raya No.47, Senen, Kota Jakarta Pusat, Daerah Khusus Ibukota Jakarta 10450";

        Marker marker_pmi = mMap.addMarker(new MarkerOptions()
                .position(dest)
                .title(nama_cabang)
                .snippet(alamat)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

    }

    @Override
    public void onLocationChanged(Location location)
    {
        //Creating a LatLng object for the current location
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

        addMarkerCurrent(current);

        //Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));

        //Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        /*if(!dt.isCancelled())
        {
            dt.cancel(true);
            dt.execute(dest);
        }*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    class DirectionTask extends AsyncTask<LatLng, Void, ArrayList<LatLng>>
    {
        LatLng start;
        ArrayList<String> htmlInstructions;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            start = new LatLng(mMarker.getPosition().latitude, mMarker.getPosition().longitude);
        }

        @Override
        protected ArrayList<LatLng> doInBackground(LatLng... params)
        {
            Log.e("DALEM DO IN BACKGROUND", "DALAM DO IN BACKGROUND");
            LatLng dest = new LatLng(params[0].latitude, params[0].longitude);
            DirectionAPI directionAPI = new DirectionAPI(start, dest);
            GoogleResponse googleResponse = directionAPI.execute();
            if (googleResponse.isOk())
            {
                DrivingDirection drivingDirection = new DrivingDirection(googleResponse.getJsonObject());
                ArrayList<LatLng> polyline = drivingDirection.getTotalPolyline();
                htmlInstructions = drivingDirection.getHtmlInstructions();
                return polyline;
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> polyline)
        {
            super.onPostExecute(polyline);
            if(polyline != null)
            {
                Log.e("INSIDE POST EXECUTE", "INSIDE POST EXECUTE");
                mMap.addPolyline(new PolylineOptions().addAll(polyline).width(6).color(Color.RED));
                StringBuilder sb = new StringBuilder();
                int i = 1;
                for (String instruct : htmlInstructions)
                {
                    sb.append("<b>"+i+".</b> ").append(instruct).append("<br/>");
                    i++;
                }
                TextView hd = (TextView) findViewById(R.id.html_directions);
                hd.setText(Html.fromHtml(sb.toString()));
                this.cancel(true);
            }
        }
    }
}
