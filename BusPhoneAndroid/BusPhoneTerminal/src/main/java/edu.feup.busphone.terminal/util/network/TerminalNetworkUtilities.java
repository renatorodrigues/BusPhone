package edu.feup.busphone.terminal.util.network;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

import edu.feup.busphone.util.network.NetworkUtilities;

public class TerminalNetworkUtilities extends NetworkUtilities {
    private static final String TAG = "TerminalNetworkUtilities";

    public static String register(String bus_id, String password) {
        String uri = BASE_URL + "/bus_register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", bus_id));
        params.add(new BasicNameValuePair("password", password));

        return "";
    }

    public static String login(String bus_id, String password) {
        String uri = BASE_URL + "/bus_login";

        return "";
    }

    public static String login(String token) {
        String uri = BASE_URL + "/bus_login";

        return "";

    }

    public static String validate(String token, String ticket_id) {
        String uri = BASE_URL + "/validate";

        return "";
    }
}
