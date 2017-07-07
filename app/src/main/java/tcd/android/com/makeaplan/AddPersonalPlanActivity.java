package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import tcd.android.com.makeaplan.Entities.PersonalPlan;
import tcd.android.com.makeaplan.Entities.PlanOption;

public class AddPersonalPlanActivity extends AppCompatActivity {

    private static final String TAG_LOG = "AddPersonalPlanActivity";
    private static final int RC_PLACE_PICKER = 1;

    // personal plan option list view
    private ListView optionListView;
    private PlanOptionListAdapter optionListAdapter;

    // firebase components
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference personalPlanDatabaseRef;

    private EditText taskNameEditText;

    // other variables
    private String userId;
    private PersonalPlan personalPlan;            // this contains the result
    private Calendar selectedDate = Calendar.getInstance();
    private String dateFormatPref;
    private String timeFormatPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_plan);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeBasicComponents();
        initializeFirebaseComponents();
        initializePersonalPlanOptionListView();

        optionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlanOption option = ((PlanOption)parent.getAdapter().getItem(position));
                String title = option.getTitle();
                if (title.equals(getResources().getString(R.string.due_date))) {
                    choosePlanDueDate(option);
                } else if (title.equals(getResources().getString(R.string.time))) {
                    choosePlanTime(option);
                }
            }
        });


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
                personalPlan.setName(taskName);
                // get personal plan ID
                String personalPlanId = personalPlanDatabaseRef.push().getKey();
                // upload data to Firebase
                personalPlanDatabaseRef.child(personalPlanId).setValue(personalPlan);
                createPlanInSingleInvitee(userId, personalPlanId);

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

        taskNameEditText = (EditText) findViewById(R.id.edt_task_name);

        personalPlan = new PersonalPlan("",
                getFormattedDate(selectedDate, dateFormatPref),
                getFormattedDate(selectedDate, timeFormatPref),
                getResources().getString(R.string.personal),
                userId);
    }

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference().child("users");
        personalPlanDatabaseRef = firebaseDatabase.getReference().child("personalPlan");
    }

    private void initializePersonalPlanOptionListView() {
        optionListView = (ListView) findViewById(R.id.lv_personal_plan_option);
        optionListAdapter = new PlanOptionListAdapter(this);
        optionListView.setAdapter(optionListAdapter);
        // due date option
        String today = getFormattedDate(selectedDate, dateFormatPref);
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.due_date),
                today, android.R.drawable.ic_menu_today));
        // time option
        String currentTime = getFormattedDate(selectedDate, timeFormatPref);
        optionListAdapter.add(new PlanOption(getResources().getString(R.string.time),
                currentTime, android.R.drawable.ic_lock_idle_alarm));
    }

    private String getFormattedDate(Calendar date, String format) {
        return new SimpleDateFormat(format).format(date.getTime());
    }

    private void choosePlanDueDate(final PlanOption option) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddPersonalPlanActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        option.setValue(getFormattedDate(selectedDate, dateFormatPref));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        personalPlan.setDate(getFormattedDate(selectedDate, dateFormatPref));
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void choosePlanTime(final PlanOption option) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AddPersonalPlanActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDate.set(Calendar.MINUTE, minute);
                        option.setValue(getFormattedDate(selectedDate, timeFormatPref));
                        ((BaseAdapter)optionListView.getAdapter()).notifyDataSetChanged();
                        // save it
                        personalPlan.setTime(getFormattedDate(selectedDate, timeFormatPref));
                    }
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1,
                Calendar.getInstance().get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void createPlanInSingleInvitee(final String inviteeId, final String personalPlanId) {
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
                plansRef.put(personalPlanId, getResources().getString(R.string.personal));
                // and update it to Firebase
                userDatabaseRef.child(inviteeId).child("plans").setValue(plansRef);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
