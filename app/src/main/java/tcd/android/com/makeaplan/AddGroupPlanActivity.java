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
import tcd.android.com.makeaplan.Entities.GlobalMethod;
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
                if (title.equals(getString(R.string.due_date_label))) {
                    choosePlanDueDate(option);
                } else if (title.equals(getString(R.string.time_label))) {
                    choosePlanTime(option);
                } else if (title.equals(getString(R.string.location_label))) {
                    locationOptionIndex = position;
                    choosePlanLocation();
                } else if (title.equals(getString(R.string.invitees_label))) {
                    chooseInvitees(option);
                }
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateUserInputs()) {
                    return;
                }
                // get group plan ID
                String groupPlanId = groupPlanDatabaseRef.push().getKey();
                groupPlan.setId(groupPlanId);
                // upload data to Firebase
                groupPlanDatabaseRef.child(groupPlanId).setValue(groupPlan);
                createPlanInSingleInvitee(userId, groupPlanId);
                if (groupPlan.getInvitees() != null) {
                    for (String inviteeId : groupPlan.getInvitees().keySet()) {
                        createPlanInSingleInvitee(inviteeId, groupPlanId);
                    }
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
        userId = getIntent().getStringExtra(getString(R.string.account_id));
        
        groupPlan = new GroupPlan("",
                selectedDate.getTimeInMillis(),
                getString(R.string.group),
                userId);
    }

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference().child(getString(R.string.firebase_users));
        groupPlanDatabaseRef = firebaseDatabase.getReference().child(getString(R.string.firebase_group_plan));
    }

    private void initializeGroupPlanOptionListView() {
        optionListView = (ListView) findViewById(R.id.lv_group_plan_option);
        optionListAdapter = new PlanOptionListAdapter(this);
        optionListView.setAdapter(optionListAdapter);
        // due date option
        optionListAdapter.add(new PlanOption(getString(R.string.due_date_label),
                GlobalMethod.getDateFromMilliseconds(selectedDate.getTimeInMillis(), this),
                R.drawable.ic_date_black_48px));
        // time option
        optionListAdapter.add(new PlanOption(getString(R.string.time_label),
                GlobalMethod.getTimeFromMilliseconds(selectedDate.getTimeInMillis(), this),
                R.drawable.ic_time_black_48px));
        // location option
        optionListAdapter.add(new PlanOption(getString(R.string.location_label),
                "", R.drawable.ic_location_black_48px));
        // friends option
        optionListAdapter.add(new PlanOption(getString(R.string.invitees_label),
                getString(R.string.no_invitee_label), R.drawable.ic_invitee_black_48px));
    }

    private void choosePlanDueDate(final PlanOption option) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddGroupPlanActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        option.setValue(GlobalMethod
                                .getDateFromMilliseconds(selectedDate.getTimeInMillis(), AddGroupPlanActivity.this));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        groupPlan.setDateTime(selectedDate.getTimeInMillis());
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
                        option.setValue(GlobalMethod
                                .getTimeFromMilliseconds(selectedDate.getTimeInMillis(), AddGroupPlanActivity.this));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        groupPlan.setDateTime(selectedDate.getTimeInMillis());
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1,
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void choosePlanLocation() {
        // check network requirement
        if (!GlobalMethod.isNetworkConnected(AddGroupPlanActivity.this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.feature_requires_network_error), Snackbar.LENGTH_LONG).show();
            return;
        }
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
        // validate list friends
        if (friendsNameList == null) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_friend_error), Snackbar.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.invitees_label));
        // add a checkbox list
        builder.setMultiChoiceItems(friendsNameList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {}
        });
        // add OK and Cancel buttons
        builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
                HashMap<String, String> invitees = new HashMap<String, String>();
                HashMap<String, Integer> inviteesStatus = new HashMap<String, Integer>();
                int count = 0;
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]){
                        invitees.put(friendsIdList[i], friendsNameList[i]);
                        inviteesStatus.put(friendsIdList[i], -1);
                        count++;
                    }
                }
                groupPlan.setInvitees(invitees);
                groupPlan.setInviteesStatus(inviteesStatus);
                option.setValue(String.valueOf(count) + " " + getString(R.string.invitee_label));
                ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_button), null);
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void retrieveFriendsListFromFirebase() {
        userDatabaseRef.child(userId).child(getString(R.string.firebase_friends)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                friendsList = dataSnapshot.getValue(t);
                if (friendsList != null) {
                    friendsIdList = friendsList.keySet().toArray(new String[0]);
                    friendsNameList = friendsList.values().toArray(new String[0]);
                    checkedItems = new boolean[friendsList.size()];         // default value is false
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private boolean validateUserInputs() {
        // check network requirement
        if (!GlobalMethod.isNetworkConnected(AddGroupPlanActivity.this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.feature_requires_network_error), Snackbar.LENGTH_LONG).show();
            return false;
        }
        // validate task name
        String taskName = ((EditText)findViewById(R.id.edt_task_name)).getText().toString();
        if (taskName.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.name_empty_error), Snackbar.LENGTH_LONG).show();
            return false;
        }
        groupPlan.setName(taskName);
        // validate location
        if (groupPlan.getPlaceLatLng() == null) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.location_empty_error), Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void createPlanInSingleInvitee(final String inviteeId, final String groupPlanId) {
        userDatabaseRef.child(inviteeId).child(getString(R.string.firebase_plans)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get current plans list from Firebase
                GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                HashMap<String, String> plansRef = dataSnapshot.getValue(t);
                if (plansRef == null) {
                    plansRef = new HashMap<String, String>();
                }
                // put new plan to map
                plansRef.put(groupPlanId, getString(R.string.group));
                // and update it to Firebase
                userDatabaseRef.child(inviteeId).child(getString(R.string.firebase_plans)).setValue(plansRef);
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
