package tcd.android.com.makeaplan;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import tcd.android.com.makeaplan.Adapter.PlanListAdapter;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.Entities.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;

    // firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserDatabaseRef;
    private DatabaseReference mGroupPlanDatabaseRef;
    private FirebaseStorage mFirebaseStorage;
    private ChildEventListener mChildEventListener;

    // plan list view components
    private ListView planListView;
    private PlanListAdapter planListAdapter;

    private String userId = null;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeBasicComponents();
        initializeFirebaseAuthentication();
        initializeFirebaseComponents();

        // personal plan action
        FloatingActionButton personalFAB = (FloatingActionButton) findViewById(R.id.fab_personal);
        personalFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // group plan action
        FloatingActionButton groupFAB = (FloatingActionButton) findViewById(R.id.fab_group);
        groupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddGroupPlanActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.settings_menu:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (resultCode == ResultCodes.CANCELED) {
                    // User cancelled
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.cancel_sign_in_message),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void initializeBasicComponents() {
        planListView = (ListView) findViewById(R.id.plan_list_view);
        planListAdapter = new PlanListAdapter(this);
        planListView.setAdapter(planListAdapter);
        planListAdapter.add(new Plan("University of Science", "03/07/2017", "11:21 PM", "Personal"));
        planListAdapter.add(new Plan("University of Technology", "04/07/2017", "11:21 AM", "Group"));
    }

    private void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserDatabaseRef = mFirebaseDatabase.getReference().child("users");
        mGroupPlanDatabaseRef = mFirebaseDatabase.getReference().child("groupPlan");

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    private void initializeFirebaseAuthentication() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    onSignedOutCleanUp();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                } else {
                    if (userId == null) {
                        userId = firebaseUser.getUid();
                        // save user to database if this is a new user
                        if (mUserDatabaseRef.child(userId) == null) {
                            mUserDatabaseRef.child(userId)
                                    .setValue(new User(firebaseUser.getDisplayName(), firebaseUser.getEmail()));
                        }
                        // retrieve user info
                        mUserDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                user = dataSnapshot.getValue(User.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    onSignedInInitialize();
                }
            }
        };
    }

    private void onSignedInInitialize() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    planListAdapter.clear();
                    createPlanList(dataSnapshot);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    planListAdapter.clear();
                    createPlanList(dataSnapshot);
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    planListAdapter.clear();
                    createPlanList(dataSnapshot);
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mUserDatabaseRef.child(userId).child("plans").addChildEventListener(mChildEventListener);
        }
    }

    private void onSignedOutCleanUp() {
        if (mChildEventListener != null) {
            mUserDatabaseRef.child(userId).child("plans").removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        planListAdapter.clear();
    }

    private void createPlanList(DataSnapshot dataSnapshot) {
        // get the plan
        if (dataSnapshot.getValue(String.class).equals(getResources().getString(R.string.group))) {
            mGroupPlanDatabaseRef.child(dataSnapshot.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GroupPlan groupPlan = dataSnapshot.getValue(GroupPlan.class);
                            planListAdapter.add(groupPlan);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }
}
