package tcd.android.com.makeaplan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.internal.PlaceEntity;
import com.google.android.gms.location.places.internal.zzy;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import tcd.android.com.makeaplan.Adapter.GroupPlanOptionListAdapter;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.GroupPlanOption;

public class AddGroupPlanActivity extends AppCompatActivity {

    private static final int RC_PLACE_PICKER = 1;

    // group plan option list view
    private ListView optionListView;
    private GroupPlanOptionListAdapter optionListAdapter;

    private EditText taskNameEditText;

    private GroupPlan groupPlan;
    private Calendar selectedDate = Calendar.getInstance();
    private int positionOptionIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_plan);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeGroupPlanOptionListView();
        optionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupPlanOption option = ((GroupPlanOption)parent.getAdapter().getItem(position));
                String title = option.getTitle();
                if (title.equals(getResources().getString(R.string.due_date))) {
                    choosePlanDueDate(option);
                } else if (title.equals(getResources().getString(R.string.time))) {
                    choosePlanTime(option);
                } else if (title.equals(getResources().getString(R.string.location))) {
                    positionOptionIndex = position;
                    choosePlanLocation();
                }
            }
        });

        taskNameEditText = (EditText) findViewById(R.id.edt_task_name);
        groupPlan = new GroupPlan("", Calendar.getInstance(), getResources().getString(R.string.group));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String taskName = taskNameEditText.getText().toString();
                if (taskName.length() == 0) {
                    Snackbar.make(view, getResources().getString(R.string.name_empty_error), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    return;
                }
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra(getResources().getString(R.string.group), groupPlan);
//                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void initializeGroupPlanOptionListView() {
        optionListView = (ListView) findViewById(R.id.lv_group_plan_option);
        optionListAdapter = new GroupPlanOptionListAdapter(this);
        optionListView.setAdapter(optionListAdapter);
        // due date option
        String today = getFormattedDate(selectedDate, "dd/MM/yyyy");
        optionListAdapter.add(new GroupPlanOption(getResources().getString(R.string.due_date),
                today, android.R.drawable.ic_menu_today));
        // time option
        String currentTime = getFormattedDate(selectedDate, "hh:mm a");
        optionListAdapter.add(new GroupPlanOption(getResources().getString(R.string.time),
                currentTime, android.R.drawable.ic_lock_idle_alarm));
        // location option
        optionListAdapter.add(new GroupPlanOption(getResources().getString(R.string.location),
                "University of Science", android.R.drawable.ic_menu_mylocation));
    }

    String getFormattedDate(Calendar date, String format) {
        return new SimpleDateFormat(format).format(date.getTime());
    }

    void choosePlanDueDate(final GroupPlanOption option) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddGroupPlanActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        option.setValue(getFormattedDate(selectedDate, "dd/MM/yyyy"));
                        groupPlan.setDate(selectedDate);
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    void choosePlanTime(final GroupPlanOption option) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddGroupPlanActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        option.setValue(getFormattedDate(selectedDate, "hh:mm a"));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        groupPlan.setDate(selectedDate);
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1,
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    void choosePlanLocation() {
        // picking a place (using google Places API)
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(AddGroupPlanActivity.this), RC_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    // save the place and display its name in Toast
                    Place place = PlacePicker.getPlace(data, AddGroupPlanActivity.this);
                    groupPlan.setPlace(place);
                    String placeName = String.format("%s", place.getName());
                    ((GroupPlanOption)optionListView.getAdapter().getItem(positionOptionIndex)).setValue(placeName);
                    ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                }
                break;
        }
    }
}
