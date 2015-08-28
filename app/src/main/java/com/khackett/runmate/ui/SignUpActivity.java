package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.khackett.runmate.R;
import com.khackett.runmate.RunMateApplication;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    // member variables that correspond to items in the layout
    protected EditText mUserName;
    protected EditText mPassword;
    protected EditText mEmail;
    protected Button mSignUpButton;
    protected Button mCancelSignUpButton;

//    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // set each member variable for the ui components
        mUserName = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mEmail = (EditText) findViewById(R.id.emailField);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mCancelSignUpButton = (Button) findViewById(R.id.cancelSignUpButton);

        // Register buttons with the listener
        mSignUpButton.setOnClickListener(this);
        mCancelSignUpButton.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpButton:
                signUpUser();
                break;
            case R.id.cancelSignUpButton:
                cancelSignUp();
                break;
            default:
                System.out.println("Problem with input");
        }
    }

    public void signUpUser() {
        // now we need some interactivity when the user taps the sign up button
        // we will check the values when the user does this - if they are ok, we will send them to parse
        // if not, a message will be displayed to the user so they can try again

        // this will get us the text from the edit text fields
        // need to add the toString() method as the return type in getText() is Editable, which is a special type of String value that needs to be converted to a regular String
        // ... so this is typically how we get String values from EditText
        String username = mUserName.getText().toString();
        String password = mPassword.getText().toString();
        String email = mEmail.getText().toString();

        // now we want to trim whitespaces from these values in the case that the user accidentally hits a space
        username = username.trim();
        password = password.trim();
        email = email.trim();

        // ensure that none of the values are blank
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            // then let the user know to fill in each of the fields
            // display a message to the user - use a dialog  so that some user interaction is required before it disappears
            // use Builder to build and configure the alert
            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            // set the message title and text for the button - use String resources for all of these values
            // chain the methods together as they are all referencing the builder object
            builder.setMessage(R.string.sign_up_error_message)
                    .setTitle(R.string.sign_up_error_title)
                            // button to dismiss the dialog.  Set the listener to null as we only want to dismiss the dialog when the button is tapped
                            // ok is got from android resources
                    .setPositiveButton(android.R.string.ok, null);
            // we need to create a dialog from the builder object and show it
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

//                    // set progress bar to visible
//                    // setProgressBarIndeterminateVisibility(true);
//                    mProgressBar.setVisibility(View.VISIBLE);

            // create the new user.  First create a new ParseUser object
            ParseUser newUser = new ParseUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setEmail(email);

            // the ParseUser class has special signup methods that we can use
            // this method will sign the user up in a background processing thread and call a special method when it is complete
            // set the callback parameter as SignUpCallback()
            newUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {

//                            // once contact has been made with parse, (before the error is checked) then set progress indicator visibility to false
//                            // setProgressBarIndeterminateVisibility(false);
//                            mProgressBar.setVisibility(View.INVISIBLE);

                    // this time the done() method has a parseUser object returned
                    // if signup is successful, then the user variable will be initialised and the exception will be null
                    if (e == null) {
                        // Update the Parse Installation object with the users ID
                        RunMateApplication.updateParseInstallationObject(ParseUser.getCurrentUser());

                        // then the user creation was a success - treat them as a logged in user and take them to the inbox
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        // we need to set flags so that the user cannot back through to the signup page using the signup button
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        // if there is a parse exception then...
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        // set the message from the exception
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.sign_up_error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });

        }
    }

    public void cancelSignUp() {
        // Finish the sign up activity and return to the previous activity
        finish();
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
