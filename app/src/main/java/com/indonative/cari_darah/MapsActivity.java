package com.indonative.cari_darah;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.indonative.cari_darah.controller.DrivingDirection;
import com.indonative.cari_darah.controller.JSONParser;
import com.indonative.cari_darah.model.PMIBranchModel;
import com.indonative.cari_darah.utilities.ConstantValues;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, GpsStatus.Listener
{
    private GoogleMap mMap;
    private Marker mMarker;
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private JSONParser jParser = new JSONParser();
    TextView text_jenis_rhesus, text_golongan_darah, text_jumlah_labu;
    JSONArray products;
    String golongan_darah, jenis_rhesus;
    int jumlah_labu;
    ArrayList<PMIBranchModel> PMIBranchList = new ArrayList<PMIBranchModel>();
    Map<String, Long> durationHashMap = new HashMap<String, Long>();
    Map<String, Long> distanceHashMap = new HashMap<String, Long>();
    private GetDirections gd;
    private String nama_cabang = "";
    private Location location = null;
    public static final int OUT_OF_SERVICE = 0;
    public static final int TEMPORARY_UNAVAILABLE = 1;
    public static final int AVAILABLE = 2;
    boolean mStatus;
    ArrayList<DistanceMap> al = new ArrayList<DistanceMap>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction_view);

        //getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        //showing status
        if(status != ConnectionResult.SUCCESS)
        {
            //Google Play Services are not available
            int request_code = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, request_code);
            dialog.show();
        }
        else
        {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            golongan_darah = getIntent().getExtras().getString("golongan_darah");
            /*text_golongan_darah = (TextView) findViewById(R.id.golongan_darah);
            text_golongan_darah.setText("Golongan Darah : " + golongan_darah);*/

            jumlah_labu = getIntent().getExtras().getInt("jumlah_labu", 1);
            /*text_jumlah_labu = (TextView) findViewById(R.id.jumlah_labu);
            text_jumlah_labu.setText("Jumlah Labu : " + jumlah_labu);*/

            jenis_rhesus = getIntent().getExtras().getString("jenis_rhesus");
            /*text_jenis_rhesus = (TextView) findViewById(R.id.jenis_rhesus);
            text_jenis_rhesus.setText("Jenis Rhesus : " + jenis_rhesus);*/

            //PMIBranchList.add(new PMIBranchModel("DKIJKT", 54, "PMI DKI Jakarta", 106.844868, -6.184805, "Jl. Kramat Raya No.47, Senen, Kota Jakarta Pusat, Daerah Khusus Ibukota Jakarta 10450"));
            //PMIBranchList.add(new PMIBranchModel("BDGKOTA", 50, "PMI Kota Bandung", 107.62405900, -6.90988900, "Jalan Aceh No. 79, Jalan Aceh, Bandung, Jawa Barat 40113"));
            //PMIBranchList.add(new PMIBranchModel("BDGKAB", 50, "Unit Donor Darah PMI Kabupaten Bandung", 107.57843600, -6.96208900, "Jl. Raya Soreang Kopo, Margahayu, Bandung, Jawa Barat 40225"));
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

           // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location loc = locationManager.getLastKnownLocation(provider);
            locationManager.addGpsStatusListener(this);
            //mStatus = false;
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(mStatus == true)//(loc != null)
            {
                location = loc;
                locationManager.requestLocationUpdates(provider, 5000, 10, this);
                Log.e("PROVIDER USED", "USING BEST PROVIDER");
            }
            else if(isNetworkEnabled)
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
                Log.e("PROVIDER USED", "USING NETWORK PROVIDER");
            }
            else if(isGPSEnabled)
            {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
                Log.e("PROVIDER USED", "USING GPS PROVIDER");
            }
            else //!isGPSEnabled && !isNetworkEnabled
            {
                //no network provider is enabled
                Log.e("ERROR PROVIDER", "NO PROVIDER ENABLED");
            }

            if(location != null) {
                Log.e("LOCATION PROVIDER", "PROVIDER : " + location.toString());
            }
            else
            {
                Log.e("LOCATION PROVIDER", "PROVIDER IS NULL");
            }

            new GetDirections().execute();
            if(location!=null)
            {
                Log.e("ERROR", "INSIDE onMapReady() LOCATION IS NOT NULL LAT :"+location.getLatitude() + " LNG :" +location.getLongitude());
                onLocationChanged(location);
            }

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if (!mMarker.getId().equals(marker.getId())) {
                        LatLng dest = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                        Intent i = new Intent(MapsActivity.this, DirectionsActivity.class);
                        i.putExtra("destination", dest);
                        startActivity(i);
                    }
                }
            });

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                        return true;
                    }
                    return false;
                }
            });

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());

                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current_location, 10);
                        mMap.animateCamera(update);
                        if (mMarker != null) {
                            mMarker.remove();
                        }
                        mMarker = mMap.addMarker(new MarkerOptions().position(current_location).title("Your New Position"));
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        //TextView tv_position = (TextView) findViewById(R.id.position);

        //Getting latitude of the current location
        latitude = location.getLatitude();

        //Getting longitude of the current location
        longitude = location.getLongitude();

        //Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        //Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //Setting latitude and longitude in the TextView tv_position
        //tv_position.setText("Latitude : " + latitude + ", Longitude : " + longitude);

        if(mMarker != null)
        {
            mMarker.remove();
        }
        Log.e("ERROR", "INSIDE onMapReady() before add the first marker");
        mMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Your First Marker"));
        Log.e("ERROR", "INSIDE onMapReady() after adding MARKER");
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 16.0f));
    }

    public void addMarkerPMI()
    {
        double lat;
        double lng;
        //String nama_cabang;
        String alamat;

        for(int i = 0; i < PMIBranchList.size(); i++)
        {
            lat = PMIBranchList.get(i).getLatitude();
            lng = PMIBranchList.get(i).getLongitude();
            nama_cabang = PMIBranchList.get(i).getNamaCabang();
            alamat = PMIBranchList.get(i).getAlamat();
            LatLng pos = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(nama_cabang)
                    .snippet(" Jarak : " + distanceHashMap.get(nama_cabang) + " Durasi : " + durationHashMap.get(nama_cabang) + " " + alamat)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
            Log.e("nama_cabang", nama_cabang);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        switch(status)
        {
            case OUT_OF_SERVICE :
                    break;
            case TEMPORARY_UNAVAILABLE :
                    break;
            case AVAILABLE :
                    break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onGpsStatusChanged(int event)
    {
        switch (event)
        {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.e("GPS STATUS", "GPS STARTTING.....");
                mStatus = false;
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.e("GPS STATUS", "GPS STOPPING....");
                mStatus = false;
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.e("GPS STATUS", "GPS FIRST FIXING.....");
                mStatus = true;
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Log.e("GPS STATUS", "GPS SATELLITE STATUS.....");
                //mStatus = false;
                break;
        };
    }

    class GetDirections extends AsyncTask<LatLng, Void, Void>
    {
        LatLng start;
        ProgressDialog pdLoading = new ProgressDialog(MapsActivity.this);



        private String getDurationString(int seconds)
        {
           int minutes = 0;
           int hours = 0;
           int days = 0;
           int months = 0;
           int years = 0;
           if(seconds >= 60)
           {
               minutes = Double.valueOf(Math.floor(seconds / 60)).intValue();
               seconds = seconds%60;

               if(minutes >= 60)
               {
                   hours = Double.valueOf(Math.floor(minutes / 60)).intValue();
                   minutes = minutes%60;

                   if(hours >= 24)
                   {
                       days = Double.valueOf(Math.floor(hours / 24)).intValue();
                       hours = hours%24;

                       if(days >= 30)
                       {
                           months = Double.valueOf(Math.floor(days / 30)).intValue();
                           days = days%30;

                           if(months >= 12)
                           {
                               years = Double.valueOf(Math.floor(months / 12)).intValue();
                               months = months%12;
                           }
                       }
                   }
               }
           }
           StringBuilder sb = new StringBuilder();
           if(years > 0)
           {
               sb.append(Integer.toString(years));
               if(years > 1)
                   sb.append(" Years ");
               else
                   sb.append(" Year ");
           }
            if(months > 0)
            {
                sb.append(Integer.toString(months));
                if(months > 1)
                    sb.append(" Months ");
                else
                    sb.append(" Month ");
            }
            if(days > 0)
            {
                sb.append(Integer.toString(days));
                if(days > 1)
                    sb.append(" Days ");
                else
                    sb.append(" Day ");
            }
            if(hours > 0)
            {
                sb.append(Integer.toString(hours));
                if(hours > 1)
                    sb.append(" Hours ");
                else
                    sb.append(" Hour ");
            }
            if(minutes > 0)
            {
                sb.append(Integer.toString(minutes));
                if(minutes > 1)
                    sb.append(" Minutes ");
                else
                    sb.append(" Minute ");
            }
            if(seconds > 0)
            {
                sb.append(Integer.toString(seconds)).append(" Second");
                if(seconds > 1)
                {
                    sb.append("s");
                }
            }
            return sb.toString();
        }

        private String getDistanceString(int meters)
        {
            StringBuilder sb = new StringBuilder();
            if(meters < 1000)
            {
                if(meters <= 1)
                    sb.append(Integer.toString(meters)).append(" Meter");
                else
                    sb.append(Integer.toString(meters)).append(" Meters");
            }
            else
            {
                Double kiloMeters = Double.valueOf(meters/1000);
                sb.append(Double.toString(kiloMeters)).append(" KM");
            }
            return sb.toString();
        }

        void sortByDistance()
        {
            int i = 0;
            for(Map.Entry<String, Long> entry : distanceHashMap.entrySet())
            {
                al.add(i,new DistanceMap(entry.getKey(), entry.getValue(), durationHashMap.get(entry.getKey())));
                i++;
            }

            DistanceMap temp;
            boolean swap = true;
            while(swap)
            {
                for(i=0; i<al.size()-1; i++)
                {
                    if(al.get(i).jarak > al.get(i+1).jarak)
                    {
                        temp = al.get(i);
                        al.set(i,al.get(i+1));
                        al.set(i+1,temp);
                        swap = true;
                        break;
                    }
                    else
                    {
                        swap = false;
                    }
                    Log.e("PRINT ARRAY", "No. - " + i );
                }
            }
            /*distanceHashMap.clear();
            durationHashMap.clear();
            for (i=0; i<al.size(); i++)
            {
                Log.e("PRINT ARRAY", al.get(i).nama_cabang);
                distanceHashMap.put(al.get(i).nama_cabang, al.get(i).jarak);
                durationHashMap.put(al.get(i).nama_cabang, al.get(i).durasi);
            }*/
        }


        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            pdLoading.setMessage("\tLoading Maps...");
            pdLoading.show();
            start = new LatLng(latitude, longitude);
        }

        protected Void doInBackground(LatLng... parameter)
        {
            Log.e("DALEM DO IN BACKGROUND", "DALAM DO IN BACKGROUND");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("golongan_darah", golongan_darah.toUpperCase()));
            params.add(new BasicNameValuePair("jenis_rhesus", jenis_rhesus.toUpperCase()));
            params.add(new BasicNameValuePair("jumlah_labu", String.valueOf(jumlah_labu)));
            params.add(new BasicNameValuePair("bentuk_darah", "WB"));
            Log.e("PARAMS", params.toString());
            JSONObject json = jParser.makeHttpRequest(ConstantValues.URL_DISPLAY_AVAILABLE_BRANCHES , "POST", params);
            Log.e("JSON OBJECT", json.toString());
            try
            {
                int success = json.getInt("SUCCESS");

                if (success == 1)
                {
                    products = json.getJSONArray("PRODUCTS");
                    for (int i = 0; i < products.length(); i++)
                    {
                        JSONObject c = products.getJSONObject(i);

                        String kode_cabang = c.getString("KODE_CABANG");
                        int jumlah_stok = c.getInt("JUMLAH_STOK");
                        String nama_cabang = c.getString("NAMA_CABANG");
                        Double longitude = c.getDouble("LONGITUDE");
                        Double latitude = c.getDouble("LATITUDE");
                        String alamat = c.getString("ALAMAT");

                        PMIBranchList.add(new PMIBranchModel(kode_cabang, jumlah_stok, nama_cabang, longitude, latitude, alamat));
                        Log.e("TEST ", kode_cabang + " " + String.valueOf(jumlah_stok) + " " + nama_cabang + " " +
                                longitude + " " + latitude + " " + alamat);


                    }
                }
                else
                {
                    Log.e("ERROR LOADING JSON","Error when loading JSON from server");
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            for(int i=0; i<PMIBranchList.size(); i++)
            {
                String cab = PMIBranchList.get(i).getNamaCabang();
                Double lat = PMIBranchList.get(i).getLatitude();
                Double lng = PMIBranchList.get(i).getLongitude();
                LatLng dest = new LatLng(lat, lng);
                DirectionAPI directionAPI = new DirectionAPI(new LatLng(latitude, longitude), dest);
                GoogleResponse googleResponse = directionAPI.execute();
                if (googleResponse.isOk())
                {
                    Log.e("ERROR TESTING", "INSIDE DO IN BACKGROUND " + cab + " (" + lat + "," + lng + ")");
                    DrivingDirection drivingDirection = new DrivingDirection(googleResponse.getJsonObject());
                    durationHashMap.put(cab, drivingDirection.getDuration());
                    distanceHashMap.put(cab, drivingDirection.getDistance());
                    Log.e("ERROR CEK", cab + " - " + getDurationString(Long.valueOf(drivingDirection.getDuration()).intValue()) + " - " + getDistanceString(Long.valueOf(drivingDirection.getDistance()).intValue()));
                    Log.e("ISI HASH", durationHashMap.toString());
                    for(Map.Entry<String, Long> entry : durationHashMap.entrySet())
                    {
                        String cabang = entry.getKey();
                        String dur = entry.getValue().toString();
                        String str = getDurationString(entry.getValue().intValue());
                        Log.e("STATUS DUR", cabang + " - " + dur + " or " + str);
                    }
                }
            }
            sortByDistance();
            return null;
        }

        protected void onPostExecute(Void zzz)
        {
            //this.cancel(true);
            TextView tv_list = (TextView) findViewById(R.id.list);
            StringBuilder sb = new StringBuilder();
            /*for(int i=0; i<PMIBranchList.size(); i++)
            {
                String cab = PMIBranchList.get(i).getNamaCabang();
                sb.append(i+1).append(". ");
                sb.append(cab).append(" - ");
                sb.append(getDurationString(durationHashMap.get(cab).intValue())).append(" , ");
                sb.append(getDistanceString(distanceHashMap.get(cab).intValue())).append("\n");
            }*/
            for(int i=0; i<al.size(); i++)
            {
                sb.append(i+1).append(". ");
                sb.append(al.get(i).nama_cabang).append(" - ");
                sb.append(getDurationString(al.get(i).durasi.intValue())).append(" , ");
                sb.append(getDistanceString(al.get(i).jarak.intValue())).append("\n");
            }
            tv_list.setText(sb.toString());
            addMarkerPMI();
            pdLoading.dismiss();
        }

    }

    class DistanceMap
    {
        private String nama_cabang;
        private Long jarak;
        private Long durasi;

        DistanceMap(String key, Long value, Long value2)
        {
            this.nama_cabang = key;
            this.jarak = value;
            this.durasi = value2;
        }
    }
}
