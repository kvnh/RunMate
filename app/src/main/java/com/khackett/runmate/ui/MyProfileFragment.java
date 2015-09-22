package com.khackett.runmate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.FileHelper;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment to display and edit the users profile picture
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener {

    // TAG to represent the FriendsFragment class
    public static final String TAG = MyProfileFragment.class.getSimpleName();

    // Member variables for UI components
    private Button mTakePicture;
    private Button mChoosePicture;
    private ImageView mProfilePicture;

    // Member variable to store the media type as a URI (uniform resource identifier)
    private Uri mMediaUri;
    // Member variable to hold the users profile picture
    private Bitmap bitmapPicture;

    // Request codes for taking a camera picture and choosing a gallery picture
    private static final int TAKE_PHOTO_REQUEST = 1889;
    private static final int PICK_PHOTO_REQUEST = 1888;

    /**
     * The image type of the chosen picture.
     */
    private static final String IMAGE_TYPE = ".jpg";

    /**
     * The date and time format of the images' timestamp.
     */
    private static final String SIMPLE_DATE_FORMAT = "yyyyMMdd_HHmmss";


    /**
     * Default constructor
     */
    public MyProfileFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        if (mMediaUri != null) {
            savedInstanceState.putString("media_uri", mMediaUri.toString());
        }
        // Call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mMediaUri = Uri.parse(savedInstanceState.getString("media_uri"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        // Return the view of the fragment
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create the profile picture item and update it
        mProfilePicture = (ImageView) view.findViewById(R.id.profilePicture);
        updateProfilePic();

        // Set up member variables for each UI component
        mTakePicture = (Button) view.findViewById(R.id.takePictureButton);
        mChoosePicture = (Button) view.findViewById(R.id.choosePictureButton);

        // Register components with the listener
        mTakePicture.setOnClickListener(this);
        mChoosePicture.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // Switch statement to select which action to take depending on the component pressed
        switch (v.getId()) {
            case R.id.takePictureButton:
                takeCameraPicture();
                break;
            case R.id.choosePictureButton:
                chooseGalleryPicture();
                break;
            default:
                System.out.println("Problem with input");
        }
    }

    /**
     * Method to take a picture with the devices camera
     */
    public void takeCameraPicture() {
        // Use an existing camera app on the phone using ACTION_IMAGE_CAPTURE
        // declare intent to capture a photo using whatever camera app is available
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Assign the output media uri
        mMediaUri = getOutputMediaFileUri();

        // Check that a null value is not returned
        if (mMediaUri == null) {
            // If it is, display an error toast
            Toast.makeText(getActivity(), R.string.error_external_storage, Toast.LENGTH_LONG).show();
        } else {
            // Otherwise, call startActivityForResult() so that the activity returns a result back
            startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
        }
    }

    /**
     * Method to choose a picture from the devices gallery
     */
    public void chooseGalleryPicture() {
        // Create a new intent
        Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // Specify the type of data (image) the new intent wants
        choosePhotoIntent.setType("image/*");
        // Start intent
        startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
    }

    /**
     * Method to create a File or Uri location for a media file.
     * Can be used when invoking a device's camera with an Intent.
     * See http://developer.android.com/guide/topics/media/camera.html#saving-media for the method used.
     *
     * @return a uri value
     */
    private Uri getOutputMediaFileUri() {
        // Check if SD card / external storage is mounted
        if (isExternalStorageAvailable()) {
            // If so, create a String to contain the app name
            String appName = MyProfileFragment.this.getString(R.string.app_name);

            // Get the external storage directory and return a file object
            File externalMediaDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

            // Create a subdirectory
            if (!externalMediaDirectory.exists()) {
                if (!externalMediaDirectory.mkdir()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            // Create a file to hold the image
            File mediaFile;
            // Get the current date and time
            Date currentDate = new Date();
            // Convert the date and time into a String.
            // Append a timestamp to make image unique so that it doesn't overwrite previous photos.
            String dateTimeStamp = new SimpleDateFormat(SIMPLE_DATE_FORMAT, Locale.ENGLISH).format(currentDate);

            // Create a String to represent the external media directory
            String path = externalMediaDirectory.getPath() + File.separator;

            // Create a new file using the constructor that takes a name of the directory and dateTimeStamp
            mediaFile = new File(path + "IMG_" + dateTimeStamp + IMAGE_TYPE);

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
            // Return the files URI
            Log.d(TAG, "Returning the files URI");
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    /**
     * Check if external storage is available on the users device
     *
     * @return a boolean value
     */
    private boolean isExternalStorageAvailable() {
        // Create a String to store state of the external storage
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // If external storage is available, return true
            return true;
        } else {
            // Otherwise return false
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // On the result of either the takeCameraPicture() or chooseGalleryPicture()
        if (resultCode == Activity.RESULT_OK) {
            // Image has been chosen from the devices gallery
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "Data is null");
                    Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_LONG).show();
                } else {
                    // The intent has data - set the media uri
                    mMediaUri = data.getData();
                    Log.d(TAG, "Media Uri: " + mMediaUri);
                }

                // Save the image to Parse
                saveImageToParse(PICK_PHOTO_REQUEST);
                // Update profile picture in the Fragment
                updateProfilePic();

            } else if (requestCode == TAKE_PHOTO_REQUEST) {
                // Image has been taken with the devices camera
                // Assign the returned image data to a Bitmap object
                bitmapPicture = (Bitmap) data.getExtras().get("data");
                Log.d(TAG, "bitmapPicture picture: " + bitmapPicture);

                // Save the image to Parse
                saveImageToParse(TAKE_PHOTO_REQUEST);
                // Update profile picture in the Fragment
                updateProfilePic();
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
            // Otherwise there was a problem - alert the user
            Log.d(TAG, "Problem getting the picture from gallery");
            Toast.makeText(getActivity(), R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method to save an image to Parse.
     *
     * @param requestCode
     */
    public void saveImageToParse(int requestCode) {
        if (requestCode == PICK_PHOTO_REQUEST) {
            // If user has chosen an image from the gallery:
            // Convert the image to a byte array.
            byte[] fileBytes = FileHelper.getByteArrayFromFile(getActivity(), mMediaUri);
            fileBytes = FileHelper.reduceImageForUpload(fileBytes);

            // Add the image to Parse
            String fileName = FileHelper.getFileName(getActivity(), mMediaUri, "file");
            ParseFile file = new ParseFile(fileName, fileBytes);
            ParseUser.getCurrentUser().put(ParseConstants.KEY_PROFILE_PICTURE, file);

        } else if (requestCode == TAKE_PHOTO_REQUEST) {
            // If user has taken a picture with devices camera:
            // Convert the image to a byte array.
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] fileBytes = byteStream.toByteArray();

            // Add the image to Parse
            String fileName = FileHelper.getFileName(getActivity(), mMediaUri, "file");
            ParseFile file = new ParseFile(fileName, fileBytes);
            ParseUser.getCurrentUser().put(ParseConstants.KEY_PROFILE_PICTURE, file);
        }

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Image saved successfully");
                } else {
                    Log.d(TAG, "Image not saved");
                }
            }
        });
    }

    /**
     * Method to update a users profile picture in MyProfileFragment
     */
    public void updateProfilePic() {
        // Get the current users image file from Parse.
        ParseFile image = (ParseFile) ParseUser.getCurrentUser().getParseFile("profilePic");
        if (image == null) {
            // If image file is empty, alert user.
            Log.d(TAG, "No profile picture for: " + ParseUser.getCurrentUser().getUsername());
        } else {
            // Otherwise inject it using Picasso
            Picasso.with(getActivity())
                    // Load the URL
                    .load(image.getUrl())
                            // if a 404 code is returned, use the placeholder image
                    .placeholder(R.mipmap.avatar_empty)
                            // Load into user image view
                    .into(mProfilePicture);
        }
    }

}