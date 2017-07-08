package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import tcd.android.com.makeaplan.Adapter.PlanOptionListAdapter;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.PlanOption;

public class AddGroupPlanActivity extends AppCompatActivity {
    private static final String TAG_LOG = "AddGroupPlanActivity";
    private static final int RC_PLACE_PICKER = 1;

    // group plan option list view
    private ListView optionListView;
    private PlanOptionListAdapter optionListAdapter;

    // firebase components
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference groupPlanDatabaseRef;

    // other variables
    private String userId;
    private GroupPlan groupPlan;            // this contains the result
    private Calendar selectedDate = Calendar.getInstance();
    private int locationOptionIndex = -1;   // this is the index of location option in list view
    private String dateFormatPref;
    private String timeFormatPref;

    // manage friends list
    HashMap<String, String> friendsList;
    String[] friendsIdList;
    String[] friendsNameList;
    boolean[] checkedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_plan);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeBasicComponents();
        initializeFirebaseComponents();
        initializeGroupPlanOptionListView();
        retrieveFriendsListFromFirebase();

        optionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlanOption option = ((PlanOption)parent.getAdapter().getItem(position));
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


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get task name
                String taskName = ((EditText)findViewById(R.id.edt_task_name)).getText().toString();
                if (taskName.length() == 0) {
                    Snackbar.make(view, getResources().getString(R.string.name_empty_error), Snackbar.LENGTH_LONG).show();
                    return;
                }
                groupPlan.setName(taskName);
                // validate location
                if (groupPlan.getPlaceLatLng() == null) {
                    Snackbar.make(view, getResources().getString(R.string.location_empty_error), Snackbar.LENGTH_LONG).show();
                    return;
                }
                // get group plan ID
                String groupPlanId = groupPlanDatabaseRef.push().getKey();
                // upload data to Firebase
                groupPlanDatabaseRef.child(groupPlanId).setValue(groupPlan);
                createPlanInSingleInvitee(userId, groupPlanId);
                for (String inviteeId : groupPlan.getinvitees().keySet()) {
                    createPlanInSingleInvitee(inviteeId, groupPlanId);
                }

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

    private void initializeBasicComponents() {
        userId = getIntent().getStringExtra("userId");

        // get chosen format from Settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_DATE_FORMAT, "");
        timeFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_TIME_FORMAT, "");

        groupPlan = new GroupPlan("",
                getFormattedDate(selectedDate, dateFormatPref),
                getFormattedDate(selectedDate, timeFormatPref),
                getResources().getString(R.string.group),
                userId);
    }

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference().child("users");
        groupPlanDatabaseRef = firebaseDatabase.getReference().child("groupPlan");
    }

    private void initializeGroupPlanOptionListView() {
        optionListView = (ListView) findViewById(R.id.lv_group_plan_option);
        optionListAdapter = new PlanOptionListAdapter(this);
        optionListView.setAdapter(optionListAdapter);
        // due date option
        String today = getFormattedDate(selectedDate, dateFormatPref);
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.due_date),
                today, R.drawable.ic_date_black_48px));
        // time option
        String currentTime = getFormattedDate(selectedDate, timeFormatPref);
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.time),
                currentTime, R.drawable.ic_time_black_48px));
        // location option
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.location),
                "", R.drawable.ic_location_black_48px));
        // friends option
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.invitees),
                "0 invitees", R.drawable.ic_invitee_black_48px));
    }

    private String getFormattedDate(Calendar date, String format) {
        return new SimpleDateFormat(format).format(date.getTime());
    }

    private void choosePlanDueDate(final PlanOption option) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddGroupPlanActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        option.setValue(getFormattedDate(selectedDate, dateFormatPref));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        groupPlan.setDate(getFormattedDate(selectedDate, dateFormatPref));
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void choosePlanTime(final PlanOption option) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddGroupPlanActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        option.setValue(getFormattedDate(selectedDate, timeFormatPref));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        groupPlan.setTime(getFormattedDate(selectedDate, timeFormatPref));
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1,
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void choosePlanLocation() {
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

    private void chooseInvitees(final PlanOption option) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.invitees));

        // add a checkbox list
        builder.setMultiChoiceItems(friendsNameList, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
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
                HashMap<String, String> invitees = new HashMap<String, String>();
                int count = 0;
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]){
                        invitees.put(friendsIdList[i], friendsNameList[i]);
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

    private void retrieveFriendsListFromFirebase() {
        userDatabaseRef.child(userId).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                friendsList = dataSnapshot.getValue(t);
                friendsIdList = friendsList.keySet().toArray(new String[0]);
                friendsNameList = friendsList.values().toArray(new String[0]);
                checkedItems = new boolean[friendsList.size()];         // default value is false
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void createPlanInSingleInvitee(final String inviteeId, final String groupPlanId) {
        userDatabaseRef.child(inviteeId).child("plans").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get current plans list from Firebase
                GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                HashMap<String, String> plansRef = dataSnapshot.getValue(t);
                if (plansRef == null) {
                    plansRef = new HashMap<String, String>();
                }
                // put new plan to map
                plansRef.put(groupPlanId, getResources().getString(R.string.group));
                // and update it to Firebase
                userDatabaseRef.child(inviteeId).child("plans").setValue(plansRef);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
                            + String.valueOf(place.getLatLng().longitude));
                    groupPlan.setPlaceAddress(String.valueOf(place.getAddress()));
                    // update its info
                    ((PlanOption)optionListView.getAdapter().getItem(locationOptionIndex)).setValue(placeName);
                    ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                }
                break;
        }
    }
}
