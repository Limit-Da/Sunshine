package com.example.liaohuaida.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.liaohuaida.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    private final Context mContext;

    private final int numDays = 7;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public FetchWeatherTask(Context context) {
        mContext = context;
    }

    private String[] getDataFromJsonStr(String jsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(jsonStr);
        JSONObject dataObject = forecastJson.getJSONObject("result").getJSONObject("data");
        JSONArray weatherArray = dataObject.getJSONArray("weather");
        JSONObject realTimeObject = dataObject.getJSONObject("realtime");

        String cityName = realTimeObject.getString("city_name");
        long locationId = addLocation(cityName);

        String[] resultStr = new String[numDays];

        Vector<ContentValues> cvVector = new Vector<>(weatherArray.length());

        for (int i = 0; i < weatherArray.length(); i++) {
            String dateStr;

            JSONObject dayForecast = weatherArray.getJSONObject(i);
            dateStr = dayForecast.getString("date");

            JSONArray dayInfo = dayForecast.getJSONObject("info")
                    .getJSONArray("day");
            JSONArray nightInfo = dayForecast.getJSONObject("info")
                    .getJSONArray("night");

            int highTemp = dayInfo.getInt(2);
            int lowTemp = nightInfo.getInt(2);
            int weatherId = dayInfo.getInt(0);
            String shortDesc = dayInfo.getString(1);

            long date= 0;
            try {
                date = dateFormat.parse(dateStr).getTime();
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Failed to parse date");
                e.printStackTrace();
            }
            String description = dayInfo.getString(1);

            String forecast = dateStr + " - "+ description + " - " + highTemp + "/" + lowTemp;

            Log.i(LOG_TAG, forecast);

            resultStr[i] = forecast;

            ContentValues weatherValue = new ContentValues();
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, highTemp);
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, lowTemp);
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, shortDesc);
            weatherValue.put(WeatherContract.WeatherEntry.COLUMN_DATE, date);

            cvVector.add(weatherValue);
        }

        int inserted = 0;
        if (cvVector.size() > 0) {
            ContentValues[] weatherValues = new ContentValues[cvVector.size()];
            cvVector.toArray(weatherValues);
            inserted = mContext.getContentResolver().
                    bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);
        }
        Log.i(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

        return resultStr;

    }

    private long addLocation(String cityName) {
        long locationId;

        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_CITY_NAME + " = ? ",
                new String[]{cityName},
                null
        );
        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = cursor.getLong(locationIdIndex);
        } else {
            ContentValues cityValues = new ContentValues();
            cityValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cityValues);
            locationId = ContentUris.parseId(uri);
        }

        cursor.close();

        return locationId;
    }

    @Override
    protected Void doInBackground(String... params) {

        final String CITY_NAME = "cityname";
        final String KEY = "key";

        String city = params[0];
        String key = "a443f4c942af901659d40fbec108ac3a";

        HttpURLConnection urlConnection = null;
        StringBuilder stringBuilder = new StringBuilder();
        String forecastJsonStr = null;
        try {
            Uri queryUri = Uri.parse("http://op.juhe.cn/onebox/weather/query?").buildUpon()
                    .appendQueryParameter(CITY_NAME, city)
                    .appendQueryParameter(KEY, key)
                    .build();
            URL url = new URL(queryUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            forecastJsonStr = stringBuilder.toString();

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }

        try{
            getDataFromJsonStr(forecastJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse json string");
        }

        return null;
    }

}
