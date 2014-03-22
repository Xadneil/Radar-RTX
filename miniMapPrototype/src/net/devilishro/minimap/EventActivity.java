package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.Packet;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import net.devilishro.minimap.network.PacketHandlers.Type;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
			State.setCurrentEvent(position); // update State
			// send packet to server
			eventServer.send(PacketCreator.selectEvent(position));

			if (State.networkDebug) {
				PacketHandlers.eventChoose.handlePacket(new Packet(), null,
						EventActivity.this);
			}
		}
	};

	public Network eventServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event);
		ListView l = (ListView) this.findViewById(R.id.eventListView);
		Log.d("EventActivity", "State.getEvents(): "
				+ (State.getEvents() == null ? "yes" : "no"));
		l.setAdapter(new EventAdapter(this));
		l.setOnItemClickListener(clickListener);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (eventServer == null) {
			eventServer = new Network(Type.EVENT, State.getServerAddress(),
					33601 /* event port */, this);
			eventServer.start();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		eventServer.close();
		eventServer = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Replays");
		menu.add(0, 1, 1, "FriendForcer");
		menu.add(0, 2, 2, "Logout");
		if (State.isAdmin()) {
			menu.add(0, 3, 3, "Event Add");
			menu.add(0, 4, 4, "Event Notify");
			menu.add(0, 5, 5, "Player List");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: // replay
		{
			Intent i = new Intent(this, Replay.class);
			startActivity(i);
			break;
		}
		case 1: // friendforcer
		{
			Intent i = new Intent(this, EventJoinActivity.class);
			startActivity(i);
			break;
		}
		case 2:// logout
				// intent
			break;
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

	/**
	 * Proceeds from event screen to join screen. Used by the event handler for
	 * event choosing.
	 */
	public void startJoinActivity() {
		Intent i = new Intent(EventActivity.this, EventJoinActivity.class);
		startActivity(i);
	}

	/**
	 * ArrayAdapter for showing custom layouts for each event
	 * 
	 * @author Daniel
	 */
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

	/**
	 * Event information
	 * 
	 * @author Daniel
	 */
	public static class Event {
		public String title, provider;
		public LatLng position;
		public float zoom;

		/**
		 * Class Constructor
		 * 
		 * @param id
		 *            event id for server
		 * @param title
		 * @param provider
		 * @param position
		 * @param zoom
		 */
		public Event(int id, String title, String provider, LatLng position,
				float zoom) {
			this.title = title;
			this.provider = provider;
			this.position = position;
			this.zoom = zoom;
		}
	}

}
