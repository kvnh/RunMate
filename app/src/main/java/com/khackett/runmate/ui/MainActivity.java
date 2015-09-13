package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.khackett.runmate.MapsActivityDirectionsMultiple;
import com.khackett.runmate.MapsActivityManualPolyline;
import com.khackett.runmate.MapsActivityTrackRun;
import com.khackett.runmate.R;
import com.khackett.runmate.TabFragmentContainer;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check to see if a user is logged in before the intent is started.
        // If getCurrentUser() returns a parse user, which is cached on the device by the Parse SDK,
        // then a user is logged in as a ParseUser and they will be stored in the ParseUser object.
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            // If no user returned, user is not logged in - take them to the login page.
            navigateToLogin();
        } else {
            // User is already logged in - output a log statement displaying their name.
            Log.i(TAG, currentUser.getUsername());
        }

        // Initialise the DrawerLayout and NavigationView views.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigationDrawerMenu);

        // Inflate the first fragment to be displayed when logged into the app.
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, new TabFragmentContainer()).commit();

        // Setup click events on the NavigationView items.
        // When an item is selected, replace the tab fragment container with the requested fragment.
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.navItemHome) {
                    FragmentTransaction tabFragmentContainer = mFragmentManager.beginTransaction();
                    tabFragmentContainer.replace(R.id.containerView, new TabFragmentContainer()).commit();
                }
                if (menuItem.getItemId() == R.id.navItemRunHistory) {
                    FragmentTransaction runHistoryFragment = mFragmentManager.beginTransaction();
                    runHistoryFragment.replace(R.id.containerView, new RunHistoryFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.navItemSettings) {
                    FragmentTransaction settingsFragment = mFragmentManager.beginTransaction();
                    settingsFragment.replace(R.id.containerView, new SettingsFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.navItemHelp) {
                    FragmentTransaction instructionsFragment = mFragmentManager.beginTransaction();
                    instructionsFragment.replace(R.id.containerView, new InstructionsFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.navItemMyProfile) {
                    FragmentTransaction myProfileFragment = mFragmentManager.beginTransaction();
                    myProfileFragment.replace(R.id.containerView, new MyProfileFragment()).commit();
                }
                if (menuItem.getItemId() == R.id.navItemLogOut) {
                    // User has selected log out option. Log user out and return to login screen.
                    ParseUser.logOut();
                    navigateToLogin();
                }
                return false;
            }
        });

        // Set up the Toolbar.
        setupToolbar();
    }

    public void setupToolbar() {
        // Attaching the toolbar layout to the toolbar object.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call.
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup the ActionBarDrawerToggle functionality for the Toolbar
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. Action bar will handle clicks on the Home/Up button.
        // Therefore need to specify a parent activity in AndroidManifest.xml for each activity.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_add_friends:
                // Create and start a new intent to add/remove friends.
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_plot_route:
                // Open an alert dialog to select a route creation procedure.
                AlertDialog.Builder builderPlot = new AlertDialog.Builder(this);
                // Get the list in the Strings file.
                // Create a member variable, mDialogListenerPlotRoute, for the listener below.
                builderPlot.setItems(R.array.plot_route_choices, mDialogListenerPlotRoute);
                AlertDialog dialogPlot = builderPlot.create();
                dialogPlot.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Define and set listener for the array list dialog.
    protected DialogInterface.OnClickListener mDialogListenerPlotRoute = new DialogInterface.OnClickListener() {
        @Override
        // Get the index of the value that was tapped on in the dialog list.
        public void onClick(DialogInterface dialog, int index) {
            // Create a switch statement to select the relevant route creation procedure.
            switch (index) {
                case 0: // Choose Map option 1: manual
                    Intent intent1 = new Intent(MainActivity.this, MapsActivityManualPolyline.class);
                    startActivity(intent1);
                    break;
                case 1: // Choose Map option 2: directions assisted
                    Intent intent2 = new Intent(MainActivity.this, MapsActivityDirectionsMultiple.class);
                    startActivity(intent2);
                    break;
                case 2: // Choose Map option 3: track run via GPS
                    Intent intent3 = new Intent(MainActivity.this, MapsActivityTrackRun.class);
                    startActivity(intent3);
                    break;
            }
        }
    };

    /**
     * Method to show the login screen
     */
    private void navigateToLogin() {
        // All activities are started through intents so create a new intent object.
        // Two parameters required:
        // The current system context within which the app is operating.
        // The class of the activity to start.
        Intent intent = new Intent(this, LoginActivity.class);

        // Remove MainActivity from the history so that it is skipped when user selects the back button from the newly created LoginActivity.
        // Create flags and add to the intent for LoginActivity.
        // In this case, logging in is a new task, and the old task (starting the app), should be cleared so that the user can't get back to it.
        // FLAG_ACTIVITY_CLEAR_TASK will cause any existing task that would be associated with the activity to be cleared before the activity is started.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Start the new LoginActivity activity using the startActivity() method.
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}