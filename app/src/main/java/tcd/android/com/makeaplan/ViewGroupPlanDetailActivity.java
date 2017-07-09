package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.GroupPlan;

public class ViewGroupPlanDetailActivity extends AppCompatActivity {

    private GroupPlan groupPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_plan_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        groupPlan = (GroupPlan) getIntent().getSerializableExtra(getString(R.string.group));

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
                onBackPressed();
                break;
            case R.id.edit_menu:
                GlobalMethod.showUnderDevelopmentDialog(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayGroupPlanInfo() {
        ((TextView)findViewById(R.id.tv_group_plan_name)).setText(groupPlan.getName());
        ((TextView)findViewById(R.id.tv_group_plan_date))
                .setText(GlobalMethod.getDateFromMilliseconds(groupPlan.getDateTime(), this));
        ((TextView)findViewById(R.id.tv_group_plan_time))
                .setText(GlobalMethod.getTimeFromMilliseconds(groupPlan.getDateTime(), this));
        ((TextView)findViewById(R.id.tv_group_plan_address)).setText(groupPlan.getPlaceAddress());

        // retrieve owner name from its ID
        DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_users));
        userDatabaseRef.child(groupPlan.getOwner()).child(getString(R.string.firebase_name))
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
            String attendance = invitees.size() + " " + getString(R.string.invitee);
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
