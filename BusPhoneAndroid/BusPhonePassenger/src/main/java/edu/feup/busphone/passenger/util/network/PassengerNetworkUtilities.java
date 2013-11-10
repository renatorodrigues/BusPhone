package edu.feup.busphone.passenger.util.network;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import edu.feup.busphone.passenger.client.Passenger;
import edu.feup.busphone.passenger.client.TicketsWallet;
import edu.feup.busphone.util.network.NetworkUtilities;

public class PassengerNetworkUtilities extends NetworkUtilities {
    private static final String TAG = "PassengerNetworkUtilities";

    public static String register(String name, String username, String password, String credit_card) {
        String uri = BASE_URL + "/register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("creditcard", credit_card));

        JSONObject response = post(uri, params);

        return response.optString("info");
    }

    public static HashMap<String, String> login(String username, String password) {
        String uri = BASE_URL + "/login";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
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

    public static void passengerInfo(String token) {
        String uri = BASE_URL + "/info" + "?token)" + token;
        JSONObject response = get(uri);
        Passenger.getInstance().setInfo(response);
    }

    public static TicketsWallet tickets(String token) {
        String uri = BASE_URL + "/tickets" + "?token=" + token;
        JSONObject response = get(uri);

        /*try {
            Log.d(TAG, response.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return response != null ? TicketsWallet.valueOf(response) : null;
    }

    public static HashMap<String, String> buy(String token, int t1, int t2, int t3, boolean confirm) {
        String uri = BASE_URL + "/buy";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("t1", Integer.toString(t1)));
        params.add(new BasicNameValuePair("t2", Integer.toString(t2)));
        params.add(new BasicNameValuePair("t3", Integer.toString(t3)));

        if (confirm) {
            params.add(new BasicNameValuePair("buy", "1"));
        }

        JSONObject response = post(uri, params);

        String info = response.optString("info");

        HashMap<String, String> sanitized_response = new HashMap<String, String>();
        sanitized_response.put("info", info);

        if (confirm) {
            return sanitized_response;
        }

        if ("OK".equals(info)) {
            Log.d(TAG, Double.toString(response.optDouble("cost")));
            sanitized_response.put("cost", Double.toString(response.optDouble("cost")));
            String extra = response.optString("extra");
            if (!"".equals(extra)) {
                sanitized_response.put("extra", extra);
            }
        }

        try {
            Log.d(TAG, response.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sanitized_response;
    }


    // TODO: REMOVE THIS
    public static String validate(String token, String ticket_id) {
        String uri = BASE_URL + "/validate";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("token", token));
        params.add(new BasicNameValuePair("id", ticket_id));

        JSONObject response = post(uri, params);

        return response.optString("info");
    }
}
