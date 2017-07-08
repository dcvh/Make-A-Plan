package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import tcd.android.com.makeaplan.Adapter.PlanOptionListAdapter;
import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.PersonalPlan;
import tcd.android.com.makeaplan.Entities.PlanOption;

public class AddPersonalPlanActivity extends AppCompatActivity {

    private static final String TAG_LOG = "AddPersonalPlanActivity";
    private static final int RC_PHOTO_PICKER = 1;

    // firebase components
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDatabaseRef;
    private DatabaseReference personalPlanDatabaseRef;
    private FirebaseStorage firebaseStorage;
    private StorageReference mPlanImageStorageRef;

    // other variables
    private String userId;
    private PersonalPlan personalPlan;            // this contains the result
    private Calendar selectedDate = Calendar.getInstance();
    private String dateFormatPref;
    private String timeFormatPref;
    private ImageView planImageView;
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_personal_plan);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        initializeBasicComponents();
        initializeFirebaseComponents();

        planImageView = (ImageView) findViewById(R.id.iv_personal_plan_image);
        planImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb = new AlertDialog.Builder(AddPersonalPlanActivity.this)
                        .setMessage(R.string.remove_image_warning)
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                planImageView.setVisibility(View.GONE);
                            }
                        }).setNegativeButton(getResources().getString(R.string.cancel), null);
                adb.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_personal_plan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.done_menu:
                if (validateUserInputs() == false) {
                    break;
                }
                uploadPlanDataToFirebase();
                break;
            case R.id.reminder_menu:
                choosePersonalPlanDateAndTime();
                break;
            case R.id.take_photo_menu:
                GlobalMethod.showUnderDevelopmentDialog(this);
                break;
            case R.id.gallery_menu:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadPlanDataToFirebase() {
        // upload image to firebase
        StorageReference photoRef = mPlanImageStorageRef.child(selectedImageUri.getLastPathSegment());
        UploadTask uploadTask = photoRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.uploading_message), Snackbar.LENGTH_LONG)
                        .show();
                personalPlan.setImageUrl(downloadUrl.toString());
                // get personal plan ID
                String personalPlanId = personalPlanDatabaseRef.push().getKey();
                // upload data to Firebase
                personalPlanDatabaseRef.child(personalPlanId).setValue(personalPlan);
                createPlanInSingleInvitee(userId, personalPlanId);

                finish();
            }
        });
    }

    private void initializeBasicComponents() {
        userId = getIntent().getStringExtra("userId");

        // get chosen format from Settings
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dateFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_DATE_FORMAT, "");
        timeFormatPref = sharedPref.getString(SettingsActivity.KEY_PREF_TIME_FORMAT, "");

        // initialize base result
        personalPlan = new PersonalPlan("",
                getFormattedDate(selectedDate, dateFormatPref),
                getFormattedDate(selectedDate, timeFormatPref),
                getResources().getString(R.string.personal),
                userId);

        // update date and time
        ((TextView)findViewById(R.id.tv_personal_plan_date)).setText(personalPlan.getDate());
        ((TextView)findViewById(R.id.tv_personal_plan_time)).setText(personalPlan.getTime());
    }

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference().child("users");
        personalPlanDatabaseRef = firebaseDatabase.getReference().child("personalPlan");

        firebaseStorage = FirebaseStorage.getInstance();
        mPlanImageStorageRef = firebaseStorage.getReference().child("personalPlan");
    }

    private boolean validateUserInputs() {
        // validate task name
        String taskName = ((EditText)findViewById(R.id.edt_task_name)).getText().toString();
        if (taskName.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.name_empty_error), Snackbar.LENGTH_LONG)
                    .show();
            return false;
        }
        personalPlan.setName(taskName);
        // validate note content
        String noteContent = ((EditText)findViewById(R.id.edt_note_content)).getText().toString();
        if (noteContent.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.note_empty_error), Snackbar.LENGTH_LONG)
                    .show();
            return false;
        }
        personalPlan.setNote(noteContent);
        return true;
    }

    private void choosePersonalPlanDateAndTime() {
        // date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddPersonalPlanActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        // time picker dialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddPersonalPlanActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        selectedDate.set(Calendar.MINUTE, minute);
                                        // save result
                                        personalPlan.setDate(getFormattedDate(selectedDate, dateFormatPref));
                                        personalPlan.setTime(getFormattedDate(selectedDate, timeFormatPref));
                                        ((TextView)findViewById(R.id.tv_personal_plan_date)).setText(personalPlan.getDate());
                                        ((TextView)findViewById(R.id.tv_personal_plan_time)).setText(personalPlan.getTime());
                                    }
                                },
                                Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1,
                                Calendar.getInstance().get(Calendar.MINUTE),
                                false);
                        timePickerDialog.show();
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String getFormattedDate(Calendar date, String format) {
        return new SimpleDateFormat(format).format(date.getTime());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_PHOTO_PICKER:
                if (resultCode == RESULT_OK) {
                    selectedImageUri = data.getData();
                    // display selected image
                    planImageView.setImageURI(selectedImageUri);
                    planImageView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
