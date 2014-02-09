package edu.usf.cse.minimap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class LoadingActivity extends Activity {
    private AsyncTask<Void, Void, Void> current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        int type = getIntent().getExtras().getInt("TYPE");
        switch (type) {
            case 0:
                current = new LoadEventsTask();
                break;
            case 1:
                current = new LoadGroupsTask();
                break;
        }
        current.execute((Void) null);
    }

    public class LoadEventsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Network.getInstance().fetchEvents();
            return null;
        }

        @Override
        protected void onPostExecute(final Void x) {
            Intent i = new Intent(LoadingActivity.this, EventActivity.class);
            startActivity(i);
        }
    }

    public class LoadGroupsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Network.getInstance().fetchGroups();
            return null;
        }

        @Override
        protected void onPostExecute(final Void x) {
            Intent i = new Intent(LoadingActivity.this, GroupActivity.class);
            startActivity(i);
        }
    }
}
