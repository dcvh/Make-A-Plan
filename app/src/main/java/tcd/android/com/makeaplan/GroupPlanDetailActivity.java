package tcd.android.com.makeaplan;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import tcd.android.com.makeaplan.Entities.GroupPlan;

public class GroupPlanDetailActivity extends AppCompatActivity {

    private GroupPlan groupPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_plan_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        groupPlan = (GroupPlan) getIntent().getSerializableExtra(getResources().getString(R.string.group));

        displayGroupPlanInfo();

        // get the location image
        String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                + groupPlan.getPlaceLatLng()
                + "&zoom=16&size=1000x300";
        ImageView locationImageView = (ImageView) findViewById(R.id.iv_group_plan_location);
        Picasso.with(this).load(imageUrl).fit().centerCrop().into(locationImageView);

        // navigate button
        ((TextView)findViewById(R.id.tv_navigate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + groupPlan.getPlaceLatLng()));
                startActivity(intent);
            }
        });

        displayInvitees();
    }

    private void displayGroupPlanInfo() {
        ((TextView)findViewById(R.id.tv_group_plan_name)).setText(groupPlan.getName());
        ((TextView)findViewById(R.id.tv_group_plan_date)).setText(groupPlan.getDate());
        ((TextView)findViewById(R.id.tv_group_plan_time)).setText(groupPlan.getTime());
        ((TextView)findViewById(R.id.tv_group_plan_address)).setText(groupPlan.getPlaceAddress());

        // retrieve owner name from its ID
        DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabaseRef.child(groupPlan.getOwner()).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ((TextView) findViewById(R.id.tv_group_plan_owner)).setText(dataSnapshot.getValue(String.class));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void displayInvitees() {
        HashMap invitees = groupPlan.getinvitees();
        if (invitees != null) {
            // attendance
            String attendance = invitees.size() + " invitee" + (invitees.size() > 1 ? "s" : "");
            ((TextView)findViewById(R.id.tv_group_plan_attendance)).setText(attendance);
            // invitees list
            ListView inviteesListView = (ListView) findViewById(R.id.lv_group_plan_invitees);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1,
                    groupPlan.getinvitees().values().toArray(new String[0]));
            inviteesListView.setAdapter(adapter);
        }
    }
}
