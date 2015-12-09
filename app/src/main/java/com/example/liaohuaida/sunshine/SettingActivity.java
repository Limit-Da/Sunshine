package com.example.liaohuaida.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingActivity extends AppCompatActivity {

    private static final String LOG_TAG = SettingActivity.class.getSimpleName();

    public static void startSettingActivity(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "in Setting Activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new SettingFragment()).commit();

    }

}
