package com.infinity.observe;

import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Created by bradleyhekman on 3/18/15.
 */
public class AsyncPlacesFetcher extends AsyncTask<GPSTracker, Integer, PlacesManager> {
    static final String LOGTAG = "AsyncPlacesFetcher";

    TextView guessTextView = null;

    public AsyncPlacesFetcher(TextView guess_text_view) {
        guessTextView = guess_text_view;
    }

    protected void onPreExecute() {
    }

    protected PlacesManager doInBackground(GPSTracker... arg0) {

        GPSTracker gps = arg0[0];
        publishProgress(new Integer(10));

        double lat = gps.getLatitude();
        double lon = gps.getLongitude();
        Lazy.dlog(LOGTAG, "lat is: " + lat);
        Lazy.dlog(LOGTAG, "lon is: " + lon);
        publishProgress(new Integer(50));

        PlacesFetcher.runFetchPlaces(lat, lon);
        publishProgress(new Integer(60));
        while (!PlacesFetcher.done) {
        }
        ;
        publishProgress(new Integer(95));

        return new PlacesManager(PlacesFetcher.result);
    }

    protected void onProgressUpdate(Integer... a) {
        Lazy.dlog(LOGTAG, "You are in progress update ... " + a[0]);
    }

    protected void onPostExecute(PlacesManager result) {
        Lazy.dlog(LOGTAG, "" + result);
        try {
            Lazy.ilog(LOGTAG, "Setting guess to: " + result.getTopPlace().getString("name"));
            guessTextView.setText(result.getTopPlace().getString("name"));
        } catch (Exception e) {
            Lazy.elog(LOGTAG, e);
        }
    }
}
