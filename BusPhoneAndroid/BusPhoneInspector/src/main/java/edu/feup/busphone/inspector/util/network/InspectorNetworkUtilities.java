package edu.feup.busphone.inspector.util.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.feup.busphone.client.Ticket;
import edu.feup.busphone.util.network.NetworkUtilities;

public class InspectorNetworkUtilities extends NetworkUtilities {
    private static final String TAG = "InspectorNetworkUtilities";

    public static ArrayList<Ticket> inspect(String bus_id) {
        String uri = BASE_URL + "/inspect" + "?id=" + bus_id;

        Log.d(TAG, "URI: " + uri);

        JSONObject response = get(uri);
        if (response.optInt("status") != Status.OK) {
            return null;
        }

        ArrayList<Ticket> tickets = new ArrayList<Ticket>();
        JSONArray tickets_array = response.optJSONArray("tickets");

        for (int i = 0, length = tickets_array.length(); i < length; ++i) {
            JSONObject ticket_json = tickets_array.optJSONObject(i);
            Ticket ticket = Ticket.valueOf(ticket_json);
            tickets.add(ticket);
        }

        return tickets;
    }
}
