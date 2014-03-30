package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EventAdd extends Activity {
	String[] eventMatchTypeList;
	TextView error;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				error.setText("");
				finish();
			} else if (msg.what == 1) {
				short status = (short) msg.arg1;
				String eString = "";
				if ((status & 0x0002) != 0) {
					eString += "Event name length must be 5 - 25 characters.\n";
				}
				if ((status & 0x0004) != 0) {
					eString += "Another event with the same name already exists.\n";
				}
				if ((status & 0x0005) != 0) {
					eString += "Team 1 name length must be 5 - 25 characters.\n";
				}
				if ((status & 0x0010) != 0) {
					eString += "Team 2 name length must be 5 - 25 characters.\n";
				}
				if ((status & 0x0020) != 0) {
					eString += "Team 1 name and Team 2 name must be different.\n";
				}
				if ((status & 0x0040) != 0) {
					eString += "Invalid event type.\n";
				}
				if ((status & 0x0080) != 0) {
					eString += "Message length must be 1 - 50 characters.\n";
				}
				if ((status & 0x0100) != 0) {
					eString += "Invalid privledge.\n";
				}
				if ((status & 0x0200) != 0) {
					// not connected
					throw new RuntimeException("EventAdd: Not Connected");
				}
				error.setText(eString);
			}
		}
	};

	public void onAddEventClick(View view) {
		String name, team1, team2, message;
		int type;
		name = ((EditText) findViewById(R.id.eventedit)).getText().toString();
		team1 = ((EditText) findViewById(R.id.teamoneedit)).getText()
				.toString();
		team2 = ((EditText) findViewById(R.id.teamtwoedit)).getText()
				.toString();
		type = 1 + ((Spinner) findViewById(R.id.eventmatchtype))
				.getSelectedItemPosition();
		message = ((EditText) findViewById(R.id.eventMessage)).getText()
				.toString();
		AppState.getEventServer().send(
				PacketCreator.addEvent(name, team1, team2, type, message));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventadd);

		// setup spinner
		eventMatchTypeList = getResources().getStringArray(
				R.array.eventmatchtypelst);
		error = (TextView) findViewById(R.id.event_add_error);
		Spinner eventMatchTypeSpinner = (Spinner) findViewById(R.id.eventmatchtype);
		ArrayAdapter<String> eventMatchTypeAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, eventMatchTypeList);
		eventMatchTypeSpinner.setAdapter(eventMatchTypeAdapter);
		eventMatchTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						int index = arg0.getSelectedItemPosition();
						Toast.makeText(getBaseContext(),
								eventMatchTypeList[index], Toast.LENGTH_SHORT)
								.show();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	@Override
	public void onResume() {
		AppState.getEventServer().registerContext(this,
				Network.Activities.EVENT_ADD);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		AppState.getEventServer().unregisterContext(
				Network.Activities.EVENT_ADD);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AppState.getEventServer().unregisterContext(
				Network.Activities.EVENT_ADD);
	}

	public Handler getHandler() {
		return handler;
	}
}
