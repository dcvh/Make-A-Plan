package tcd.android.com.makeaplan.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tcd.android.com.makeaplan.Entities.GroupPlanOption;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.R;

/**
 * Created by ADMIN on 05/07/2017.
 */

public class GroupPlanOptionListAdapter extends ArrayAdapter<GroupPlanOption> {
    private Context mContext;

    public GroupPlanOptionListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.add_group_plan_list_item, null);
        }

        GroupPlanOption option = getItem(position);
        ((TextView)convertView.findViewById(R.id.tv_option_title)).setText(option.getTitle());
        ((TextView)convertView.findViewById(R.id.tv_option_value)).setText(option.getValue());
        ((ImageView)convertView.findViewById(R.id.iv_option_icon)).setImageResource(option.getDrawableId());

        return convertView;
    }
}
