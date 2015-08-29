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
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

/**
 * Class to log a registered user into RunMate
 */
public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    // Member variables that correspond to items in the layout.
    protected EditText mEmail;
    protected Button mResetPasswordButton;

    // Declare the context of the activity.
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Set each member variable for the ui components
        mEmail = (EditText) findViewById(R.id.emailField);
        mResetPasswordButton = (Button) findViewById(R.id.resetPasswordButton);

        // Register buttons with the listener
        mResetPasswordButton.setOnClickListener(this);

        // Initialise the Context to the LoginActivity.
        mContext = ForgotPasswordActivity.this;
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
            case R.id.resetPasswordButton:
                resetPassword();
                break;
//            case R.id.loginButton:
//                loginInUser();
//                break;
//            case R.id.signUpText:
//                signUpUser();
//                break;
            default:
                System.out.println("Problem with input");
        }
    }


    /**
     * Method to reset the users password
     */
    public void resetPassword() {
        // Get value from the edit text field.
        // Add the toString() method, as the return type in getText() is Editable.
        // - special type of String value that needs to be converted to a regular String.
        String email = mEmail.getText().toString();

        // Trim whitespaces in case the user accidentally hits a space.
        email = email.trim();

        // Ensure that the email field is not blank.
        if (email.isEmpty()) {
            // Alert user to fill in all of the fields.
            showDialog(R.string.forgot_password_error_title, R.string.forgot_password_error_message);
        } else {

            // Set up a dialog progress indicator box.
            final ProgressDialog progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
            progressDialog.setTitle(R.string.forgot_password_progress_dialog_title);
            progressDialog.setMessage(mContext.getString(R.string.forgot_password_progress_dialog_message));
            progressDialog.show();

            // Credentials pass client side validation. Validate in backend.
            ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
                public void done(ParseException e) {
                    // Dismiss progress dialog once connection with backend has been made.
                    progressDialog.dismiss();

                    if (e == null) {
                        // An email was successfully sent with reset instructions - alert the user
                        showDialog(R.string.reset_password_success_title, R.string.reset_password_success_message);
                        // Finish ForgotPasswordActivity and return to the previous activity
                        finish();
                    } else {
                        // There is an exception - alert the user.
                        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.forgot_password_error_title)
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
    public void showDialog(int title, int message) {
        // Alert user of relevant error - Use a dialog so user interaction is required.
        // Use Builder to build and configure the alert
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
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
