package com.infinity.observe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.concurrent.TimeUnit;

/**
 * Created by bradleyhekman on 2/24/15.
 */
public class CreateFeedbackActivity extends Activity {
    private static final String LOGTAG = "CreateFeedbackActivity";

    private ParseUser user;
    private TextView mBranding;
    private EditText mFeedbackEditText;
    private Button mFeedbackSubmitButton;
    private GPSTracker gps;
    private AsyncPlacesFetcher places_fetcher;
    private String submittedText = "";

    // TODO: Connection checking
    //ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_feedback);

        // Prep layout references
        mBranding = (TextView) findViewById(R.id.mBranding);
        mFeedbackEditText = (EditText) findViewById(R.id.mFeedbackEditText);
        mFeedbackSubmitButton = (Button) findViewById(R.id.mFeedbackSubmitButton);
        mFeedbackSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedback();
            }
        });

        // Start GPS and Places lookup
        gps = new GPSTracker(this);
        places_fetcher = new AsyncPlacesFetcher(mBranding);
        places_fetcher.execute(gps);

        // Prep Parse
        user = ParseUser.getCurrentUser();
        if (user != null) {
            Lazy.dlog(LOGTAG, "User is logged in as " + user.getUsername());
            Lazy.dlog(LOGTAG, "User obj id is: " + user.getObjectId());
        } else {
            finish();
        }
    }

    private void sendFeedback() {
        try {
            // TODO: add in progress dialog
            Lazy.ilog(LOGTAG, "sendFeedback() called");

            // Check for empty text first
            if (mFeedbackEditText.getText().toString() == "") return;

            /*// check if GPS enabled (can block)
            double latitude = 0;
            double longitude = 0;
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();   // Can block
                longitude = gps.getLongitude(); // Can block
            } else {
                gps.showSettingsAlert();
                return;
            }*/

            // get and clear text
            // text is still saved in submittedText in case submit fails.
            String feedbackText = mFeedbackEditText.getText().toString();
            Lazy.ilog(LOGTAG, "Feedback is: " + feedbackText);
            if (feedbackText.equals("")) return;
            mFeedbackEditText.setText("");
            submittedText = feedbackText;

            // get place
            PlacesManager placesManager;
            double latitude = 0;
            double longitude = 0;
            String place_id = "";
            try {
                Lazy.dlog(LOGTAG, "getting place");
                placesManager = places_fetcher.get(5, TimeUnit.SECONDS);
                latitude = PlacesFetcher.lat;
                longitude = PlacesFetcher.lon;
                place_id = placesManager.getTopPlace().getString("place_id");
            } catch (Exception e) {
                Lazy.elog(LOGTAG, e);
                Lazy.quickToast(this, "There was a problem fetching your location");
                mFeedbackEditText.setText(submittedText);
                return;
            }

            // set up for save
            ParseObject feedbackObj = new ParseObject("Feedback");
            feedbackObj.put("text", feedbackText);
            feedbackObj.put("creator", user);
            feedbackObj.put("latitude", latitude);
            feedbackObj.put("longitude", longitude);
            feedbackObj.put("placeId", place_id);

            // Do save
            feedbackObj.saveInBackground(new SaveCallback() {
                public void done(ParseException e) {

                    // Saved successfully
                    if (e == null) {
                        // Track event
                        ParseAnalytics.trackEventInBackground("Android-FeedbackSubmitted");
                        // build alert dialog
                        // to ask if user wants to submit more feedback.
                        AlertDialog.Builder adb = new AlertDialog.Builder(
                                CreateFeedbackActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                        adb.setTitle("Thanks for the feedback");
                        adb
                                .setMessage("Feel free to write some more!")
                                .setCancelable(true)
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        // create alert dialog
                        AlertDialog alertDialog = adb.create();
                        alertDialog.show();

                        // Save feedbackObj had an error
                    } else {
                        Lazy.quickToast(getApplicationContext(), "FEEDBACK FAILED TO SAVE! RAGEEEE! ");
                        mFeedbackEditText.setText(submittedText);
                        Lazy.elog(LOGTAG, e);
                        //myObjectSaveDidNotSucceed();
                    }
                }
            });
        } catch (Exception e) {
            Lazy.elog(LOGTAG, e);
            Lazy.quickToast(this, "Whoops. Something went very wrong.");
        }
    }
}
