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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.khackett.runmate.R;
import com.khackett.runmate.utils.FileHelper;
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
 * Created by KHackett on 01/09/15.
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = MyProfileFragment.class.getSimpleName();

    protected Button mTakePicture;
    protected Button mChoosePicture;

    protected ImageView mProfilePicture;

    // member variable to store the media type as a URI, that can be stored in multiple places
    // Uri = uniform resource identifier
    private Uri mMediaUri;

    private Bitmap bitmapPicture;

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
            Log.d(TAG, "onSaveInstanceState() for mMediaUri: " + mMediaUri);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mMediaUri = Uri.parse(savedInstanceState.getString("media_uri"));
            Log.d(TAG, "onCreate() for mMediaUri: " + mMediaUri);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        mProfilePicture = (ImageView) rootView.findViewById(R.id.profilePicture);
        updateProfilePic();

        mTakePicture = (Button) rootView.findViewById(R.id.takePictureButton);
        mChoosePicture = (Button) rootView.findViewById(R.id.choosePictureButton);

        mTakePicture.setOnClickListener(this);
        mChoosePicture.setOnClickListener(this);

        return rootView;

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // Switch statement to select which action to take depending on button/text pressed
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

    public static final int TAKE_PHOTO_REQUEST = 1889;
    public static final int PICK_PHOTO_REQUEST = 1888;


    public void takeCameraPicture() {
        // Take picture
        // use an existing camera app on the phone by starting an intent
        // declare intent to capture a photo using whatever camera app is available
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // after invoking the camera,
        mMediaUri = getOutputMediaFileUri();

        // check that a null value is not returned
        if (mMediaUri == null) {
            // display an error
            Toast.makeText(getActivity(), R.string.error_external_storage, Toast.LENGTH_LONG).show();
        } else {

            // start an activity for a result so that the activity exits and returns a result back for us
            // ie, the main activity will wait for the result
            startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
        }
    }

    public void chooseGalleryPicture() {
        // Choose picture
        Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // need to specify which type of action we want to get - an image in this case
        choosePhotoIntent.setType("image/*");
        startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
    }

    private Uri getOutputMediaFileUri() {
        // To be safe, you should check that the SD card / external storage is mounted
        // using Environment.getExternalStorageState() before doing this.
        // see method below...
        if (isExternalStorageAvailable()) {
            String appName = MyProfileFragment.this.getString(R.string.app_name);
            // get the Uri

            // Get the external storage directory - we want to return a file object
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

            // Create our subdirectory
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdir()) {
                    Log.e(TAG, "Failed to create directory");
                    return null;
                }
            }

            // Create a file to hold the image
            File mediaFile;
            // get the current date and time
            Date now = new Date();
            // convert the date and time into a String datetimestamp
            // see http://developer.android.com/guide/topics/media/camera.html#saving-media for the methods used
            // need to append a timestamp to make it unique - otherwise it will overwrite the previous photo
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(now);

            String path = mediaStorageDir.getPath() + File.separator;

            // create a new file using the constructor that takes a name

            mediaFile = new File(path + "IMG_" + timestamp + ".jpg");

            Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

            // Return the files URI
            Log.d(TAG, "Returning the files URI");
            return Uri.fromFile(mediaFile);
        } else {
            return null;
        }
    }

    /**
     * check if external storage is available on the users device
     *
     * @return
     */
    private boolean isExternalStorageAvailable() {
        // find out what state external storage is in
        String state = Environment.getExternalStorageState();
        // if external storage is available, return true,
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Log.d(TAG, "Data is null");
                    Toast.makeText(getActivity(), getString(R.string.general_error), Toast.LENGTH_LONG).show();
                } else {

                    // the intent has data, so set the media uri
                    mMediaUri = data.getData();
                    Log.d(TAG, "Media Uri: " + mMediaUri);
                }

                saveImageToParse(PICK_PHOTO_REQUEST);
                updateProfilePic();

            } else if (requestCode == TAKE_PHOTO_REQUEST) {

//                mMediaUri = (Uri)data.getData();
//                Log.d(TAG, "mMediaUri after data.getData(): " + mMediaUri);

                bitmapPicture = (Bitmap) data.getExtras().get("data");
                Log.d(TAG, "bitmapPicture picture: " + bitmapPicture);

                saveImageToParse(TAKE_PHOTO_REQUEST);
                updateProfilePic();
            }
        } else if (resultCode != Activity.RESULT_CANCELED) {
            Log.d(TAG, "Problem getting the picture from gallery");
            Toast.makeText(getActivity(), R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    public void saveImageToParse(int requestCode) {
        if (requestCode == PICK_PHOTO_REQUEST) {
            byte[] fileBytes = FileHelper.getByteArrayFromFile(getActivity(), mMediaUri);

            fileBytes = FileHelper.reduceImageForUpload(fileBytes);

            String fileName = FileHelper.getFileName(getActivity(), mMediaUri, "file");
            ParseFile file = new ParseFile(fileName, fileBytes);
            ParseUser.getCurrentUser().put("profilePic", file);

        } else {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmapPicture.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] fileBytes = byteStream.toByteArray();

            String fileName = FileHelper.getFileName(getActivity(), mMediaUri, "file");
            ParseFile file = new ParseFile(fileName, fileBytes);
            ParseUser.getCurrentUser().put("profilePic", file);
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

    public void updateProfilePic() {
        ParseFile image = (ParseFile) ParseUser.getCurrentUser().getParseFile("profilePic");
        // Email will be an empty String if the user didn't supply an email address
        if (image == null) {
            // If image file is empty, set the default avatar
            Log.d(TAG, "No profile picture for: " + ParseUser.getCurrentUser().getUsername());
//            holder.userImageView.setImageResource(R.mipmap.avatar_empty);
        } else {

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