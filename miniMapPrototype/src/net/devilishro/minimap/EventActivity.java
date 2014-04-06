package net.devilishro.minimap;

import java.util.HashMap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.Packet;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Event Activity. Contains the event list and the main menu.
 * 
 * @author Daniel
 */
public class EventActivity extends Activity {
	private OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			AppState.setCurrentEvent(position); // update State

			if (AppState.networkBypass) {
				HashMap<Network.Activities, Activity> temp = new HashMap<Network.Activities, Activity>(
						1);
				temp.put(Network.Activities.EVENT_LIST, EventActivity.this);
				PacketHandlers.eventChoose.handlePacket(new Packet(0), null,
						temp);
			} else {
				// send packet to server
				AppState.getEventServer()
						.send(PacketCreator.selectEvent(AppState
								.getCurrentEvent().id));
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			if (m.what == 0) {
				ListView l = (ListView) EventActivity.this
						.findViewById(R.id.eventListView);
				l.setAdapter(new EventAdapter(EventActivity.this));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);
		ListView l = (ListView) this.findViewById(R.id.eventListView);
		Log.d("EventActivity", "State.getEvents(): "
				+ (AppState.getEvents() == null ? "yes" : "no"));
		if (AppState.getEvents() == null) {
			finish();
			return;
		}
		l.setAdapter(new EventAdapter(this));
		l.setOnItemClickListener(clickListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AppState.getEventServer().registerContext(this,
				Network.Activities.EVENT_LIST);
	}

	@Override
	protected void onPause() {
		super.onPause();
		AppState.getEventServer().unregisterContext(
				Network.Activities.EVENT_LIST);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If back button pushed, send logout
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			AppState.setLoginOK(false);
			AppState.getEventServer().send(PacketCreator.logout());
			AppState.setUsername(null);
			Intent i = new Intent(this, Minimap.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Replays");
		menu.add(0, 1, 1, "FriendForcer");
		menu.add(0, 2, 2, "Logout");
		// Admin only options
		if (AppState.isAdmin()) {
			menu.add(0, 3, 3, "Add Event");
			menu.add(0, 4, 4, "Notifications");
			menu.add(0, 5, 5, "Player List");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:// replay
		{
			Intent i = new Intent(this, Replay.class);
			startActivity(i);
			break;
		}
		case 1:// friendforcer
		{
			AppState.getEventServer().send(PacketCreator.friend());
			break;
		}
		case 2:// logout
		{
			AppState.setLoginOK(false);
			AppState.getEventServer().send(PacketCreator.logout());
			AppState.setUsername(null);
			Intent i = new Intent(this, Minimap.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
		}
		case 3:// event add
		{
			Intent i = new Intent(this, EventAdd.class);
			startActivity(i);
			break;
		}
		case 4:// event notify
		{
			Intent i = new Intent(this, Note_acti.class);
			startActivity(i);
			break;
		}
		case 5:// player list
		{
			Intent i = new Intent(this, Player_state.class);
			startActivity(i);
			break;
		}
		}
		return true;
	}

	public void onRefreshClicked(View view) {
		AppState.getEventServer().send(PacketCreator.requestEventList());
	}

	/**
	 * Causes the event list to refresh and display current data
	 */
	public void refresh() {
		handler.obtainMessage(0).sendToTarget();
	}

	/**
	 * Proceeds from event screen to join screen. Used by the event handler for
	 * event choosing.
	 */
	public void startJoinActivity() {
		Intent i = new Intent(EventActivity.this, EventJoinActivity.class);
		startActivity(i);
	}

	public void eventFull() {
		Toast.makeText(this, "This event is full.", Toast.LENGTH_LONG).show();
	}

	/**
	 * ArrayAdapter for showing custom layouts for each event
	 * 
	 * @author Daniel
	 */
	private static class EventAdapter extends ArrayAdapter<Event> {

		Context context;

		public EventAdapter(EventActivity act) {
			super(act, R.id.eventListView, AppState.getEvents());
			context = act;
		}

		private static class InfoStruct {
			public TextView title, message;
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
				is.message = (TextView) convertView
						.findViewById(R.id.event_message);
				convertView.setTag(is);
			} else {
				is = (InfoStruct) convertView.getTag();
			}
			Event event = AppState.getEvents()[position];
			if (event != null) {
				is.title.setText(event.title);
				is.message.setText(event.message);
			}
			return convertView;
		}
	}

	/**
	 * Event information
	 * 
	 * @author Daniel
	 */
	public static class Event {
		public String title, message;
		public LatLng location;
		public float zoom;
		public int id;
		public String team1, team2;
		public float bearing;

		/**
		 * Class Constructor
		 * 
		 * @param id
		 *            event id for server
		 * @param title
		 *            the main text of the event
		 * @param provider
		 *            the minor text of the event
		 * @param position
		 *            the location of the event
		 * @param zoom
		 *            the zoom level for the map of the event
		 */
		public Event(int id, String title, String provider, LatLng position,
				float zoom) {
			this.title = title;
			this.message = provider;
			this.location = position;
			this.zoom = zoom;
			this.id = id;
		}

		public Event() {
		}
	}

}
