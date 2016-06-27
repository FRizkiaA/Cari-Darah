package com.indonative.cari_darah;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;

public class MainMenu extends Activity
{
    public String golongan_darah;
    public String jenis_rhesus;
    public static final String POSITIVE = "POSITIVE";
    public static final String NEGATIVE = "NEGATIVE";
    public GPSTracker tracker;
    public int jumlah_labu;
    public String jumlah_labu_string;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        tracker = new GPSTracker(this);
        Log.e("ERROORRRRRRRRR", "APAKAH DIALOG MUNCUL??? " + tracker.canGetLocation());
        if(!tracker.canGetLocation())
        {
            Log.e("ERROORRRRRRRRR", "DIALOG GAK MUNCUL");
            tracker.showSettingAlert();
        }

        Spinner dropdown_gol_darah = (Spinner) findViewById(R.id.dropdown_gol_darah);

        dropdown_gol_darah.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                golongan_darah = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                golongan_darah = "A";//init
            }
        });

        jenis_rhesus = POSITIVE;//init

        Spinner dropdown_jumlah_labu = (Spinner) findViewById(R.id.dropdown_jml_labu);
        dropdown_jumlah_labu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                jumlah_labu_string = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                jumlah_labu_string = "1";//init
            }
        });
    }

    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId())
        {
            case R.id.positive_radio_button:
                if (checked)
                {
                    RadioButton negative = (RadioButton) findViewById(R.id.negative_radio_button);
                    negative.setChecked(false);
                    jenis_rhesus = POSITIVE;
                }
                break;
            case R.id.negative_radio_button:
                if (checked)
                {
                    RadioButton positive = (RadioButton) findViewById(R.id.positive_radio_button);
                    positive.setChecked(false);
                    jenis_rhesus = NEGATIVE;
                };
                break;
        }
    }

    public void onCariButtonClick(View view)
    {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        //getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //boolean isNetworkEnabled = isNetworkAvailable();
        Log.e("STATUS PROVIDER", "GPS : " + isGPSEnabled + " NETWORK : " + isNetworkEnabled);
        if(!isGPSEnabled)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Provider is not enabled");
            alertDialog.setMessage("Please turn on your GPS !");
            alertDialog.setNeutralButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    }
            );
            alertDialog.show();
        }
        else if(!isNetworkEnabled)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Provider is not enabled");
            alertDialog.setMessage("Please check your internet connection!");
            alertDialog.setNeutralButton("Okay", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    }
            );
            alertDialog.show();
        }
        else
        {
            Intent i = new Intent(MainMenu.this, MapsActivity.class);
            i.putExtra("golongan_darah", golongan_darah);
            jumlah_labu = Integer.parseInt(jumlah_labu_string);
            i.putExtra("jumlah_labu", jumlah_labu);
            i.putExtra("jenis_rhesus", jenis_rhesus);
            startActivity(i);
        }
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
