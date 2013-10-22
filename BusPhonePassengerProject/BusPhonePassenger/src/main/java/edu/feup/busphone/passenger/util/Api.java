package edu.feup.busphone.passenger.util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;

public class Api {
    // TODO: insert correct url
    private static final String BASE_URL = "http://";

    public static boolean register(String name, String username, String password, String credit_card) {
        String uri = BASE_URL + "/register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("credit_card", credit_card));

        JSONObject response = NetworkUtilities.post(uri, params);

        // TODO: verify response

        return true;
    }

}
