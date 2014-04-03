package net.devilishro.minimap;

import java.util.ArrayList;

import net.devilishro.minimap.EventActivity.Event;
import net.devilishro.minimap.network.Network.Activities;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Note_acti extends Activity {

	EventCheckAdapter adapter;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				finish();
			} else {
				Toast.makeText(Note_acti.this, (String) msg.obj,
						Toast.LENGTH_LONG).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_acti);

		ListView eventsList = (ListView) this
				.findViewById(R.id.player_list);
		adapter = new EventCheckAdapter(this, AppState.getEvents());
		eventsList.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		AppState.getEventServer()
				.registerContext(this, Activities.NOTIFICATION);
	}

	@Override
	public void onPause() {
		super.onPause();
		AppState.getEventServer().unregisterContext(Activities.NOTIFICATION);
	}

	public void onSendClicked(View view) {
		ListView events = (ListView) findViewById(R.id.player_list);
		SparseBooleanArray positions = events.getCheckedItemPositions();
		ArrayList<Integer> selectedIds = new ArrayList<Integer>();
		for (int i = 0; i < positions.size(); i++) {
			if (positions.get(i)) {
				selectedIds.add(adapter.events[i].id);
			}
		}
		if (selectedIds.isEmpty()) {
			Toast.makeText(this, "No Events Selected.", Toast.LENGTH_SHORT)
					.show();
		} else {
			String message = ((EditText) findViewById(R.id.notification))
					.getText().toString();
			AppState.getEventServer().send(
					PacketCreator.eventNotification(message, selectedIds));
			if (AppState.networkBypass) {
				PacketHandlers.eventNotificationCreate.handlePacket(null,
						AppState.getEventServer(), AppState.getEventServer()
								.getContext());
			}
		}
	}

	public void response(String error) {
		if (error == null) {
			// no error, finish
			handler.obtainMessage(0).sendToTarget();
		} else {
			// error, display it
			handler.obtainMessage(1, error).sendToTarget();
		}
	}

	private static class EventCheckAdapter extends ArrayAdapter<Event> {
		public Event events[];

		public EventCheckAdapter(Note_acti activity, Event list[]) {
			super(activity, R.id.player_list, list);
			events = list;
		}

		@Override
		public View getView(int pos, View convert, ViewGroup parent) {
			Event temp;
			// Check if the convert is null, if it is null it probably means
			// that this is the first time the view has been displayed
			if (convert == null) {
				convert = View.inflate(getContext(),
						R.layout.notification_checkbox, null);
			}

			temp = events[pos];

			if (temp != null) {
				((TextView) convert.findViewById(R.id.notificationEvent))
						.setText(temp.title);
				((TextView) convert.findViewById(R.id.notificationEventMessage))
						.setText(temp.message);
			}
			return convert;
		}
	}
}
