package com.example.liaohuaida.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ForecastAdapter extends CursorAdapter {

    private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private static final int TODAY_VIEW_TYPE = 0;
    private static final int FUTURE_VIEW_TYPE = 1;
    private boolean mUseTodayLayout;
    private Context mContext;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case TODAY_VIEW_TYPE: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case FUTURE_VIEW_TYPE: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
            default:
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {

        return (position == 0 && mUseTodayLayout) ? TODAY_VIEW_TYPE : FUTURE_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //设置天气图标
        ImageView imageView = viewHolder.iconView;
        int iconId;
        int viewType = getItemViewType(cursor.getPosition());
        if (viewType == TODAY_VIEW_TYPE) {
            iconId = Utility.getArtWeatherIcon(cursor.getInt(ForecastFragment.COL_WEATHER_ID));
        } else {
            iconId = Utility.getWeatherIcon(cursor.getInt(ForecastFragment.COL_WEATHER_ID));
        }
        if (iconId != -1) {
            imageView.setImageResource(iconId);
        }

        //设置日期
        String dateStr = Utility.getFriendlyDate(cursor.getLong(ForecastFragment.COL_DATE));
        if (viewType == TODAY_VIEW_TYPE) {
            dateStr += "," + Utility.getFormatDate(cursor.getLong(ForecastFragment.COL_DATE));
        }
        viewHolder.dateTextView.setText(dateStr);

        //设置天气情况描述
        String desc = cursor.getString(ForecastFragment.COL_SHORT_DESC);
        viewHolder.descTextView.setText(desc);

        //设置最高气温
        int highTemp = cursor.getInt(ForecastFragment.COL_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.getFormatTemp(mContext, highTemp));

        //设置最低气温
        int lowTemp = cursor.getInt(ForecastFragment.COL_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.getFormatTemp(mContext, lowTemp));
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateTextView;
        public final TextView descTextView;
        public final TextView highTempView;
        public final TextView lowTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_imageView);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date);
            descTextView = (TextView) view.findViewById(R.id.list_item_desc);
            highTempView = (TextView) view.findViewById(R.id.list_item_hightemp);
            lowTempView = (TextView) view.findViewById(R.id.list_item_lowtemp);
        }
    }
}
