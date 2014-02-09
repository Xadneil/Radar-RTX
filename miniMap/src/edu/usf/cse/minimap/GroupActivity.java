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

public class GroupActivity extends Activity {
    private OnItemClickListener clickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position,
                long id) {
            State.setCurrentGroup(position);
            State.setNumber(State.getCurrentGroup().capacity);
            Intent i = new Intent(GroupActivity.this, MapActivity.class);
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ListView l = (ListView) this.findViewById(R.id.groupListView);
        l.setAdapter(new GroupAdapter(this));
        l.setOnItemClickListener(clickListener);
    }

    private static class GroupAdapter extends ArrayAdapter<Group> {

        Context context;

        public GroupAdapter(GroupActivity act) {
            super(act, R.id.groupListView, State.getGroups());
            context = act;
        }

        private static class InfoStruct {
            public TextView name, details;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InfoStruct is;
            // Check if the convertview is null, if it is null it probably means
            // that this is the first time the view has been displayed
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.view_group, null);
                is = new InfoStruct();
                is.name = (TextView) convertView.findViewById(R.id.group_name);
                is.details = (TextView) convertView
                        .findViewById(R.id.group_details);
                convertView.setTag(is);
            } else {
                is = (InfoStruct) convertView.getTag();
            }
            Group group = State.getGroups()[position];
            if (group != null) {
                is.name.setText(group.name);
                is.details.setText("Capacity of " + group.capacity);
            }
            return convertView;
        }
    }

    public static class Group {
        public String name;
        public int capacity;

        public Group(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }
    }
}
