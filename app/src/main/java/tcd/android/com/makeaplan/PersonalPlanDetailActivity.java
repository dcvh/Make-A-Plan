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

import tcd.android.com.makeaplan.Entities.PersonalPlan;

public class PersonalPlanDetailActivity extends AppCompatActivity {

    private PersonalPlan personalPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_plan_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personalPlan = (PersonalPlan) getIntent().getSerializableExtra(getResources().getString(R.string.personal));

        displaypersonalPlanInfo();
    }

    private void displaypersonalPlanInfo() {
        ((TextView)findViewById(R.id.tv_personal_plan_name)).setText(personalPlan.getName());
        ((TextView)findViewById(R.id.tv_personal_plan_date)).setText(personalPlan.getDate());
        ((TextView)findViewById(R.id.tv_personal_plan_time)).setText(personalPlan.getTime());
    }
}
