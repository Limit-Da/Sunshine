package com.example.liaohuaida.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.liaohuaida.sunshine.MainActivity;
import com.example.liaohuaida.sunshine.R;
import com.example.liaohuaida.sunshine.Utility;
import com.example.liaohuaida.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Vector;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    private final Context mContext;

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;


    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        String city = Utility.getPreferenceLocation(mContext);

        final String CITY = "city";
        final String KEY = "key";

        String key = "bd6dbff1129f4d5a904155b5ef5686dd";

        HttpURLConnection urlConnection = null;
        StringBuilder stringBuilder = new StringBuilder();
        String forecastJsonStr = null;
        try {
            Uri queryUri = Uri.parse("https://api.heweather.com/x3/weather?").buildUpon()
                    .appendQueryParameter(CITY, city)
                    .appendQueryParameter(KEY, key)
                    .build();
            URL url = new URL(queryUri.toString());

            Log.i(LOG_TAG, url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            forecastJsonStr = stringBuilder.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != urlConnection) {
                urlConnection.disconnect();
            }
        }

        try{
            getDataFromJson(forecastJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse json string");
        }
    }

    private void getDataFromJson(String jsonStr)
        throws JSONException {

        JSONObject forecastJson = new JSONObject(jsonStr);
        JSONObject dataObject = forecastJson.getJSONArray("HeWeather data service 3.0").getJSONObject(0);
        JSONObject cityObject = dataObject.getJSONObject("basic");
        JSONArray forecastArray = dataObject.getJSONArray("daily_forecast");

        String cityName = cityObject.getString("city");
        long locationId = addLocation(cityName);

        Vector<ContentValues> cvVector = new Vector<>(forecastArray.length());

        for (int i = 0; i < forecastArray.length(); i++) {
            String dateStr;

            JSONObject dayForecast = forecastArray.getJSONObject(i);

            dateStr = dayForecast.getString("date");
            JSONObject condition = dayForecast.getJSONObject("cond");
            JSONObject tmp = dayForecast.getJSONObject("tmp");

            int highTemp = tmp.getInt("max");
            int lowTemp = tmp.getInt("min");
            int weatherId = condition.getInt("code_d");
            String shortDesc = condition.getString("txt_d");

            long date= 0;
            try {
                date = Utility.getParseDate(dateStr);
            } catch (ParseException e) {
                Log.e(LOG_TAG, "Failed to parse date");
                e.printStackTrace();
            }

            String forecast = dateStr + " - "+ shortDesc + " - " + highTemp + "/" + lowTemp;
            Log.i(LOG_TAG, forecast);

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

            // 删除过时的数据
            int rows = mContext.getContentResolver()
                    .delete(WeatherContract.WeatherEntry.CONTENT_URI,
                            WeatherContract.WeatherEntry.COLUMN_DATE + " < ? ",
                            new String[]{Long.toString(Utility.getStartDateTime(System.currentTimeMillis()))});

            Log.i(LOG_TAG, "Delete is completed. " + rows + " rows are deleted");
        }
        Log.i(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

        if (Utility.isNotificationOpen(mContext)) {
            notifyWeather();
        }
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

    /**
     * 在synImmediately中使用，判断是否需要调用synImmediately马上同步。
     * @return 若数据库中已经存在所选地区一周的天气数据，则返回false，否则返回true
     * */
    private static boolean isNeedUpdate(Context context) {
        String city = Utility.getPreferenceLocation(context);

        Uri weatherUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(city, Utility.getStartDateTime(System.currentTimeMillis()));

        Cursor result = context.getContentResolver().query(weatherUri,null,null,null,null);
        int count = result.getCount();
        result.close();

        return count < 7;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        if (!isNeedUpdate(context)) {
            return ;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    private void notifyWeather() {
        Log.i(LOG_TAG, "in notifyWeather");

        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
            // Last sync was more than 1 day ago, let's send a notification with the weather.
            String locationQuery = Utility.getPreferenceLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(locationQuery, Utility.getStartDateTime(System.currentTimeMillis()));

            // we'll query our contentProvider, as always
            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                int high = cursor.getInt(INDEX_MAX_TEMP);
                int low = cursor.getInt(INDEX_MIN_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = Utility.getWeatherIcon(weatherId);
                String title = context.getString(R.string.app_name);

                // Define the text of the forecast.
                String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        Integer.toString(high),
                        Integer.toString(low));
                Log.i(LOG_TAG, contentText);
                //build your notification here.
                NotificationCompat.Builder mBuilder= (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText);
                Intent resultIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
                taskStackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                //refreshing last sync
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
            }
            cursor.close();
        }

    }
}
