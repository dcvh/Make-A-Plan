package tcd.android.com.makeaplan.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.R;

/**
 * Created by ADMIN on 10/07/2017.
 */

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.NumberViewHolder> {

    private static final String TAG = PlanAdapter.class.getSimpleName();

    final private ListItemClickListener mOnClickListener;
    private ArrayList<Plan> mPlansList;

    public interface ListItemClickListener {
        void onListItemClick(int item);
    }

    public PlanAdapter(ListItemClickListener listItemClickListener) {
        mOnClickListener = listItemClickListener;
        mPlansList = new ArrayList<>();
    }

    public void addPlan(Plan plan) {
        mPlansList.add(plan);
    }

    public Plan getPlan(int position) {
        return mPlansList.get(position);
    }

    public void setPlansList(ArrayList<Plan> plansList) {
        this.mPlansList = plansList;
    }

    public void updateInviteesStatus(int position, int updatedStatus, String userId) {
        if (mPlansList.get(position) instanceof GroupPlan) {
            GroupPlan groupPlan = (GroupPlan) mPlansList.get(position);
            HashMap<String, Integer> inviteesStatus = groupPlan.getInviteesStatus();
            inviteesStatus.put(userId, updatedStatus);
            groupPlan.setInviteesStatus(inviteesStatus);
            mPlansList.set(position, groupPlan);
        }
    }

    public void clear() {
        mPlansList.clear();
    }

    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.plan_list_item, viewGroup, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {
        // basic info
        Plan plan = mPlansList.get(position);
        Context context = holder.planNameTextView.getContext();
        holder.planNameTextView.setText(plan.getName());
        holder.planDateTextView.setText(GlobalMethod.getDateFromMilliseconds(plan.getDateTime(), context));
        holder.planTimeTextView.setText(GlobalMethod.getTimeFromMilliseconds(plan.getDateTime(), context));
        holder.planTagTextView.setText(plan.getTag());
        // status icon
        if (plan.getTag().equals(context.getString(R.string.group))) {
            holder.planTagIconImageView.setImageResource(R.drawable.ic_dot_green_512px);
        } else if (plan.getTag().equals(context.getString(R.string.personal))) {
            holder.planTagIconImageView.setImageResource(R.drawable.ic_dot_blue_512px);
        }
    }

    @Override
    public int getItemCount() {
        return mPlansList.size();
    }


    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView planNameTextView;
        TextView planDateTextView;
        TextView planTimeTextView;
        TextView planTagTextView;
        ImageView planTagIconImageView;

        public NumberViewHolder(View itemView) {
            super(itemView);

            planNameTextView = (TextView) itemView.findViewById(R.id.tv_plan_name);
            planDateTextView = (TextView) itemView.findViewById(R.id.tv_plan_date);
            planTimeTextView = (TextView) itemView.findViewById(R.id.tv_plan_time);
            planTagTextView = (TextView) itemView.findViewById(R.id.tv_plan_tag);
            planTagIconImageView = (ImageView) itemView.findViewById(R.id.iv_tag_icon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(position);
        }
    }
}

