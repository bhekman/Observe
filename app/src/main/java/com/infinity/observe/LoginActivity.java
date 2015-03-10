package com.infinity.observe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Locale;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    private static final String LOGTAG = "LoginActivity";
    ConnectionDetector cd;
    // UI references.
    private AutoCompleteTextView mUserNameEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onRestart() {
        Lazy.ilog(LOGTAG, "login restarted");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Lazy.ilog(LOGTAG, "login started");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Lazy.ilog(LOGTAG, "login resumed");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Lazy.ilog(LOGTAG, "login paused");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Lazy.ilog(LOGTAG, "login stopped");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Lazy.ilog(LOGTAG, "login destroyed");
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Lazy.dlog(LOGTAG, "login oncreate");
        cd = new ConnectionDetector(getApplicationContext());

        // Set up the login form.
        mUserNameEditText = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordEditText = (EditText) findViewById(R.id.password);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Lazy.dlog(LOGTAG, "login clicked");
                attemptLogin();
            }
        });
    }

    /*private void populateAutoComplete() {
        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }*/

    public void attemptLogin() {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "user wants to log in");

        Boolean isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            checkLoginCredentials();
        } else {
            Toast.makeText(getApplicationContext(),
                    "No internet connection found",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void checkLoginCredentials() {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "checking login credentials");

        clearErrors();

        // Store values at the time of the login attempt.
        String email = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email) || !isEmailValid(email)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_email));
            focusView = mUserNameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            if (BuildConfig.DEBUG) Log.d(LOGTAG, "doing login");
            login(email.toLowerCase(Locale.getDefault()), password);
        }
    }

    private void login(final String lowerCase, final String password) {
        ParseUser.logInInBackground(lowerCase, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    if (BuildConfig.DEBUG) Log.d(LOGTAG, "successful login!");
                    ParseAnalytics.trackEventInBackground("Android-ReturnUserLogin");
                    loginSuccessful();
                } else {
                    if (BuildConfig.DEBUG) Log.d(LOGTAG, "unsuccessful login: " + e.toString());
                    if (BuildConfig.DEBUG) Log.d(LOGTAG, "unsuccessful login: " + e.getCode());
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND)
                        signUp(lowerCase, lowerCase, password);
                    else
                        loginUnSuccessful();
                }
            }
        });
    }

    private void signUp(final String mUsername, String mEmail, String mPassword) {

        ParseUser user = new ParseUser();
        user.setUsername(mUsername);
        user.setPassword(mPassword);
        user.setEmail(mEmail);

        ParseAnalytics.trackEventInBackground("Android-SignupAttempted");
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Lazy.quickToast(getApplicationContext(), "Account Created Successfully");
                    loginSuccessful();
                } else {
                    // Sign up failed.
                    // Look at the ParseException to figure out what went wrong
                    if (BuildConfig.DEBUG) Log.d(LOGTAG, "unsuccessful signup: " + e.toString());
                    Lazy.quickToast(getApplicationContext(), "Account already taken.");
                }
            }
        });
    }

    protected void loginSuccessful() {
        Intent in = new Intent(this, CreateFeedbackActivity.class);
        startActivity(in);
    }

    protected void loginUnSuccessful() {
        Toast.makeText(getApplicationContext(), "FAILURE", Toast.LENGTH_SHORT).show();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void clearErrors() {
        mUserNameEditText.setError(null);
        mPasswordEditText.setError(null);
    }
}



