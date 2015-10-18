package com.indonative.cari_darah;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String golongan_darah = getIntent().getExtras().getString("golongan_darah");
        TextView text_golongan_darah = (TextView) findViewById(R.id.golongan_darah);
        text_golongan_darah.setText("Golongan Darah : " + golongan_darah);

        Integer jumlah_labu = getIntent().getExtras().getInt("jumlah_labu",1);
        TextView text_jumlah_labu = (TextView) findViewById(R.id.jumlah_labu);
        text_jumlah_labu.setText("Jumlah Labu : " + jumlah_labu);

        String jenis_rhesus = getIntent().getExtras().getString("jenis_rhesus");
        TextView text_jenis_rhesus = (TextView) findViewById(R.id.jenis_rhesus);
        text_jenis_rhesus.setText("Jenis Rhesus : " + jenis_rhesus);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
