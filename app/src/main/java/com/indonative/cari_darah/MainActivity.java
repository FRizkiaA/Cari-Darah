package com.indonative.cari_darah;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    public String golongan_darah;
    public String jenis_rhesus;
    public static final String POSITIVE = "POSITIVE";
    public static final String NEGATIVE = "NEGATIVE";
    public GPSTracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tracker = new GPSTracker(this);
        Log.e("ERROORRRRRRRRR", "APAKAH DIALOG MUNCUL??? "+tracker.canGetLocation());
        if(!tracker.canGetLocation())
        {
            Log.e("ERROORRRRRRRRR", "DIALOG GAK MUNCUL");
            tracker.showSettingAlert();
        }

        Spinner dropdown_gol_darah = (Spinner) findViewById(R.id.dropdown_gol_darah);
        String[] items = new String[]{"A","B","AB","O"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,items);
        dropdown_gol_darah.setAdapter(adapter);
        dropdown_gol_darah.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //Log.v("item", (String) parent.getItemAtPosition(position));
                golongan_darah = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
                golongan_darah = "A";//init
            }
        });

        jenis_rhesus = POSITIVE;//init

        NumberPicker picker_jumlah_labu = (NumberPicker) findViewById(R.id.picker_jml_labu);
        picker_jumlah_labu.setMinValue(1);
        picker_jumlah_labu.setMaxValue(25);
        picker_jumlah_labu.setWrapSelectorWheel(true);
        picker_jumlah_labu.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal)
            {
                picker.setValue(newVal);
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
                }
                break;
        }
    }

    public void onCariButtonClick(View view)
    {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        //getting GPS status
        //boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //getting network status
        //boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(provider == null)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Provider is not enabled");
            alertDialog.setMessage("Please check your internet connection or turn on your GPS !");
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
            NumberPicker picker_jumlah_labu = (NumberPicker) findViewById(R.id.picker_jml_labu);
            Intent i = new Intent(MainActivity.this, MapsActivity.class);
            i.putExtra("golongan_darah", golongan_darah);
            i.putExtra("jumlah_labu", picker_jumlah_labu.getValue());
            i.putExtra("jenis_rhesus", jenis_rhesus);
            startActivity(i);
        }
    }

}
