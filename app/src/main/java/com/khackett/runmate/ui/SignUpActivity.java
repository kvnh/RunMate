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

import com.khackett.runmate.R;
import com.khackett.runmate.RunMateApplication;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Class to sign up a new user to RunMate.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    // Member variables that correspond to items in the layout.
    private EditText mFullName;
    private EditText mUserName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordConfirm;
    private Button mSignUpButton;
    private Button mCancelSignUpButton;

    // Declare the context of the activity.
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Set each member variable for the ui components
        mFullName = (EditText) findViewById(R.id.fullNameField);
        mUserName = (EditText) findViewById(R.id.usernameField);
        mEmail = (EditText) findViewById(R.id.emailField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mPasswordConfirm = (EditText) findViewById(R.id.passwordConfirmField);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mCancelSignUpButton = (Button) findViewById(R.id.cancelSignUpButton);

        // Register buttons with the listener
        mSignUpButton.setOnClickListener(this);
        mCancelSignUpButton.setOnClickListener(this);

        // Initialise the Context to the SignUpActivity.
        mContext = SignUpActivity.this;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        // Switch statement to select which action to take depending on button pressed
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

    /**
     * Signs up a user using the credentials entered.
     * Validation checks for empty fields and a non-duplicate confirmation password.
     * Check values of text fields locally when the user taps the sign up button.
     * If OK, send the values to the backend to check validation credentials.
     * If not, display an instruction message to the user.
     */
    public void signUpUser() {

        // Get values from the edit text fields.
        // Add the toString() method, as the return type in getText() is Editable.
        // - special type of String value that needs to be converted to a regular String.
        String fullName = mFullName.getText().toString();
        String username = mUserName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String passwordConfirm = mPasswordConfirm.getText().toString();

        // Trim whitespaces from these values in case the user accidentally hits a space
        fullName = fullName.trim();
        username = username.trim();
        email = email.trim();
        password = password.trim();
        passwordConfirm = passwordConfirm.trim();

        // Ensure that none of the values are blank
        if (checkForEmptyFields(fullName, username, password, passwordConfirm, email)) {
            // Alert user to fill in all of the fields
            showErrorDialog(R.string.sign_up_error_title, R.string.sign_up_error_message);
        } else if (!isPasswordMatching(password, passwordConfirm)) {
            // Alert user to enter matching passwords
            showErrorDialog(R.string.confirm_password_error_title,
                    R.string.confirm_password_error_message);
        } else if (!isPasswordValid(password)) {
            // Alert user to enter a valid password
            showErrorDialog(R.string.password_validation_error_title,
                    R.string.password_validation_error_message);
        } else {

            // Credentials pass client side validation. Validate in backend.

            // Set up a dialog progress indicator box
            final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
            progressDialog.setTitle(R.string.sign_up_progress_dialog_title);
            progressDialog.setMessage(mContext.getString(R.string.sign_up_progress_dialog_message));
            progressDialog.show();

            // Create a ParseUser object and add each of the information fields to it
            ParseUser newUser = new ParseUser();
            newUser.put(ParseConstants.KEY_USER_FULL_NAME, fullName);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password);

            // Use ParseUser signUpInBackground() method to sign user up in a background processing thread, separate from main thread.
            // Use a callback method when it is complete - SignUpCallback()
            newUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {

                    // Dismiss progress dialog once connection with backend has been made
                    progressDialog.dismiss();

                    // done() method returns a ParseUser object.
                    // If signup is successful and no ParseException, initialise newUser variable.
                    if (e == null) {

                        // Update the Parse Installation object with the users ID to be used for push notifications.
                        RunMateApplication.updateParseInstallationObject(ParseUser.getCurrentUser());

                        // User creation successful - treat them as a logged in user and take them to the inbox.
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        // Set flags so that user cannot back through to the sign up page using the sign up button.
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {

                        // If ParseException, alert the user with dialog.
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        // Set the message from the exception.
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

    /**
     * Checks for empty fields in the sign up activity.
     *
     * @param fullName        the users full name
     * @param username        the users username
     * @param password        the users password
     * @param passwordConfirm the users confirmation password
     * @param email           the users email
     * @return true if any of the text fields are empty; false if they all contain a String value
     */
    public boolean checkForEmptyFields(String fullName, String username, String email, String password, String passwordConfirm) {

        // Declare variables for text field checks
        String fullNameCheck, usernameCheck, emailCheck, passwordCheck, passwordConfirmCheck;

        // Initialise variables
        fullNameCheck = fullName;
        usernameCheck = username;
        emailCheck = email;
        passwordCheck = password;
        passwordConfirmCheck = passwordConfirm;

        // Check if any of the arguments are empty String values
        if (fullNameCheck.isEmpty() ||
                usernameCheck.isEmpty() ||
                emailCheck.isEmpty() ||
                passwordCheck.isEmpty() ||
                passwordConfirmCheck.isEmpty()
                ) {
            // If so, return a value of true
            return true;
        }
        // If not, return a value of false
        return false;
    }

    /**
     * Checks that the confirmation password entered by the user matches the first password
     *
     * @param password        the users password
     * @param passwordConfirm the users confirmation password
     * @return true if both passwords match; false if they don't
     */
    public boolean isPasswordMatching(String password, String passwordConfirm) {
        // Declare variables for text field checks
        String passwordCheck, passwordConfirmCheck;

        // Initialise variables
        passwordCheck = password;
        passwordConfirmCheck = passwordConfirm;

        // Check if the two password fields are matching
        if (passwordCheck.equals(passwordConfirmCheck)) {
            // If so, return a value of true
            return true;
        }
        // If not, return a value of false
        return false;
    }

    /**
     * Method to check for password validation client side
     * Conditions:
     * (?=.*[A-Z]) an upper case letter must occur at least once
     * (?=.*[a-z]) a lower case letter must occur at least once
     * (?=.*[0-9]) a digit must occur at least once
     * (?=\\S+$) no whitespace allowed in the entire string
     * .{6,} at least 6 characters
     *
     * @param password
     * @return true if the password passes the regex conditions; false if not.
     */
    public boolean isPasswordValid(String password) {
        // String variable to represent the regex search pattern
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{6,}";
        if (password.matches(pattern)) {
            // If the password meets the conditions of the regex, return true.
            return true;
        }
        // Otherwise return false.
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        // Set the message title and text for the dialog
        // Chain the methods together as they are all referencing the builder object
        builder.setMessage(message).setTitle(title)
                // Button to dismiss the dialog.
                // Set the listener to null - only want to dismiss the dialog when the button is tapped.
                // ok is from android resources
                .setPositiveButton(android.R.string.ok, null);
        // Create a dialog from the builder object and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Cancels the SignUpActivity and returns the user to the login screen
     */
    public void cancelSignUp() {
        // Finish the SignUpActivity and return to the previous activity
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