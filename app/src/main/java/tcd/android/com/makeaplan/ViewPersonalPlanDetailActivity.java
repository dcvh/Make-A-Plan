package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.PersonalPlan;

public class ViewPersonalPlanDetailActivity extends AppCompatActivity {

    private PersonalPlan personalPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_personal_plan_detail);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        personalPlan = (PersonalPlan) getIntent().getSerializableExtra(getResources().getString(R.string.personal));

        displaypersonalPlanInfo();

        if (personalPlan.getImageUrl() != null) {
            ImageView planImageView = (ImageView) findViewById(R.id.iv_personal_plan_image);
            planImageView.setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tv_personal_plan_image_label)).setVisibility(View.VISIBLE);
            Glide.with(this).load(personalPlan.getImageUrl()).dontAnimate().into(planImageView);
        }
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

    private void displaypersonalPlanInfo() {
        ((TextView)findViewById(R.id.tv_personal_plan_name)).setText(personalPlan.getName());
        ((TextView)findViewById(R.id.tv_personal_plan_date))
                .setText(GlobalMethod.getDateFromMilliseconds(personalPlan.getDateTime(), this));
        ((TextView)findViewById(R.id.tv_personal_plan_time))
                .setText(GlobalMethod.getTimeFromMilliseconds(personalPlan.getDateTime(), this));
        ((TextView)findViewById(R.id.tv_personal_plan_note)).setText(personalPlan.getNote());
    }
}
