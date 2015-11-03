package com.indonative.cari_darah;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.indonative.cari_darah.controller.JSONParser;
import com.indonative.cari_darah.model.PMIBranchModel;
import com.indonative.cari_darah.utilities.ConstantValues;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback
{
    private GoogleMap mMap;
    private Marker mMarker;
    private double latitude;
    private double longitude;
    private JSONParser jParser = new JSONParser();
    TextView text_jenis_rhesus, text_golongan_darah, text_jumlah_labu;
    JSONArray products;
    String golongan_darah, jenis_rhesus;
    int jumlah_labu;
    ArrayList<PMIBranchModel> PMIBranchList = new ArrayList<PMIBranchModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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
            //Google Play Service are available

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            golongan_darah = getIntent().getExtras().getString("golongan_darah");
            text_golongan_darah = (TextView) findViewById(R.id.golongan_darah);
            text_golongan_darah.setText("Golongan Darah : " + golongan_darah);

            jumlah_labu = getIntent().getExtras().getInt("jumlah_labu", 1);
            text_jumlah_labu = (TextView) findViewById(R.id.jumlah_labu);
            text_jumlah_labu.setText("Jumlah Labu : " + jumlah_labu);

            jenis_rhesus = getIntent().getExtras().getString("jenis_rhesus");
            text_jenis_rhesus = (TextView) findViewById(R.id.jenis_rhesus);
            text_jenis_rhesus.setText("Jenis Rhesus : " + jenis_rhesus);

            new LoadAllDatas().execute();

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
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);
            //location.reset();
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            criteria = new Criteria();

            // Getting the name of the best provider
            provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            location = locationManager.getLastKnownLocation(provider);

            //Location location = new Location(locationManager.NETWORK_PROVIDER);
            //Location location = mMap.getMyLocation();
            if(location!=null)
            {
                Log.e("ERROR", "INSIDE onMapReady() LOCATION IS NOT NULL LAT :"+location.getLatitude() + " LNG :" +location.getLongitude());
                onLocationChanged(location);
            }

            addMarkerPMI();

            locationManager.requestLocationUpdates(provider, 15000, 0, this);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if (!mMarker.getId().equals(marker.getId()))
                    {
                        /*if (dt == null)
                        {
                            LatLng dest = new LatLng(-6.184805, 106.844868);
                            dt = new DirectionTask();
                            dt.execute(dest);
                        }*/
                        //LatLng dest = new LatLng(-6.184805, 106.844868);
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
                        //Getting LocationManager object from System Service LOCATION_SERVICE
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        // Creating a criteria object to retrieve provider
                        Criteria criteria = new Criteria();

                        // Getting the name of the best provider
                        String provider = locationManager.getBestProvider(criteria, true);

                        // Getting Current Location
                        Location location = locationManager.getLastKnownLocation(provider);
                        //Location location = new Location(locationManager.NETWORK_PROVIDER);
                        LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());

                        //current_location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current_location, 15);
                        mMap.animateCamera(update);
                        if (mMarker != null) {
                            mMarker.remove();
                        }
                        mMarker = mMap.addMarker(new MarkerOptions().position(current_location).title("Your New Position"));

                        /*if (dt == null)
                        {
                            LatLng dest = new LatLng(-6.184805, 106.844868);
                            dt = new DirectionTask();
                            dt.execute(dest);
                        }*/
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        TextView tv_position = (TextView) findViewById(R.id.position);

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
        tv_position.setText("Latitude : " + latitude + ", Longitude : " + longitude);

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
        String nama_cabang;
        String alamat;

        for(int i = 0; i < PMIBranchList.size(); i++) {
            lat = PMIBranchList.get(i).getLatitude();
            lng = PMIBranchList.get(i).getLongitude();
            nama_cabang = PMIBranchList.get(i).getNamaCabang();
            alamat = PMIBranchList.get(i).getAlamat();
            LatLng pos = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(nama_cabang)
                    .snippet(alamat)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
            Log.e("nama_cabang", nama_cabang);
        }

        /*
        double lat = -6.184805; //contoh marker PMI DKI Jakarta
        double lng = 106.844868;
        String nama_cabang = "PMI DKI Jakarta";
        String alamat = "Jl. Kramat Raya No.47, Senen, Kota Jakarta Pusat, Daerah Khusus Ibukota Jakarta 10450";
        LatLng pos = new LatLng(lat, lng);

        mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(nama_cabang)
                .snippet(alamat)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));

        lat = -6.870310; //contoh marker PMI Kota Bandung
        lng = 107.620000;
        nama_cabang = "PMI Kota Bandung";
        alamat = "Jalan Aceh No. 79, Jalan Aceh, Bandung, Jawa Barat 40113";
        pos = new LatLng(lat, lng);

        Marker marker_pmi = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(nama_cabang)
                .snippet(alamat)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
        */


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

    class LoadAllDatas extends AsyncTask<String, String, Bitmap> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Create account...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("golongan_darah", golongan_darah.toUpperCase()));
            params.add(new BasicNameValuePair("jenis_rhesus", jenis_rhesus.toUpperCase()));
            params.add(new BasicNameValuePair("jumlah_labu", String.valueOf(jumlah_labu)));
            params.add(new BasicNameValuePair("bentuk_darah", "WB"));
            JSONObject json = jParser.makeHttpRequest(ConstantValues.URL_DISPLAY_AVAILABLE_BRANCHES , "POST", params);
            try {
                int success = json.getInt("SUCCESS");

                if (success == 1) {
                    products = json.getJSONArray("PRODUCTS");
                    for (int i = 0; i < products.length(); i++) {
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
                } else {
                    pDialog.setMessage("Error when loading image from server");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Bitmap zzz) {
            pDialog.dismiss();
        }

    }
}
