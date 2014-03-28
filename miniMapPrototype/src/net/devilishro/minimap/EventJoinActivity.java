package net.devilishro.minimap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class EventJoinActivity extends Activity {

	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {

			Intent i = new Intent(EventJoinActivity.this, MapActivity.class);
			startActivity(i);
		}
	};

	private ListView team1List, team2List;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_join);
		((TextView) this.findViewById(R.id.event_join_name)).setText(AppState
				.getCurrentEvent().title);
		((Button) this.findViewById(R.id.group_1_button))
				.setOnClickListener(listener);
		team1List = (ListView) findViewById(R.id.team1_list);
		team2List = (ListView) findViewById(R.id.team2_list);
	}

	public ListView getTeam1() {
		return team1List;
	}

	public ListView getTeam2() {
		return team2List;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_join, menu);
		return true;
	}
}
