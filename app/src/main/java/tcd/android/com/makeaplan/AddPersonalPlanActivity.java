package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
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

import java.io.File;
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
    private static final int RC_TAKE_PHOTO = 2;

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
                        .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                planImageView.setVisibility(View.GONE);
                            }
                        }).setNegativeButton(getString(R.string.cancel_button), null);
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
                if (!validateUserInputs()) {
                    break;
                }
                uploadPlanDataToFirebase();
                break;
            case R.id.reminder_menu:
                choosePersonalPlanDateAndTime();
                break;
            case R.id.take_photo_menu:
                selectedImageUri = Uri.fromFile(new File(getExternalFilesDir(null), "image.jpg"));
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(cameraIntent, getString(R.string.complete_action_using_label)), RC_TAKE_PHOTO);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_app_handle_intent_error), Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.gallery_menu:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using_label)), RC_PHOTO_PICKER);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_app_handle_intent_error), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadPlanDataToFirebase() {
        // get personal plan ID
        final String personalPlanId = personalPlanDatabaseRef.push().getKey();
        personalPlan.setId(personalPlanId);
        // upload image to firebase
        if (planImageView.getVisibility() == View.VISIBLE) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.uploading_message), Snackbar.LENGTH_LONG).show();
            // start uploading image
            StorageReference photoRef = mPlanImageStorageRef.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = photoRef.putFile(selectedImageUri);
            uploadTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    personalPlan.setImageUrl(downloadUrl.toString());
                    // upload data to Firebase
                    personalPlanDatabaseRef.child(personalPlanId).setValue(personalPlan);
                    createPlanInSingleInvitee(userId, personalPlanId);
                    finish();
                }
            });
        } else {
            // upload data to Firebase
            personalPlanDatabaseRef.child(personalPlanId).setValue(personalPlan);
            createPlanInSingleInvitee(userId, personalPlanId);
            finish();
        }
    }

    private void initializeBasicComponents() {
        userId = getIntent().getStringExtra(getString(R.string.account_id));

        // initialize base result
        personalPlan = new PersonalPlan("",
                selectedDate.getTimeInMillis(),
                getString(R.string.personal),
                userId);

        // update date and time
        ((TextView)findViewById(R.id.tv_personal_plan_date))
                .setText(GlobalMethod.getDateFromMilliseconds(personalPlan.getDateTime(), this));
        ((TextView)findViewById(R.id.tv_personal_plan_time))
                .setText(GlobalMethod.getTimeFromMilliseconds(personalPlan.getDateTime(), this));
    }

    private void initializeFirebaseComponents() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDatabaseRef = firebaseDatabase.getReference().child(getString(R.string.firebase_users));
        personalPlanDatabaseRef = firebaseDatabase.getReference().child(getString(R.string.firebase_personal_plan));

        firebaseStorage = FirebaseStorage.getInstance();
        mPlanImageStorageRef = firebaseStorage.getReference().child(getString(R.string.firebase_personal_plan));
    }

    private boolean validateUserInputs() {
        // check network requirement
        if (!GlobalMethod.isNetworkConnected(AddPersonalPlanActivity.this)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.feature_requires_network_error), Snackbar.LENGTH_LONG).show();
            return false;
        }
        // validate task name
        String taskName = ((EditText)findViewById(R.id.edt_task_name)).getText().toString();
        if (taskName.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.name_empty_error), Snackbar.LENGTH_LONG)
                    .show();
            return false;
        }
        personalPlan.setName(taskName);
        // validate note content
        String noteContent = ((EditText)findViewById(R.id.edt_note_content)).getText().toString();
        if (noteContent.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.note_empty_error), Snackbar.LENGTH_LONG)
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
                                        personalPlan.setDateTime(selectedDate.getTimeInMillis());
                                        // update date and time
                                        ((TextView)findViewById(R.id.tv_personal_plan_date)).setText(GlobalMethod
                                                .getDateFromMilliseconds(personalPlan.getDateTime(), AddPersonalPlanActivity.this));
                                        ((TextView)findViewById(R.id.tv_personal_plan_time)).setText(GlobalMethod
                                                .getTimeFromMilliseconds(personalPlan.getDateTime(), AddPersonalPlanActivity.this));
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

    private void createPlanInSingleInvitee(final String inviteeId, final String personalPlanId) {
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
                plansRef.put(personalPlanId, getString(R.string.personal));
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
            case RC_TAKE_PHOTO:
                Log.e("aaa", "onActivityResult: ");
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, selectedImageUri.toString(), Toast.LENGTH_SHORT).show();
                    // display selected image
                    Glide.with(this).load(selectedImageUri).into(planImageView);
                    planImageView.setVisibility(View.VISIBLE);
                }
                break;
            case RC_PHOTO_PICKER:
                if (resultCode == RESULT_OK) {
                    selectedImageUri = data.getData();
                    // display selected image
                    Glide.with(this).load(selectedImageUri).into(planImageView);
                    planImageView.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
