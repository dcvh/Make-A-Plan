package tcd.android.com.makeaplan.Entities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import tcd.android.com.makeaplan.MainActivity;
import tcd.android.com.makeaplan.R;
import tcd.android.com.makeaplan.ViewGroupPlanDetailActivity;

/**
 * Created by ADMIN on 08/07/2017.
 */

public final class GlobalMethod {

    private static AlertDialog checkNetworkDialog = null;

    public GlobalMethod() {
    }

    public static void showUnderDevelopmentDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.under_development_message)
                .setPositiveButton(context.getResources().getString(R.string.ok), null)
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
            checkNetworkDialog.setMessage(context.getResources().getString(R.string.check_network_connection_message));
            checkNetworkDialog.setCancelable(false);
            checkNetworkDialog.setButton(context.getResources().getString(R.string.quit),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((Activity)context).finish();
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

}
