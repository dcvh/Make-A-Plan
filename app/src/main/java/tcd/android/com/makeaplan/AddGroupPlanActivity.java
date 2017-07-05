package tcd.android.com.makeaplan;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import tcd.android.com.makeaplan.Adapter.GroupPlanOptionListAdapter;
import tcd.android.com.makeaplan.Entities.GroupPlanOption;

public class AddGroupPlanActivity extends AppCompatActivity {

    private ListView optionListView;
    private GroupPlanOptionListAdapter optionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_plan);

        optionListView = (ListView) findViewById(R.id.lv_group_plan_option);
        optionListAdapter = new GroupPlanOptionListAdapter(this);
        optionListView.setAdapter(optionListAdapter);
        optionListAdapter.add(new GroupPlanOption("Due date", "Today", android.R.drawable.ic_menu_today));
        optionListAdapter.add(new GroupPlanOption("Time", "9:00AM", android.R.drawable.ic_lock_idle_alarm));
        optionListAdapter.add(new GroupPlanOption("Location", "University of Science", android.R.drawable.ic_menu_mylocation));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
