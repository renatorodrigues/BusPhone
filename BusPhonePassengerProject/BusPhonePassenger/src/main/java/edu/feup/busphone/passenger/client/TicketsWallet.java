package edu.feup.busphone.passenger.client;

import org.json.JSONObject;

import java.util.ArrayList;

public class TicketsWallet {
    // Ticket types
    public static final int T1 = 0;
    public static final int T2 = 1;
    public static final int T3 = 2;

    public static class Ticket {
        private int id_;
        private int type_;

        public Ticket() {

        }

        public static Ticket valueOf(JSONObject ticket) {
            return new Ticket();
        }
    }

    private ArrayList<ArrayList<Ticket>> tickets_collection_;

    public TicketsWallet() {
        tickets_collection_ = new ArrayList<ArrayList<Ticket>>(3);
        for (int i = 0; i < 3; ++i) {
            tickets_collection_.add(new ArrayList<Ticket>());
        }
    }

    public static TicketsWallet valueOf(JSONObject tickets) {
        if (!"OK".equals(tickets.optString("info"))) {
            return null;
        }

        TicketsWallet wallet = new TicketsWallet();
        if (tickets.optInt("total") > 0) {

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
        return (type >= T1 && type <= T3) ? tickets_collection_.get(type).size() : -1;
    }

}
