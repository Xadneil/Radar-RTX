package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class EventAdd extends Activity {
	private final int EVENT_LOCATION = 0;
	private CameraPosition camera;
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
		if (!checkLocationData())
			return;
		String name, team1, team2, message;
		int type;
		double lat, lng;
		float zoom, bearing;
		name = ((EditText) findViewById(R.id.eventedit)).getText().toString();
		team1 = ((EditText) findViewById(R.id.teamoneedit)).getText()
				.toString();
		team2 = ((EditText) findViewById(R.id.teamtwoedit)).getText()
				.toString();
		type = 1 + ((Spinner) findViewById(R.id.eventmatchtype))
				.getSelectedItemPosition();
		message = ((EditText) findViewById(R.id.eventMessage)).getText()
				.toString();
		lat = camera.target.latitude;
		lng = camera.target.longitude;
		zoom = camera.zoom;
		bearing = camera.bearing;
		AppState.getEventServer().send(
				PacketCreator.addEvent(name, team1, team2, type, message, lat, lng, zoom, bearing));
	}

	private boolean checkLocationData() {
		double latitude, longitude;
		float zoom, bearing;

		String split[] = ((EditText) findViewById(R.id.locationedit)).getText()
				.toString().split(",");

		try {
			latitude = Double.parseDouble(split[0]);
			longitude = Double.parseDouble(split[1]);
		} catch (Exception e) {
			error.setText("Location must be <latitude>, <longitude>");
			return false;
		}

		try {
			zoom = Float.parseFloat(((EditText) findViewById(R.id.zoomedit))
					.getText().toString());
		} catch (Exception e) {
			error.setText("Zoom must be a number.");
			return false;
		}

		try {
			bearing = Float
					.parseFloat(((EditText) findViewById(R.id.bearingedit))
							.getText().toString());
		} catch (Exception e) {
			error.setText("Bearing must be a number.");
			return false;
		}

		camera = new CameraPosition(new LatLng(latitude, longitude), zoom, 0,
				bearing);
		return true;
	}

	public void onEventLocationClick(View view) {
		Intent intent = new Intent(this, EventLocation.class);
		this.startActivityForResult(intent, EVENT_LOCATION);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventadd);

		// setup spinner
		String eventMatchTypeList[] = getResources().getStringArray(
				R.array.eventmatchtypelst);
		error = (TextView) findViewById(R.id.event_add_error);
		Spinner eventMatchTypeSpinner = (Spinner) findViewById(R.id.eventmatchtype);
		ArrayAdapter<String> eventMatchTypeAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, eventMatchTypeList);
		eventMatchTypeSpinner.setAdapter(eventMatchTypeAdapter);
		((EditText) findViewById(R.id.eventedit)).requestFocus();
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

	@Override
	public void onActivityResult(int request, int result, Intent data) {
		if (request == EVENT_LOCATION && result == Activity.RESULT_OK) {
			CameraPosition pos = data.getParcelableExtra("CAMERA");
			this.camera = pos;
			((EditText) findViewById(R.id.locationedit)).setText(String.format(
					"%.8f, %.8f", pos.target.latitude, pos.target.longitude));
			((EditText) findViewById(R.id.zoomedit)).setText(String.format(
					"%.2f", pos.zoom));
			((EditText) findViewById(R.id.bearingedit)).setText(String.format(
					"%.2f", pos.bearing));
		}
	}

	public Handler getHandler() {
		return handler;
	}
}
