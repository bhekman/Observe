package com.infinity.observe;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bradleyhekman on 3/17/15.
 */
public class PlacesFetcher {
    static final String LOGTAG = "PlacesFetcher";

    static Boolean done = false;
    static JSONObject result = null;
    static double lat = 0.0;
    static double lon = 0.0;

    static void runFetchPlaces(double latitude, double longitude) {
        Lazy.dlog(LOGTAG, "calling get_places");
        lat = latitude;
        lon = longitude;

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        ParseCloud.callFunctionInBackground("get_places", params, new FunctionCallback<Map<String, Object>>() {
            public void done(Map<String, Object> mapObject, ParseException e) {
                done = true;
                if (e == null) {
                    result = new JSONObject(mapObject);
                    try {
                        Lazy.ilog(LOGTAG, "text: " + result.getString("text"));
                    } catch (Exception e2) {
                        Lazy.elog(LOGTAG, "ERROR");
                    }
                }
            }
        });
    }
}
