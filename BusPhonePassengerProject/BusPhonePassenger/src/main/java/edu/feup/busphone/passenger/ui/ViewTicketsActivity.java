package edu.feup.busphone.passenger.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;

import edu.feup.busphone.passenger.R;
import edu.feup.busphone.passenger.client.User;

public class ViewTicketsActivity extends Activity {
    private static final String TAG = "ViewTicketsActivity";

    private ListView active_tickets_list_;

    private ArrayAdapter<String> adapter_;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.view_tickets_activity);

        active_tickets_list_ = (ListView) findViewById(R.id.active_tickets_list);
/*
        ProgressBar progress_bar = new ProgressBar(this);
        progress_bar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progress_bar.setIndeterminate(true);
        //getListView().setEmptyView(progress_bar);

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progress_bar);
*/
       adapter_ = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
       active_tickets_list_.setAdapter(adapter_);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_tickets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                User.getInstance().removeCredentials();
                startActivity(new Intent(ViewTicketsActivity.this, LoginActivity.class));
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
