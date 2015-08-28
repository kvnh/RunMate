package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.RunMateApplication;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    // member variables that correspond to items in the layout
    protected EditText mUserName;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected TextView mSignUpTextView;
//    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // create a variable to hold the text view from the layout
        mSignUpTextView = (TextView) findViewById(R.id.signUpText);
        // set an on click listener for this text view
        // create a new on click listener in the brackets of setOnClickListener() - some code is generated
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to the sign up activity once the sign up text view is clicked - create a new intent
                // In the first parameter, set the context from the activity
                // - this is a separate onClickListener (called an anonymous inner type because the scope of it is within this definition)
                // Therefore, reference it by using LoginActivity.this, then add the class name as a second parameter
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                // start the activity
                startActivity(intent);
            }
        });

        // set each member variable
        mUserName = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);

        mLoginButton = (Button) findViewById(R.id.loginButton);

        // add an onClickListener for the login up button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When the user taps the sign up button, check the values for validation on the client side
                // If they are ok, send them to Parse backend.  Iif not, display a message to the user

                // Get the text from the edit text field
                // Add the toString() method, as the return type in getText() is Editable
                // (a special type of String value that needs to be converted to a regular String)
                String username = mUserName.getText().toString();
                String password = mPassword.getText().toString();

                // Trim whitespaces from these values in case the user accidentally hits a space
                username = username.trim();
                password = password.trim();

                // ensure that none of the fields are blank
                if (username.isEmpty() || password.isEmpty()) {
                    // Let the user know to fill in each of the fields by displaying a message
                    // Use a dialog  so that some user interaction is required before it disappears
                    // use Builder to build and configure the alert
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    // Set the message title and text for the button - use String resources for all of these values
                    // Chain the methods together as they are all referencing the builder object
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                                    // Button to dismiss the dialog.  Set the listener to null as we only want to dismiss the dialog
                                    // ok is from android resources
                            .setPositiveButton(android.R.string.ok, null);
                    // Create a dialog and show it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
//                    // Set progress bar to visible
//                    // setProgressBarIndeterminateVisibility(true);
//                    mProgressBar.setVisibility(View.VISIBLE);

                    // Add code to attempt logging in
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {

//                            // Once contact has been made with Parse, (before the error is checked) then set progress indicator visibility to false
//                            // setProgressBarIndeterminateVisibility(false);
//                            mProgressBar.setVisibility(View.INVISIBLE);

                            // The done() method returns a parseUser object
                            // if login is successful, then the parseUser variable will be initialised and the exception will be null
                            if (e == null) {
                                // Update the Parse Installation object with the users ID
                                RunMateApplication.updateParseInstallationObject(parseUser);

                                // we have a user - take them to the inbox
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            } else {
                                // There is a Parse exception - alert the user
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
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
