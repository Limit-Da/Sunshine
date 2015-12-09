package com.example.liaohuaida.sunshine;


import android.content.Context;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Utility {
    public static String getPreferenceLocation(Context context) {

        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_city_key),
                        context.getString(R.string.pref_city_default));
    }

    public static String getFormatDate(long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日", Locale.CHINA);

        return  dateFormat.format(date);
    }

    public static long getParseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

        return dateFormat.parse(dateStr).getTime();
    }

    public static String getFormatTemp(Context context, int temp) {
        return context.getString(R.string.format_temperature, temp);
    }

    public static int getWeatherIcon(int weatherId) {
        if (weatherId == 0) {
            return R.drawable.ic_clear;
        } else if (weatherId == 1) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId == 2) {
            return R.drawable.ic_cloudy;
        } else if (weatherId <= 7) {
            return R.drawable.ic_light_rain;
        } else if (weatherId <= 12) {
            return R.drawable.ic_rain;
        } else if (weatherId <= 17) {
            return R.drawable.ic_snow;
        } else  if (weatherId == 18) {
            return R.drawable.ic_fog;
        } else if (weatherId <= 21) {
            return R.drawable.ic_light_rain;
        } else if (weatherId <= 25) {
            return R.drawable.ic_rain;
        } else if (weatherId <= 28) {
            return R.drawable.ic_snow;
        } else if (weatherId <= 53) {
            return R.drawable.ic_fog;
        }
        return -1;
    }

    public static int getArtWeatherIcon(int weatherId) {
        if (weatherId == 0) {
            return R.drawable.art_clear;
        } else if (weatherId == 1) {
            return R.drawable.art_light_clouds;
        } else if (weatherId == 2) {
            return R.drawable.art_clouds;
        } else if (weatherId <= 7) {
            return R.drawable.art_light_rain;
        } else if (weatherId <= 12) {
            return R.drawable.art_rain;
        } else if (weatherId <= 17) {
            return R.drawable.art_snow;
        } else  if (weatherId == 18) {
            return R.drawable.art_fog;
        } else if (weatherId <= 21) {
            return R.drawable.art_light_rain;
        } else if (weatherId <= 25) {
            return R.drawable.art_rain;
        } else if (weatherId <= 28) {
            return R.drawable.art_snow;
        } else if (weatherId <= 53) {
            return R.drawable.art_fog;
        }
        return -1;
    }

    //返回00:00:00的UTC时间
    public static long getStartDateTime(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar returnCalendar = new GregorianCalendar(year, month, dayOfMonth);

        return returnCalendar.getTimeInMillis();
    }

    public static boolean isNotificationOpen(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_notification_key), true);
    }

    public static String getFriendlyDate(long date) {
        final long MILLS_OF_DAY = 24 * 60 * 60 * 1000;
        final long TODAY = getStartDateTime(System.currentTimeMillis());
        final Calendar calendar = Calendar.getInstance();

        int times = (int) ((date - TODAY) / MILLS_OF_DAY);
        calendar.setTimeInMillis(date);

        switch (times) {
            case 0: {
                return "今天";
            }
            case 1: {
                return "明天";
            }
            case 2: case 3: case 4: case 5: case 6:
                return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.CHINA);
            default:
                return null;
        }

    }

}
