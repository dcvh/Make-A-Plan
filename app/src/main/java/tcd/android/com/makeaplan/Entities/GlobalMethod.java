package tcd.android.com.makeaplan.Entities;

import android.app.AlertDialog;
import android.content.Context;

import tcd.android.com.makeaplan.R;
import tcd.android.com.makeaplan.ViewGroupPlanDetailActivity;

/**
 * Created by ADMIN on 08/07/2017.
 */

public final class GlobalMethod {
    public GlobalMethod() {}

    public static void showUnderDevelopmentDialog(Context context) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.under_development_message)
                .setPositiveButton(context.getResources().getString(R.string.ok), null)
                .show();
    }
}
