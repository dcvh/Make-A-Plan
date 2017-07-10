package tcd.android.com.makeaplan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;
import java.util.HashMap;

import tcd.android.com.makeaplan.Adapter.PlanListAdapter;
import tcd.android.com.makeaplan.Entities.GlobalMethod;
import tcd.android.com.makeaplan.Entities.GroupPlan;
import tcd.android.com.makeaplan.Entities.PersonalPlan;
import tcd.android.com.makeaplan.Entities.Plan;
import tcd.android.com.makeaplan.Entities.User;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;
    private static final int RC_VIEW_GROUP_PLAN = 2;

    // firebase components
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserDatabaseRef;
    private DatabaseReference mGroupPlanDatabaseRef;
    private DatabaseReference mPersonalPlanDatabaseRef;
    private FirebaseStorage mFirebaseStorage;
    private ChildEventListener mChildEventListener;

    // plan list view components
    private ListView planListView;
    private PlanListAdapter planListAdapter;

    private String userId = null;
    private User user;
    public static int selectedGroupPlanPosition = -1;

    // dialogs
    public static ProgressDialog downloadProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GlobalMethod.checkNetworkState(this);

        initializeBasicComponents();
        initializeNavigationDrawerComponents(toolbar);
        initializeFirebaseComponents();
        initializeFirebaseAuthentication();

        // personal plan action
        FloatingActionButton personalFAB = (FloatingActionButton) findViewById(R.id.fab_personal);
        personalFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddPersonalPlanActivity.class);
                intent.putExtra(getString(R.string.account_id), userId);
                startActivity(intent);
            }
        });
        // group plan action
        FloatingActionButton groupFAB = (FloatingActionButton) findViewById(R.id.fab_group);
        groupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddGroupPlanActivity.class);
                intent.putExtra(getString(R.string.account_id), userId);
                startActivity(intent);
            }
        });
        // add friend action
        FloatingActionButton addFriendFAB = (FloatingActionButton)findViewById(R.id.fab_add_friend);
        addFriendFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check network requirement
                if (!GlobalMethod.isNetworkConnected(MainActivity.this)) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.feature_requires_network_error), Snackbar.LENGTH_LONG).show();
                    return;
                }
                // initialize zxing scanner
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        });
        // each plan action
        planListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Plan plan = (Plan) parent.getAdapter().getItem(position);
                // a group plan
                if (plan.getTag().equals(getString(R.string.group))) {
                    Intent groupPlanDetailIntent = new Intent(MainActivity.this, ViewGroupPlanDetailActivity.class);
                    groupPlanDetailIntent.putExtra(getString(R.string.group), (GroupPlan)plan);
                    groupPlanDetailIntent.putExtra(getString(R.string.account_id), userId);
                    selectedGroupPlanPosition = position;
                    startActivityForResult(groupPlanDetailIntent, RC_VIEW_GROUP_PLAN);
                }
                // a personal plan
                else if (plan.getTag().equals(getString(R.string.personal))) {
                    Intent personalPlanDetailIntent = new Intent(MainActivity.this, ViewPersonalPlanDetailActivity.class);
                    personalPlanDetailIntent.putExtra(getString(R.string.personal), (PersonalPlan)plan);
                    startActivity(personalPlanDetailIntent);
                }
            }
        });
    }

    private void initializeNavigationDrawerComponents(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(this);
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
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_current:
                break;
            case R.id.nav_overdue:
                GlobalMethod.showUnderDevelopmentDialog(this);
                break;
            case R.id.nav_account_info:
                Intent accountIntent = new Intent(this, MyAccountActivity.class);
                accountIntent.putExtra(getString(R.string.my_account), user);
                startActivity(accountIntent);
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.nav_help_feedback:
                GlobalMethod.showUnderDevelopmentDialog(this);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.KEY_PREF_DATE_FORMAT)) {
            planListView.invalidateViews();
        } else if (key.equals(SettingsActivity.KEY_PREF_TIME_FORMAT)) {
            planListView.invalidateViews();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // add friend via qr code
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (scanResult != null && scanResult.getContents() != null) {
                addNewFriendToFirebase(scanResult.getContents());
            }
        }
        // else continue with any other situation
        else if (requestCode == RC_SIGN_IN) {
            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                Toast.makeText(this, R.string.welcome_message, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (resultCode == ResultCodes.CANCELED) {
                    // User cancelled
                    Toast.makeText(MainActivity.this, R.string.cancel_sign_in_message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
        // updated status value
        else if (requestCode == RC_VIEW_GROUP_PLAN) {
            if (resultCode == ResultCodes.OK) {
                int updatedStatus = data.getIntExtra(getString(R.string.firebase_invitees_status), -1);
                HashMap<String, Integer> inviteesStatus = ((GroupPlan)planListAdapter.getItem(selectedGroupPlanPosition)).getInviteesStatus();
                inviteesStatus.put(userId, updatedStatus);
                ((GroupPlan)planListAdapter.getItem(selectedGroupPlanPosition)).setInviteesStatus(inviteesStatus);
            }
        }
    }

    private void addNewFriendToFirebase(final String friendInfo) {
        mUserDatabaseRef.child(userId).child(getString(R.string.firebase_friends)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, String>> t = new GenericTypeIndicator<HashMap<String, String>>() {};
                HashMap<String, String> friendsList = dataSnapshot.getValue(t);
                String[] results = friendInfo.split(",");
                friendsList.put(results[0], results[1]);
                mUserDatabaseRef.child(userId).child(getString(R.string.firebase_friends)).setValue(friendsList);
                Toast.makeText(MainActivity.this, results[1] + " " + getString(R.string.become_friend_message),
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void initializeBasicComponents() {
        // plans list view
        planListView = (ListView) findViewById(R.id.plan_list_view);
        planListAdapter = new PlanListAdapter(this);
        planListView.setAdapter(planListAdapter);

        // Register the listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeFirebaseComponents() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserDatabaseRef = mFirebaseDatabase.getReference().child(getString(R.string.firebase_users));
        mGroupPlanDatabaseRef = mFirebaseDatabase.getReference().child(getString(R.string.firebase_group_plan));
        mPersonalPlanDatabaseRef = mFirebaseDatabase.getReference().child(getString(R.string.firebase_personal_plan));

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    private void initializeFirebaseAuthentication() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
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
                        // udpate user name and email info
                        user = new User(firebaseUser.getDisplayName(), firebaseUser.getEmail(), userId);
                        // update user info on Firebase
                        mUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // create user to database if this is a new user
                                if (!dataSnapshot.hasChild(userId)) {
                                    mUserDatabaseRef.child(userId).setValue(user);
                                }
                                // retrieve user info if this is an existing user
                                else {
                                    mUserDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            user = dataSnapshot.getValue(User.class);
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    });
                                }
                                // update account info in navigation drawer
                                TextView accountNameTextView = (TextView) findViewById(R.id.tv_account_name);
                                TextView accountEmailTextView = (TextView) findViewById(R.id.tv_account_email);
                                if (accountNameTextView != null && accountEmailTextView != null) {
                                    accountNameTextView.setText(user.getName());
                                    accountEmailTextView.setText(user.getEmail());
                                }
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
            if (GlobalMethod.isNetworkConnected(this)) {
                showDownloadProgressDialog();
            }
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    GlobalMethod.dismissNetworkDialog();
                    createPlanInListView(dataSnapshot);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    planListAdapter.clear();
                    createPlanInListView(dataSnapshot);
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    planListAdapter.clear();
                    createPlanInListView(dataSnapshot);
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mUserDatabaseRef.child(userId).child(getString(R.string.firebase_plans)).addChildEventListener(mChildEventListener);
        }
    }

    private void onSignedOutCleanUp() {
        if (mChildEventListener != null) {
            mUserDatabaseRef.child(userId).child(getString(R.string.firebase_plans)).removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        userId = null;
        planListAdapter.clear();
    }

    private void createPlanInListView(DataSnapshot dataSnapshot) {
        // get the plan
        if (dataSnapshot.getValue(String.class).equals(getString(R.string.group))) {
            mGroupPlanDatabaseRef.child(dataSnapshot.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GroupPlan groupPlan = dataSnapshot.getValue(GroupPlan.class);
                            planListAdapter.add(groupPlan);
                            if (downloadProgressDialog != null) {
                                downloadProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
        else if (dataSnapshot.getValue(String.class).equals(getString(R.string.personal))) {
            mPersonalPlanDatabaseRef.child(dataSnapshot.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            PersonalPlan personalPlan = dataSnapshot.getValue(PersonalPlan.class);
                            planListAdapter.add(personalPlan);
                            if (downloadProgressDialog != null) {
                                downloadProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
    }

    private void showDownloadProgressDialog() {
        mUserDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(getString(R.string.firebase_plans))) {
                    downloadProgressDialog = new ProgressDialog(MainActivity.this);
                    downloadProgressDialog.setMessage(getString(R.string.downloading_message));
                    downloadProgressDialog.setCanceledOnTouchOutside(false);
                    downloadProgressDialog.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}