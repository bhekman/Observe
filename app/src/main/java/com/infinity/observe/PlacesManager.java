package com.infinity.observe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bradleyhekman on 3/18/15.
 */
public class PlacesManager {
    final private String LOGTAG = "PlacesManager";
    JSONObject _places_result = null;

    public PlacesManager(JSONObject places_result) {
        _places_result = places_result;
    }

    public JSONObject getTopPlace() throws JSONException {
        Lazy.ilog(LOGTAG, "getting text");
        String text = _places_result.getString("text");
        Lazy.ilog(LOGTAG, "got text");
        JSONObject textObj = new JSONObject(text);
        Lazy.ilog(LOGTAG, "got textObj");
        JSONArray results = textObj.getJSONArray("results");
        Lazy.ilog(LOGTAG, "got results");
        JSONObject place = results.getJSONObject(0);
        Lazy.ilog(LOGTAG, "got place");
        return place;
        //String place_id = place.getString("place_id");
        //Lazy.ilog(LOGTAG, "got place_id");
    }


}
