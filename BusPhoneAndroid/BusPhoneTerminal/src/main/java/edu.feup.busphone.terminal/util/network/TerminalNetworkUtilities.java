package edu.feup.busphone.terminal.util.network;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import edu.feup.busphone.util.network.NetworkUtilities;

public class TerminalNetworkUtilities extends NetworkUtilities {
    private static final String TAG = "TerminalNetworkUtilities";

    public static String register(String bus_id, String password) {
        String uri = BASE_URL + "/bus_register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("identifier", bus_id));
        params.add(new BasicNameValuePair("password", password));

        JSONObject response = post(uri, params);

        return response.optString("info");
    }

    public static HashMap<String, String> login(String bus_id, String password) {
        String uri = BASE_URL + "/bus_login";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("identifier", bus_id));
        params.add(new BasicNameValuePair("password", password));

        JSONObject response = post(uri, params);

        HashMap<String, String> sanitized_response = new HashMap<String, String>();
        sanitized_response.put("info", response.optString("info"));

        String token = response.optString("token");
        if (!"".equals(token)) {
            sanitized_response.put("token", token);
        }

        return sanitized_response;
    }

    public static String login(String token) {
        String uri = BASE_URL + "/bus_login";

        return "";

    }

    public static String validate(String token, String ticket_id) {
        String uri = BASE_URL + "/validate";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("id", ticket_id));

        JSONObject response = post(uri, params);

        return response.optString("info");
    }
}
