package edu.feup.busphone.passenger.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import edu.feup.busphone.BusPhone;
import edu.feup.busphone.client.Ticket;
import edu.feup.busphone.util.network.NetworkUtilities;

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

    public boolean hasValidated() {
        return validated_ != null;
    }

    public Ticket getTicket(int type, int index) {
        return tickets_collection_.get(type).get(index);
    }

    public void setTicketsCollection(ArrayList<ArrayList<Ticket>> tickets_collection) {
        tickets_collection_ = tickets_collection;
    }

    public void setValidatedTicket(Ticket ticket) {
        validated_ = ticket;
    }

    public static boolean isValid(Ticket t) {
        Timestamp timestamp = Timestamp.valueOf(t.getTimestamp());
        Calendar validation = Calendar.getInstance();
        validation.setTimeInMillis(timestamp.getTime());
        int ticket_duration;
        switch (t.getType()) {
            case Ticket.T1:
                ticket_duration = BusPhone.Constants.T1_DURATION;
                break;
            case Ticket.T2:
                ticket_duration = BusPhone.Constants.T2_DURATION;
                break;
            case Ticket.T3:
                ticket_duration = BusPhone.Constants.T3_DURATION;
                break;
            default:
                return false;
        }

        validation.add(Calendar.SECOND, ticket_duration);

        Calendar now = Calendar.getInstance();

        return now.getTime().getTime() < validation.getTime().getTime();
    }

    public static TicketsWallet valueOf(JSONObject tickets) {
        if (tickets.optInt("status") != NetworkUtilities.Status.OK) {
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

        JSONObject last_validated = tickets.optJSONObject("current");
        if (last_validated != null) {
            Ticket validated = Ticket.valueOf(last_validated);
            if (isValid(validated)) {
                wallet.setValidatedTicket(Ticket.valueOf(last_validated));
            }
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
