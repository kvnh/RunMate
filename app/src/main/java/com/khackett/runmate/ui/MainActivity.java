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
        // then a user is logged in as a ParseUser and they will be stored in the currentUser variable.
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            // If not user returned, user is not logged in - take them to the login page
            navigateToLogin();
        } else {
            // User is already logged in - add a log statement to look at the current user.
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
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.nav_item_inbox) {
                    FragmentTransaction tabFragmentContainer = mFragmentManager.beginTransaction();
                    tabFragmentContainer.replace(R.id.containerView, new TabFragmentContainer()).commit();
                }
                if (menuItem.getItemId() == R.id.nav_item_sent) {
                    FragmentTransaction settingsFragment = mFragmentManager.beginTransaction();
                    settingsFragment.replace(R.id.containerView, new SettingsFragment()).commit();
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
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_logout:
                // the user has tapped on the log out option
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_add_friends:
                // create and start and new intent
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
//            case R.id.action_camera:
//                // add code for an alert dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                // get the list the strings file
//                // code for listener will be long, so create a member variable for it and pass that in instead
//                builder.setItems(R.array.camera_choices, mDialogListenerCamera);
//                AlertDialog dialogCamera = builder.create();
//                dialogCamera.show();
//                break;
            case R.id.action_plot_route:
                // add code for an alert dialog
                AlertDialog.Builder builderPlot = new AlertDialog.Builder(this);
                // get the list the strings file
                // code for listener will be long, so create a member variable for it and pass that in instead
                builderPlot.setItems(R.array.plot_route_choices, mDialogListenerPlotRoute);
                AlertDialog dialogPlot = builderPlot.create();
                dialogPlot.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * method to show the login screen
     */
    private void navigateToLogin() {
        // all activities are started through intents so create a new intent object
        // it takes 2 parameters - first is the context (this is the current system context within
        // which the app is operating; so whenever we are working inside an activity, that is our context (activity is actually a subclass of context).
        // ... the 2nd pramter is the class of the activity that we want to start
        Intent intent = new Intent(this, LoginActivity.class);

        // remove the main activity from the history so that it is skipped when we go backwards with the back button fromm login activity
        // create a flag when we set the intent for the login activity
        // a task refers to a collection of activities in the order in which a user uses them to complete a task
        // in this case, we are saying that logging in is a new task, and the old task (starting the app), should be cleared so that we can't get back to it
        // FLAG_ACTIVITY_CLEAR_TASK will cause any existing task that would be associated with the activity to be cleared before the activity is started.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // start this new activity using the startActivity() method
        startActivity(intent);
    }

    // define and set listener for the array list dialogs in camera and plot_route
    protected DialogInterface.OnClickListener mDialogListenerPlotRoute = new DialogInterface.OnClickListener() {
        @Override
        // which tells us the index of the value that was tapped on in the dialog list
        public void onClick(DialogInterface dialog, int which) {
            // create a switch statement to look at the switch statement
            switch (which) {
                case 0: // Choose Map option 1: polylines
                    Intent intent1 = new Intent(MainActivity.this, MapsActivityManualPolyline.class);
                    startActivity(intent1);
                    break;
                case 1: // Choose Map option 2: directions(multiple)
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

//
//    public static final int TAKE_PHOTO_REQUEST = 0;
//    public static final int TAKE_VIDEO_REQUEST = 1;
//    public static final int PICK_PHOTO_REQUEST = 2;
//    public static final int PICK_VIDEO_REQUEST = 3;
//
//    public static final int MEDIA_TYPE_IMAGE = 4;
//    public static final int MEDIA_TYPE_VIDEO = 5;
//
//    // constant for the file size limit
//    public static final int FILE_SIZE_LIMIT = 1024 * 1024 * 10; // equivalent to a size of 10MB
//
//    // member variable to store the media type as a URI, that can be stored in multiple places
//    // Uri = uniform resource identifier
//    protected Uri mMediaUri;
//
//    // define and set listener for the array list dialogs in camera and plot_route
//    protected DialogInterface.OnClickListener mDialogListenerCamera =
//            new DialogInterface.OnClickListener() {
//                @Override
//                // which tells us the index of the value that was tapped on in the dialog list
//                public void onClick(DialogInterface dialog, int which) {
//                    // create a switch statement to look at the switch statement
//                    switch (which) {
//                        case 0: // Take picture
//                            // use an existing camera app on the phone by starting an intent
//                            // declare intent to capture a photo using whatever camera app is available
//                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            // after invoking the camera,
//                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
//
//                            // check that a null value is not returned
//                            if (mMediaUri == null) {
//                                // display an error
//                                Toast.makeText(MainActivity.this, R.string.error_external_storage, Toast.LENGTH_LONG).show();
//                            } else {
//                                // to add extra data to an intent, use the putExtra() method
//                                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
//                                // start an activity for a result so that the activity exits and returns a result back for us
//                                // ie, the main activity will wait for the result
//                                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
//
//                            }
//                            break;
//
//                        case 1: // Take video
//                            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
//                            // check that a null value is not returned
//                            if (mMediaUri == null) {
//                                // display an error
//                                Toast.makeText(MainActivity.this, R.string.error_external_storage, Toast.LENGTH_LONG).show();
//                            } else {
//                                // set the URI as the extra output
//                                videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
//                                // set some parameters for the video recording - length and quality
//                                videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
//                                videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//                                // now we are ready to start the activity
//                                startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
//                            }
//                            break;
//                        case 2: // Choose picture
//                            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                            // need to specify which type of action we want to get - an image in this case
//                            choosePhotoIntent.setType("image/*");
//                            startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
//                            break;
//
//                        case 3: // Choose video
//                            Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                            // need to specify which type of action we want to get - an image in this case
//                            chooseVideoIntent.setType("video/*");
//                            // impose limits so that the user doesn't select a large video file from the library
//                            Toast.makeText(MainActivity.this, R.string.video_file_size_warning, Toast.LENGTH_LONG).show();
//                            startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
//                            break;
//                    }
//                }
//
//                private Uri getOutputMediaFileUri(int mediaType) {
//                    // To be safe, you should check that the SD card / external storage is mounted
//                    // using Environment.getExternalStorageState() before doing this.
//                    // see method below...
//                    if (isExternalStorageAvailable()) {
//                        String appName = MainActivity.this.getString(R.string.app_name);
//                        // get the Uri
//
//                        // Get the external storage directory - we want to return a file object
//                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);
//
//                        // Create our subdirectory
//                        if (!mediaStorageDir.exists()) {
//                            if (!mediaStorageDir.mkdir()) {
//                                Log.e(TAG, "Failed to create directory");
//                                return null;
//                            }
//                        }
//
//                        // Create a file name that will hold the image or video
//
//                        // Create the file
//                        // need to append a timestamp to make it unique - otherwise it will overwrite the previous phot/video
//                        File mediaFile;
//                        // get the current date and time
//                        Date now = new Date();
//                        // convert the date and time into a String datetimestamp
//                        // see http://developer.android.com/guide/topics/media/camera.html#saving-media for the methods used
//                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(now);
//
//                        String path = mediaStorageDir.getPath() + File.separator;
//
//                        // create a new file using the constructor that takes a name
//                        if (mediaType == MEDIA_TYPE_IMAGE) {
//                            mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
//                        } else if (mediaType == MEDIA_TYPE_VIDEO) {
//                            mediaFile = new File(path + "VID_" + timestamp + ".mp4");
//                        } else {
//                            return null;
//                        }
//
//                        Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
//
//                        // Return the files URI
//                        return Uri.fromFile(mediaFile);
//                    } else {
//                        return null;
//                    }
//                }
//
//                /**
//                 * check if external storage is available on the users device
//                 * @return
//                 */
//                private boolean isExternalStorageAvailable() {
//                    // find out what state external storage is in
//                    String state = Environment.getExternalStorageState();
//                    // if external storage is available, return true,
//                    if (state.equals(Environment.MEDIA_MOUNTED)) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                }
//            };
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
//                if (data == null) {
//                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
//                } else {
//                    // the intent has data, so set the media uir
//                    mMediaUri = data.getData();
//                }
//                Log.i(TAG, "Media Uri: " + mMediaUri);
//                if (requestCode == PICK_VIDEO_REQUEST) {
//                    // make sure the file size is less than 10MB
//                    int fileSize = 0;
//                    InputStream inputStream = null;
//
//                    try {
//                        // open up an input stream to the file - this is used to stream information from th file byte by byte
//                        inputStream = getContentResolver().openInputStream(mMediaUri);
//                        // the getContentResolver() resolves the content URI to the actual file on the device
//                        // get total number of bytes from the file by calling the following:
//                        fileSize = inputStream.available();
//                    } catch (FileNotFoundException e) {
//                        Toast.makeText(this, getString(R.string.error_opening_file), Toast.LENGTH_LONG).show();
//                        // if this happens, we want to exit the method, so include a return at this point
//                        return;
//                    } catch (IOException e) {
//                        Toast.makeText(this, getString(R.string.error_opening_file), Toast.LENGTH_LONG).show();
//                        // if this happens, we want to exit the method, so include a return at this point
//                        return;
//                    } finally {
//                        try {
//                            inputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    // check that the file size is within the correct limit
//                    if (fileSize >= FILE_SIZE_LIMIT) {
//                        Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
//                        // if this happens, we want to exit the method, so include a return at this point
//                        return;
//                    }
//                }
//            } else {
//                // this is done by notifying the gallery that new files are available for it to include
//                // this is done by broadcasting the intent - the gallery can listen for broadcasts and take action whenever they detect a broadcast
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                mediaScanIntent.setData(mMediaUri);
//                sendBroadcast(mediaScanIntent);
//            }
//
//            // after a photo/video is captured/selected we end up in the onActivityResult()
//            // this is where we want to start our new activity for recipients
//            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
//            // set the URI of the photo/video that we want to send - attach the URI using the steData() method
//            recipientsIntent.setData(mMediaUri);
//
//            // define the file type of the file
//            String fileType;
//            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
//                // then the file is an image
//                fileType = ParseConstants.TYPE_IMAGE;
//            } else {
//                // otherwise it is a video file
//                fileType = ParseConstants.TYPE_VIDEO;
//            }
//
//            // add this to the intent
//            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
//
//            // start the intent
//            startActivity(recipientsIntent);
//        } else if (resultCode != RESULT_CANCELED) {
//            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
//        }
//    }
//
}