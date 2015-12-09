package com.example.liaohuaida.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.liaohuaida.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAIL_LOADER = 2;
    private ShareActionProvider mShareActionProvider;
    private String mShareString;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private  Uri mUri;

    public static final String FRAGMENT_TAG = "DFTAG";

    public static final String[] detailProjection = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    //public static final int WEATHER_KEY = 0;
    public static final int COL_DATE = 1;
    public static final int COL_WEATHER_ID = 2;
    public static final int COL_SHORT_DESC = 3;
    public static final int COL_MAX_TEMP = 4;
    public static final int COL_MIN_TEMP = 5;

    private TextView mFriendlyDateTextView;
    private TextView mDateTextView;
    private TextView mHighTempTextView;
    private TextView mLowTempTextView;
    private TextView mDescTextView;
    private ImageView mIconImageView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.detail_share_item);

        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareString != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareString);

        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "in onCreateView");

        Bundle bundle = getArguments();
        if (null != bundle) {
            mUri =  bundle.getParcelable(MainActivity.DATE_URI);
        } else {
            mUri = getActivity().getIntent().getData();
        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mFriendlyDateTextView = (TextView) view.findViewById(R.id.detail_friendly_date_textview);
        mDateTextView = (TextView) view.findViewById(R.id.detail_date_textview);
        mHighTempTextView = (TextView) view.findViewById(R.id.detail_hightemp_textview);
        mLowTempTextView = (TextView) view.findViewById(R.id.detail_lowtemp_textview);
        mDescTextView = (TextView) view.findViewById(R.id.detail_desc_textview);
        mIconImageView = (ImageView) view.findViewById(R.id.detail_icon_imageview);

        return view;
    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        if (null != mUri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
            mUri = WeatherContract.WeatherEntry.
                    buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null == mUri) {
            return null;
        }
        return new CursorLoader(getActivity(),
                mUri,
                detailProjection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (null == cursor || !cursor.moveToFirst()) {
            return;
        }

        String friendlyDateStr = Utility.getFriendlyDate(cursor.getLong(COL_DATE));
        String dateStr = Utility.getFormatDate(cursor.getLong(COL_DATE));
        String shortDesc = cursor.getString(COL_SHORT_DESC);
        String maxTemp = Utility.getFormatTemp(getActivity(), cursor.getInt(COL_MAX_TEMP));
        String minTemp = Utility.getFormatTemp(getActivity(), cursor.getInt(COL_MIN_TEMP));

        mShareString = dateStr + " - " + shortDesc + " - " + maxTemp + "/" + minTemp;

        if ( null != mShareActionProvider) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        mIconImageView.setImageResource(Utility.getArtWeatherIcon(cursor.getInt(COL_WEATHER_ID)));
        mFriendlyDateTextView.setText(friendlyDateStr);
        mDateTextView.setText(dateStr);
        mDescTextView.setText(shortDesc);
        mHighTempTextView.setText(maxTemp);
        mLowTempTextView.setText(minTemp);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
