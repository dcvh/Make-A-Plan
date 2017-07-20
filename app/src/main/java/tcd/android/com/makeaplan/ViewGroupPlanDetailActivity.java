package tcd.android.com.makeaplan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tcd.android.com.makeaplan.Adapter.PlanInviteeListAdapter;
import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.GroupPlan;

public class ViewGroupPlanDetailActivity extends AppCompatActivity {

    private GroupPlan groupPlan;
    private String userId;
    private Switch switchStatus;

    // firebase components
    private DatabaseReference groupPlanDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_plan_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        groupPlan = (GroupPlan) getIntent().getSerializableExtra(getString(R.string.group));
        userId = getIntent().getStringExtra(getString(R.string.account_id));
        switchStatus = (Switch) findViewById(R.id.switch_status);

        displayGroupPlanInfo();

        // get the location image
        String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                + groupPlan.getPlaceLatLng()
                + "&zoom=16&size=1000x300";
        ImageView locationImageView = (ImageView) findViewById(R.id.iv_group_plan_location);
        Glide.with(this).load(imageUrl).centerCrop().into(locationImageView);
//        Picasso.with(this).load(imageUrl).fit().centerCrop().into(locationImageView);

        // navigate button
        ((TextView) findViewById(R.id.tv_navigate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + groupPlan.getPlaceLatLng()));
                startActivity(intent);
            }
        });

        displayInvitees();

        // scroll to top
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_plan_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                updateOwnerAttendanceStatus();
                break;
            case R.id.edit_menu:
                GlobalMethod.showUnderDevelopmentDialog(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateOwnerAttendanceStatus() {
        if (switchStatus.getVisibility() == View.VISIBLE) {
            groupPlanDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_group_plan));
            groupPlanDatabaseRef.child(groupPlan.getId()).child(getString(R.string.firebase_invitees_status))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GenericTypeIndicator<HashMap<String, Integer>> t = new GenericTypeIndicator<HashMap<String, Integer>>() {};
                            HashMap<String, Integer> inviteesStatus = dataSnapshot.getValue(t);
                            if (inviteesStatus == null) {
                                inviteesStatus = new HashMap<String, Integer>();
                            }

                            inviteesStatus.put(userId, switchStatus.isChecked() ? 1 : 0);
                            groupPlanDatabaseRef.child(groupPlan.getId()).child(getString(R.string.firebase_invitees_status))
                                    .setValue(inviteesStatus);

                            // return udpated status to MainActivity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(getString(R.string.firebase_invitees_status), switchStatus.isChecked() ? 1 : 0);
                            setResult(Activity.RESULT_OK, resultIntent);
                            onBackPressed();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
        else {
            onBackPressed();
        }
    }

    private void displayGroupPlanInfo() {
        ((TextView) findViewById(R.id.tv_group_plan_name)).setText(groupPlan.getName());
        ((TextView) findViewById(R.id.tv_group_plan_date))
                .setText(GlobalMethod.getDateFromMilliseconds(groupPlan.getDateTime(), this));
        ((TextView) findViewById(R.id.tv_group_plan_time))
                .setText(GlobalMethod.getTimeFromMilliseconds(groupPlan.getDateTime(), this));
        ((TextView) findViewById(R.id.tv_group_plan_address)).setText(groupPlan.getPlaceAddress());

        // retrieve owner name from its ID
        DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_users));
        userDatabaseRef.child(groupPlan.getOwner()).child(getString(R.string.firebase_name))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String ownerName = dataSnapshot.getValue(String.class);
                        if (ownerName != null) {
                            ((TextView) findViewById(R.id.tv_group_plan_owner)).setText(ownerName);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void displayInvitees() {
        HashMap<String, String> invitees = groupPlan.getInvitees();
        if (invitees != null) {
            // attendance
            String attendance = invitees.size() + " " + getString(R.string.invitee_label);
            ((TextView) findViewById(R.id.tv_group_plan_attendance)).setText(attendance);
            // invitees list
            ListView inviteesListView = (ListView) findViewById(R.id.lv_group_plan_invitees);
            PlanInviteeListAdapter adapter = new PlanInviteeListAdapter(this);
            HashMap<String, Integer> inviteesStatus = groupPlan.getInviteesStatus();
            if (inviteesStatus != null) {
                for (Map.Entry<String, String> invitee : invitees.entrySet()) {
                    adapter.add(new Pair<String, Integer>(invitee.getValue(), inviteesStatus.get(invitee.getKey())));
                    // set owner attendance status
                    if (invitee.getKey().equals(userId)) {
                        boolean isGoing = inviteesStatus.get(invitee.getKey()) == 1;
                        ((TextView)findViewById(R.id.tv_status)).setVisibility(View.VISIBLE);
                        switchStatus.setVisibility(View.VISIBLE);
                        switchStatus.setChecked(isGoing);
                    }
                }
            }
            inviteesListView.setAdapter(adapter);

            setListViewHeightBasedOnChildren(inviteesListView, adapter);
        }
    }

    private void setListViewHeightBasedOnChildren(ListView listView, ArrayAdapter adapter) {
        int totalHeight = 0;
        // iterate through adapter
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // set its height
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
