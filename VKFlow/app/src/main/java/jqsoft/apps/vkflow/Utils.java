package jqsoft.apps.vkflow;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Utils {
    private static final String TODAY = "Today";
    private static final String YESTERDAY = "Yesterday";

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String getDateFromUnitTime(long unixTime) {
        Date date = new Date(unixTime * 1000);
        Calendar gc = GregorianCalendar.getInstance();
        gc.setTime(date);
        gc.setFirstDayOfWeek(Calendar.MONDAY);

        Date nowDate = new Date();
        Calendar gcNow = GregorianCalendar.getInstance();
        gcNow.setTime(nowDate);
        gcNow.setFirstDayOfWeek(Calendar.MONDAY);

        StringBuilder sbFormattedDate = new StringBuilder();
        if (gc.get(Calendar.YEAR) == gcNow.get(Calendar.YEAR)
                && gc.get(Calendar.DAY_OF_YEAR) == gcNow
                .get(Calendar.DAY_OF_YEAR)) {
            sbFormattedDate.append(TODAY);
        } else {
            gcNow.add(Calendar.DAY_OF_YEAR, -1);

            if (gc.get(Calendar.YEAR) == gcNow.get(Calendar.YEAR)
                    && gc.get(Calendar.DAY_OF_YEAR) == gcNow
                    .get(Calendar.DAY_OF_YEAR)) {
                sbFormattedDate.append(YESTERDAY);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.US);
                sbFormattedDate.append(dateFormat.format(gc.getTime()));
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(" 'at' H:mm", Locale.US);
        sbFormattedDate.append(dateFormat.format(gc.getTime()));

        return sbFormattedDate.toString();
    }
}
