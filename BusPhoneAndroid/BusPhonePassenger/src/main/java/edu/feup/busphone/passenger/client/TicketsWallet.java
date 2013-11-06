package edu.feup.busphone.passenger.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import edu.feup.busphone.client.Ticket;

public class TicketsWallet {
    private Ticket validated_ = null;
    private ArrayList<ArrayList<Ticket>> tickets_collection_;

    public TicketsWallet() {
        tickets_collection_ = new ArrayList<ArrayList<Ticket>>(3);
        for (int i = 0; i < 3; ++i) {
            tickets_collection_.add(new ArrayList<Ticket>());
        }
    }

    public Ticket getValidated() {
        return validated_;
    }

    public void setValidated(int type, int index) {
        Ticket ticket = tickets_collection_.get(type).get(index);
        tickets_collection_.get(type).remove(index);
        validated_ = ticket;
    }

    public Ticket getTicket(int type, int index) {
        return tickets_collection_.get(type).get(index);
    }

    public void setTicketsCollection(ArrayList<ArrayList<Ticket>> tickets_collection) {
        tickets_collection_ = tickets_collection;
    }

    public static TicketsWallet valueOf(JSONObject tickets) {
        if (!"OK".equals(tickets.optString("info"))) {
            return null;
        }

        TicketsWallet wallet = new TicketsWallet();
        if (tickets.optInt("total") > 0) {
            ArrayList<ArrayList<Ticket>> tickets_collection = new ArrayList<ArrayList<Ticket>>(3);
            for (int i = 0; i < 3; ++i) {
                tickets_collection.add(new ArrayList<Ticket>());
            }

            JSONArray tickets_array = tickets.optJSONArray("tickets");

            for (int i = 0, length = tickets_array.length(); i < length; ++i) {
                JSONObject ticket_json = tickets_array.optJSONObject(i);
                Ticket ticket = Ticket.valueOf(ticket_json);
                tickets_collection.get(ticket.getType()).add(ticket);
            }

            wallet.setTicketsCollection(tickets_collection);
        }

        return wallet;
    }

    public int getTotal() {
        int total = 0;
        for (ArrayList<Ticket> tickets_of_a_type : tickets_collection_) {
            total += tickets_of_a_type.size();
        }

        return total;
    }

    public int[] getCounts() {
        int[] counts = new int[3];
        for (int i = 0; i < 3; ++i) {
            counts[i] = tickets_collection_.get(i).size();
        }

        return counts;
    }

    public int getCount(int type) {
        return (type >= Ticket.T1 && type <= Ticket.T3) ? tickets_collection_.get(type).size() : -1;
    }

}
