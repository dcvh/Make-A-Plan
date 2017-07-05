package tcd.android.com.makeaplan.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        ((TextView)convertView.findViewById(R.id.tv_plan_date)).setText(plan.getDate());
        ((TextView)convertView.findViewById(R.id.tv_plan_tag)).setText(plan.getTag());

        return convertView;
    }
}
