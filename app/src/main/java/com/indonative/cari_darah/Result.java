package com.indonative.cari_darah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Rizkia on 17/10/2015.
 */
public class Result extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
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
}
