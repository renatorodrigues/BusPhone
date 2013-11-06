package edu.feup.busphone.client;

import org.json.JSONObject;

public class Ticket {
    // Ticket types
    public static final int T1 = 0;
    public static final int T2 = 1;
    public static final int T3 = 2;

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

    public String getTypeVerbose() {
        return ("T" + (type_ + 1));
    }

    public boolean isUsed() {
        return used_;
    }

    public void setUsed(boolean used) {
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
