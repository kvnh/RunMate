package com.khackett.runmate;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


public class ViewImageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        // get a reference to the image view add it to our layout first
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        // get the URI that was passed in by the intent
        Uri imageUri = getIntent().getData();

        // use the URI to load the image view
        // need to use picasso - allows us to load an image from the web directly into image view, as android cannot currently do this
        // imageView.setImageURI(imageUri); is the android way, does not work????
        // ... with the current context, load this image URI into the image view
        // it has many benefits - all asynchronous, captures images for reuse, etc, check out the documentation
        Picasso.with(this).load(imageUri.toString()).into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
