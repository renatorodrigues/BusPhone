package edu.feup.busphone.passenger.util;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.feup.busphone.passenger.client.Ticket;
import edu.feup.busphone.passenger.client.User;

public class Api {
    private static final String TAG = "Api";
    // TODO: insert correct url
    private static final String BASE_URL = "http://queimadus.dyndns.org";

    public static boolean userRegister(String name, String username, String password, String credit_card) {
        String uri = BASE_URL + "/register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("creditcard", credit_card));
        //uri = uri + "?name=" + name + "&username=" + username + "&password=" + password + "&credit_card=" + credit_card;
        JSONObject response = NetworkUtilities.post(uri, params);
        //JSONObject response = NetworkUtilities.get(uri);
        try {
            Log.d(TAG, response.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // TODO: verify response

        return true;
    }

    public static String userLogin(String username, String password) {
        return null;
    }

    public static User userInfo(String token) {
        return null;
    }

    public static ArrayList<Ticket> tickets() {
        return null;
    }

    public static boolean busRegister(int id, String password) {
        return false;
    }

    public static String busLogin(int id, String password) {
        return null;
    }

}
