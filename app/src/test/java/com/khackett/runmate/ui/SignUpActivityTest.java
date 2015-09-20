package com.khackett.runmate.ui;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;

import com.khackett.runmate.R;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests SignUpActivity in isolation from the system.
 * Created by KHackett on 19/09/15.
 */
public class SignUpActivityTest extends ActivityInstrumentationTestCase2<SignUpActivity> {

    // Declare test data for Strings
    String emptyString, fullName, username, email, password, passwordConfirm,
            passwordConfirmInvalid, passwordInvalid;

    // Declare a SignUpActivity object
    SignUpActivity mSignUpActivity;

    public SignUpActivityTest() {
        super(SignUpActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // assign value to String test data
        emptyString = "";
        fullName = "kevin";
        username = "kevinH";
        email = "kevin@test.com";
        password = "Password1";
        passwordConfirm = "Password1";
        passwordConfirmInvalid = "password12";
        passwordInvalid = "password1";
    }

    /**
     * Verifies that app and test code are configured correctly
     */
    @Test
    public void testActivityExists() {
        SignUpActivity signUpActivity = getActivity();
        assertNotNull(signUpActivity);
    }


    /**
     * Checks for empty fields in the registration process to return a false value
     */
    @Test
    public void testCheckForEmptyFieldsAllInvalid() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(false, mSignUpActivity.checkForEmptyFields(
                emptyString,
                emptyString,
                emptyString,
                emptyString,
                emptyString));
    }

    /**
     * Checks that all fields in the registration process are filled and returns a value of true
     */
    @Test
    public void testCheckForEmptyFieldsValid() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(true, mSignUpActivity.checkForEmptyFields(
                fullName,
                username,
                email,
                password,
                passwordConfirm));
    }

    /**
     * Checks the confirmation password matches the first password and returns a true value.
     */
    @Test
    public void testIsPasswordMatchingValid() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(true, mSignUpActivity.isPasswordMatching(password, passwordConfirm));
    }

    /**
     * Checks that when the confirmation password doesn't match the first password,
     * false is returned.
     */
    @Test
    public void testIsPasswordMatchingInvalid() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(false, mSignUpActivity.isPasswordMatching(password, passwordConfirmInvalid));
    }

    /**
     * Checks the that a password containing at least 1 uppercase, 1 lowercase, 1 number
     * and is of length 6 characters returns a value of true.
     */
    @Test
    public void testIsPasswordValidPass() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(true, mSignUpActivity.isPasswordValid(password));
    }

    /**
     * Checks the that a password which does not contain at least 1 uppercase, 1 lowercase,
     * 1 number or is of length 6 characters returns a value of false.
     */
    @Test
    public void testIsPasswordValidFail() throws Exception {
        mSignUpActivity = new SignUpActivity();
        assertEquals(false, mSignUpActivity.isPasswordValid(passwordInvalid));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}