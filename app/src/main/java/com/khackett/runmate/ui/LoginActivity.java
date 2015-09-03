package com.khackett.runmate.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

/**
 * Class to log a registered user into RunMate
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Member variables that correspond to items in the layout.
    protected EditText mUserName;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected TextView mSignUpTextView;
    protected TextView mForgotPasswordTextView;

    // Declare the context of the activity.
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set each member variable for the ui components
        mUserName = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mLoginButton = (Button) findViewById(R.id.loginButton);
        mSignUpTextView = (TextView) findViewById(R.id.signUpText);
        mForgotPasswordTextView = (TextView) findViewById(R.id.forgotPasswordText);

        // Register buttons with the listener
        mLoginButton.setOnClickListener(this);
        mSignUpTextView.setOnClickListener(this);
        mForgotPasswordTextView.setOnClickListener(this);

        // Initialise the Context to the LoginActivity.
        mContext = LoginActivity.this;
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
            case R.id.loginButton:
                loginInUser();
                break;
            case R.id.signUpText:
                signUpUser();
                break;
            case R.id.forgotPasswordText:
                forgotPassword();
                break;
            default:
                System.out.println("Problem with input");
        }
    }

    /**
     * Method to log a user into RunMate.
     */
    public void loginInUser() {
        // When the user taps the log in button, check the values for validation on the client side
        // If they are ok, send them to Parse backend.  If not, display a message to the user

        // Get values from the edit text fields.
        // Add the toString() method, as the return type in getText() is Editable.
        // - special type of String value that needs to be converted to a regular String.
        String username = mUserName.getText().toString();
        String password = mPassword.getText().toString();

        // Trim whitespaces from these values in case the user accidentally hits a space.
        username = username.trim();
        password = password.trim();

        // Ensure that none of the fields are blank.

        if (checkForEmptyFields(username, password)) {
            // Alert user to fill in all of the fields.
            showErrorDialog(R.string.login_error_title, R.string.login_error_message);
        } else {

            // Credentials pass client side validation. Validate in backend.

            // Set up a dialog progress indicator box.
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle(R.string.log_in_progress_dialog_title);
            progressDialog.setMessage(mContext.getString(R.string.log_in_progress_dialog_message));
            progressDialog.show();

            // Attempt to log user in using background thread
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {

                    // Dismiss progress dialog once connection with backend has been made.
                    progressDialog.dismiss();

                    // done() method returns a ParseUser object.
                    if (e == null) {
                        // If login is successful and no ParseException, log user into RunMate

                        // Update the Parse Installation object with the users ID to be used for push notifications.
                        RunMateApplication.updateParseInstallationObject(parseUser);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {
                        // There is an exception - alert the user.
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

    /**
     * Switches to the SignUpActivity
     */
    public void signUpUser() {
        // Create a new intent for the SignUpActivity and start.
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    /**
     * Switches to the ForgotPasswordActivity
     */
    public void forgotPassword() {
        // Create a new intent for the ForgotPasswordActivity and start.
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }


    /**
     * Checks for empty fields in the log in activity.
     *
     * @param username the users username
     * @param password the users password
     * @return true if any of the text fields are empty; false if they all contain a String value
     */
    public boolean checkForEmptyFields(String username, String password) {

        // Declare variables for text field checks
        String usernameCheck, passwordCheck;

        // Initialise variables
        usernameCheck = username;
        passwordCheck = password;

        // Check if any of the arguments are empty String values
        if (usernameCheck.isEmpty() || passwordCheck.isEmpty()) {
            // If so, return a value of true
            return true;
        }
        // If not, return a value of false
        return false;
    }

    /**
     * Error dialog to display to the user if the credentials they entered are not valid
     *
     * @param title   the title of the dialog
     * @param message the message in the dialog
     */
    public void showErrorDialog(int title, int message) {
        // Alert user of relevant error - Use a dialog so user interaction is required.
        // Use Builder to build and configure the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        // Set the message title and text for the dialog
        // Chain the methods together as they are all referencing the builder object
        builder.setMessage(message).setTitle(title)
                // Button to dismiss the dialog.
                // Set the listener to null - only want to dismiss the dialog when the button is tapped.
                // (ok is from android resources)
                .setPositiveButton(android.R.string.ok, null);
        // Create a dialog from the builder object and show it
        AlertDialog dialog = builder.create();
        dialog.show();
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
