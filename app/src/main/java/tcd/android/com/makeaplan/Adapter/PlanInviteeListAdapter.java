package tcd.android.com.makeaplan.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.R;

/**
 * Created by ADMIN on 09/07/2017.
 */

public class PlanInviteeListAdapter extends ArrayAdapter<Pair<String, Integer>> {

    private Context mContext;

    public PlanInviteeListAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.plan_invitees_list_item, null);
        }

        Pair<String, Integer> status = getItem(position);
        ((TextView) convertView.findViewById(R.id.tv_invitee_name)).setText(status.first);
        ImageView statusImageView = (ImageView) convertView.findViewById(R.id.iv_status_icon);
        switch (status.second) {
            case 0:
                statusImageView.setImageResource(R.drawable.ic_dot_red_512px);
                break;
            case 1:
                statusImageView.setImageResource(R.drawable.ic_dot_green_512px);
                break;
            default:
                break;
        }

        return convertView;
    }
}
