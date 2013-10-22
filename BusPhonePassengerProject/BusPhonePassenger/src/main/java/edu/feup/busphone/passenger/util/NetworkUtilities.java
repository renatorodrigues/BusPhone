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
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";

    private static final String HOST = "queimadus.dyndns.org";
    private static final String SCHEME = "http";
    private static final int PORT = 80;

    private static HttpClient http_client_;
    private static HttpHost http_host_;

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
                Log.d(TAG, "status code = 200");
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

    public static JSONObject get(String uri) {
        return simpleRequest(new HttpGet(uri));
    }

    private static JSONObject delete(String uri) {
        return simpleRequest(new HttpDelete(uri));
    }

    public static JSONObject post(String uri,
                                   ArrayList<NameValuePair> parameters) {
        return enclosingRequest(new HttpPost(uri), parameters);
    }

    private static JSONObject put(String uri,
                                  ArrayList<NameValuePair> parameters) {
        return enclosingRequest(new HttpPut(uri), parameters);
    }

    public static boolean isValidResponse(JSONObject response) {
        return response != null && "0".equals(response.optString("status"));
    }
}
