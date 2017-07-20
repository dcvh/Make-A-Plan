package tcd.android.com.makeaplan.Entities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tcd.android.com.makeaplan.MainActivity;
import tcd.android.com.makeaplan.R;
import tcd.android.com.makeaplan.SettingsActivity;
import tcd.android.com.makeaplan.ViewGroupPlanDetailActivity;

/**
 * Created by ADMIN on 08/07/2017.
 */

public final class GlobalMethod {

    public static final String GROUP_LABEL = "group";
    public static final String PERSONAL_LABEL = "personal";
    public static final String ACCOUNT_ID_LABEL = "accountId";
    public static final String USER_LABEL = "user";

    private static AlertDialog checkNetworkDialog = null;

    public GlobalMethod() {
    }

    public static void showUnderDevelopmentDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.under_development_message)
                .setPositiveButton(context.getString(R.string.ok_button), null)
                .show();
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    public static void checkNetworkState(final Context context) {
        if (!isNetworkConnected(context)) {
            checkNetworkDialog = new AlertDialog.Builder(context).create();
            checkNetworkDialog.setMessage(context.getString(R.string.app_requires_network_error));
            checkNetworkDialog.setCancelable(false);
            checkNetworkDialog.setButton(Dialog.BUTTON_NEGATIVE, context.getString(R.string.quit_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((Activity)context).finish();
                                }
                            });
            checkNetworkDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.try_again_button),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            checkNetworkState(context);
                        }
                    });
            checkNetworkDialog.show();
        }
    }

    public static void dismissNetworkDialog() {
        if (checkNetworkDialog != null) {
            checkNetworkDialog.dismiss();
        }
    }

    public static String getDateFromMilliseconds(long millis, Context context) {
        // get selected format from settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String dateFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_DATE_FORMAT, context.getString(R.string.pref_date_format_default));
        // return result according to format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return new SimpleDateFormat(dateFormatPref).format(calendar.getTime());
    }

    public static String getTimeFromMilliseconds(long millis, Context context) {
        // get selected format from settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String timeFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_TIME_FORMAT, context.getString(R.string.pref_time_format_default));
        // return result according to format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return new SimpleDateFormat(timeFormatPref).format(calendar.getTime());
    }
}
