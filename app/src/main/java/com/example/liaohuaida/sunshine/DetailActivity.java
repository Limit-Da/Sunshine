package com.example.liaohuaida.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, new DetailActivityFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SettingActivity.startSettingActivity(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void startDetailActivity(Context context, String forecastStr) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("forecastStr", forecastStr);
        context.startActivity(intent);
    }

}
