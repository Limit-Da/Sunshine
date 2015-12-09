package com.example.liaohuaida.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "weather.db";

    private static final String CREATE_LOCATION =
            "CREATE TABLE " + WeatherContract.LocationEntry.TABLE_NAME + "(" +
                    WeatherContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WeatherContract.LocationEntry.COLUMN_CITY_NAME + " TEXT UNIQUE NOT NULL " + ")";

    private static final String CREATE_WEATHER =
            "CREATE TABLE " + WeatherContract.WeatherEntry.TABLE_NAME + "("
            + WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " INTEGER NOT NULL, "
            + WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " INTEGER NOT NULL, "

            + "FOREIGN KEY (" + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES "
            + WeatherContract.LocationEntry.TABLE_NAME + "(" + WeatherContract.LocationEntry._ID + "),"

            + " UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ", " +
            WeatherContract.WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATION);
        db.execSQL(CREATE_WEATHER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.LocationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
