package edu.feup.busphone.passenger.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TicketsWallet {
    // Ticket types
    public static final int T1 = 0;
    public static final int T2 = 1;
    public static final int T3 = 2;

    public static class Ticket {
        private String id_;
        private int type_;
        private boolean used_;
        private String timestamp_;

        private Ticket() {}

        public Ticket(String id, int type, boolean used, String timestamp) {
            id_ = id;
            type_ = type;
            used_ = used;
            timestamp_ = timestamp;
        }

        public String getId() {
            return id_;
        }

        public int getType() {
            return type_;
        }

        public boolean isUsed() {
            return used_;
        }

        public void setUsed_(boolean used) {
            used_ = used;
        }

        public String getTimestamp() {
            return timestamp_;
        }

        public void setTimestamp(String timestamp) {
            timestamp_ = timestamp;
        }

        public static Ticket valueOf(JSONObject ticket) {
            String id = ticket.optString("id");
            String type_str = ticket.optString("type");
            int type;
            if ("t1".equals(type_str)) {
                type = T1;
            } else if ("t2".equals(type_str)) {
                type = T2;
            } else {
                type = T3;
            }
            boolean used = ticket.optInt("used") == 0 ? false : true;
            String timestamp = ticket.optString("timestamp");

            return new Ticket(id, type, used, timestamp);
        }
    }

    private ArrayList<ArrayList<Ticket>> tickets_collection_;

    public TicketsWallet() {
        tickets_collection_ = new ArrayList<ArrayList<Ticket>>(3);
        for (int i = 0; i < 3; ++i) {
            tickets_collection_.add(new ArrayList<Ticket>());
        }
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
        return (type >= T1 && type <= T3) ? tickets_collection_.get(type).size() : -1;
    }

}
