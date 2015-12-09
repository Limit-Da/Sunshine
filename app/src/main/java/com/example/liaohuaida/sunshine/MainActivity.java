package com.example.liaohuaida.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.liaohuaida.sunshine.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String DATE_URI = "dateUri";
    private static String mLocation;
    private boolean mTwoPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, " onCreate in MainActivity.");
        setContentView(R.layout.activity_main);
        if (null == mLocation ) {
            mLocation = Utility.getPreferenceLocation(this);
        }
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPanel = true;

            if (null == savedInstanceState) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailActivityFragment(),
                                DetailActivityFragment.FRAGMENT_TAG).commit();
            }
        } else {
            mTwoPanel = false;
//            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment ff = (ForecastFragment) getFragmentManager().findFragmentById(R.id.fragment_forecast);
        ff.setUseTodayLayout(!mTwoPanel);

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, " onResume in MainActivity.");
        String location = Utility.getPreferenceLocation(this);

        if ( location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailActivityFragment df = (DetailActivityFragment)getFragmentManager().findFragmentById(R.id.weather_detail_container);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onItemClick(Uri dateUri) {
        if (mTwoPanel) {
            DetailActivityFragment f = new DetailActivityFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(DATE_URI, dateUri);
            f.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, f).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(dateUri);
            startActivity(intent);
        }

    }

    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


}
