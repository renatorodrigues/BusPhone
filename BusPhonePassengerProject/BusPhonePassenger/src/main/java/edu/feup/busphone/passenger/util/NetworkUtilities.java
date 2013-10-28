package edu.feup.busphone.passenger.util;


import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.feup.busphone.passenger.client.Ticket;
import edu.feup.busphone.passenger.client.User;

public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";

    private static final String HOST = "192.168.1.201";
    private static final String SCHEME = "http";
    private static final int PORT = 3000;
    private static final String BASE_URL = SCHEME + "://" + HOST;

    private static HttpClient http_client_;
    private static HttpHost http_host_;

    /**
     *
     * @param name
     * @param username
     * @param password
     * @param credit_card
     * @return "OK" on success, "already in use" if username already exists
     */
    public static String userRegister(String name, String username, String password, String credit_card) {
        String uri = BASE_URL + "/register";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("creditcard", credit_card));

        JSONObject response = post(uri, params);

        return response.optString("info");
    }

    public static HashMap<String, String> userLogin(String username, String password) {
        String uri = BASE_URL + "/login";

        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));

        JSONObject response = post(uri, params);
        // TODO: remove this
        try {
            Log.d(TAG, response.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, String> sanitized_response = new HashMap<String, String>();
        sanitized_response.put("info", response.optString("info"));

        String token = response.optString("token");
        if (!"".equals(token)) {
            sanitized_response.put("token", token);
        }

        return sanitized_response;
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

    public static void maybeCreateHttpClient() {
        if (http_client_ == null) {
            http_client_ = new DefaultHttpClient();
        }
    }

    public static void maybeCreateHttpHost() {
        if (http_host_ == null) {
            http_host_ = new HttpHost(HOST, PORT, SCHEME);
        }
    }

    private static JSONObject executeRequest(HttpRequest request) {
        maybeCreateHttpClient();
        maybeCreateHttpHost();

        HttpResponse response;
        try {
            response = http_client_.execute(http_host_, request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return new JSONObject(EntityUtils.toString(response.getEntity()));
            }

            Log.d(TAG, "status code = " + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            // TODO
        }

        return null;
    }

    private static JSONObject simpleRequest(HttpRequestBase request) {
        return executeRequest(request);
    }

    private static JSONObject enclosingRequest(HttpEntityEnclosingRequestBase request, ArrayList<NameValuePair> parameters) {
        HttpEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(parameters, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        request.addHeader(entity.getContentType());
        request.setEntity(entity);

        return executeRequest(request);
    }

    private static JSONObject get(String uri) {
        return simpleRequest(new HttpGet(uri));
    }

    private static JSONObject delete(String uri) {
        return simpleRequest(new HttpDelete(uri));
    }

    private static JSONObject post(String uri,
                                   ArrayList<NameValuePair> parameters) {
        return enclosingRequest(new HttpPost(uri), parameters);
    }

    private static JSONObject put(String uri,
                                  ArrayList<NameValuePair> parameters) {
        return enclosingRequest(new HttpPut(uri), parameters);
    }

    private static boolean isValidResponse(JSONObject response) {
        return response != null && "0".equals(response.optString("status"));
    }
}
