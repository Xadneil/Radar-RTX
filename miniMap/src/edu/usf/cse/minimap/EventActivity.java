package edu.usf.cse.minimap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class EventActivity extends Activity {
    private OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                long id) {
            State.setCurrentEvent(position);

            Intent i = new Intent(EventActivity.this, LoadingActivity.class);
            i.putExtra("TYPE", 1);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        ListView l = (ListView) this.findViewById(R.id.eventListView);
        l.setAdapter(new EventAdapter(this));
        l.setOnItemClickListener(clickListener);
    }

    private static class EventAdapter extends ArrayAdapter<Event> {

        Context context;

        public EventAdapter(EventActivity act) {
            super(act, R.id.eventListView, State.getEvents());
            context = act;
        }

        private static class InfoStruct {
            public TextView title, provider;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InfoStruct is;
            // Check if the convertview is null, if it is null it probably means
            // that this is the first time the view has been displayed
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.view_event, null);
                is = new InfoStruct();
                is.title = (TextView) convertView.findViewById(R.id.event_name);
                is.provider = (TextView) convertView
                        .findViewById(R.id.event_provider);
                convertView.setTag(is);
            } else {
                is = (InfoStruct) convertView.getTag();
            }
            Event event = State.getEvents()[position];
            if (event != null) {
                is.title.setText(event.title);
                is.provider.setText(event.provider);
            }
            return convertView;
        }
    }

    public static class Event {
        public String title, provider;
        public LatLng position;
        public float zoom;

        public Event(String title, String provider, LatLng position, float zoom) {
            this.title = title;
            this.provider = provider;
            this.position = position;
            this.zoom = zoom;
        }
    }
}
