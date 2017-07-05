package tcd.android.com.makeaplan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.TagLostException;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import tcd.android.com.makeaplan.Adapter.GroupPlanOptionListAdapter;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.GroupPlanOption;

public class AddGroupPlanActivity extends AppCompatActivity {
    private static final String TAG_LOG = "AddGroupPlanActivity";
    private static final int RC_PLACE_PICKER = 1;

    // group plan option list view
    private ListView optionListView;
    private GroupPlanOptionListAdapter optionListAdapter;

    // firebase components
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference groupPlanDatabaseRef;

    // other components
    private EditText taskNameEditText;
    private String userId;
    private GroupPlan groupPlan;            // this contains the result
    private Calendar selectedDate = Calendar.getInstance();
    private int locationOptionIndex = -1;   // this is the index of location option in list view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_plan);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getIntent().getStringExtra("userId");

        initializeFirebaseComponents();

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
                    locationOptionIndex = position;
                    choosePlanLocation();
                } else if (title.equals(getResources().getString(R.string.invitees))) {
                    chooseInvitees(option);
                }
            }
        });

        taskNameEditText = (EditText) findViewById(R.id.edt_task_name);
        groupPlan = new GroupPlan("",
                getFormattedDate(selectedDate, "dd/MM/yyyy"),
                getFormattedDate(selectedDate, "hh:mm a"),
                getResources().getString(R.string.group),
                userId);

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
                groupPlan.setName(taskName);
                // upload to Firebase
                groupPlanDatabaseRef.push().setValue(groupPlan);
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

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        groupPlanDatabaseRef = firebaseDatabase.getReference().child("groupPlan");
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
                "", android.R.drawable.ic_menu_mylocation));
        // friends option
        optionListAdapter.add(new GroupPlanOption(getResources().getString(R.string.invitees),
                "0 invitees", android.R.drawable.ic_menu_myplaces));
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
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        groupPlan.setDate(getFormattedDate(selectedDate, "dd/MM/yyyy"));
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
                        // save it
                        groupPlan.setTime(getFormattedDate(selectedDate, "hh:mm a"));
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

    private void chooseInvitees(final GroupPlanOption option) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.invitees));

        // add a checkbox list
        final String[] friends = {"horse", "cow", "camel", "sheep", "goat"};
        final boolean[] checkedItems = {false, false, false, false, false};
        builder.setMultiChoiceItems(friends, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // user checked or unchecked a box
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                ArrayList<String> invitees = new ArrayList<String>();
                int count = 0;
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]){
                        invitees.add(friends[i]);
                        count++;
                    }
                }
                groupPlan.setinvitees(invitees);
                option.setValue(String.valueOf(count) + " invitees");
                ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PLACE_PICKER:
                if (resultCode == RESULT_OK) {
                    // get place info
                    Place place = PlacePicker.getPlace(data, AddGroupPlanActivity.this);
                    String placeName = String.format("%s", place.getName());
                    // save it
                    groupPlan.setPlaceName(placeName);
                    groupPlan.setPlaceLatLng(String.valueOf(place.getLatLng().latitude) + ","
                            + String.valueOf(place.getLatLng().latitude));
                    groupPlan.setPlaceAddress(String.valueOf(place.getAddress()));
                    // update its info
                    ((GroupPlanOption)optionListView.getAdapter().getItem(locationOptionIndex)).setValue(placeName);
                    ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                }
                break;
        }
    }
}
