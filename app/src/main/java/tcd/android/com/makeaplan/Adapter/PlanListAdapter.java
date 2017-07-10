package tcd.android.com.makeaplan.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Console;

import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.R;

/**
 * Created by ADMIN on 03/07/2017.
 */

public class PlanListAdapter extends ArrayAdapter<Plan> {
    private Context mContext;

    public PlanListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.plan_list_item, null);
        }

        Plan plan = getItem(position);
        ((TextView)convertView.findViewById(R.id.tv_plan_name)).setText(plan.getName());
        ((TextView)convertView.findViewById(R.id.tv_plan_date))
                .setText(GlobalMethod.getDateFromMilliseconds(plan.getDateTime(), parent.getContext()));
        ((TextView)convertView.findViewById(R.id.tv_plan_time))
                .setText(GlobalMethod.getTimeFromMilliseconds(plan.getDateTime(), parent.getContext()));
        ((TextView)convertView.findViewById(R.id.tv_plan_tag)).setText(plan.getTag());

//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (plan.getTag().equals(mContext.getString(R.string.personal))) {
//                ((View) convertView.findViewById(R.id.circle_icon)).setBackgroundTintList(
//                        ColorStateList.valueOf(ContextCompat.getColor(mContext, android.R.color.holo_green_light)));
//            }
//        }

        if (plan.getTag().equals(mContext.getString(R.string.personal))) {
            ((ImageView)convertView.findViewById(R.id.circle_icon)).setImageResource(R.drawable.ic_dot_blue_512px);
        }

        return convertView;
    }
}
