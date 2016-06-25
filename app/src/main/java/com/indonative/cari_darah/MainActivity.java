package com.indonative.cari_darah;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by Rizkia on 16/10/2015.
 */
public class MainActivity extends Activity
{
    ImageButton masukImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_view);
        masukImageButton = (ImageButton) findViewById(R.id.btn_masuk);
        masukImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MainMenu.class);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
