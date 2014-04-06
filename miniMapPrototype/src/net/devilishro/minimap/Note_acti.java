package net.devilishro.minimap;

import net.devilishro.minimap.network.Network.Activities;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class Note_acti extends Activity {

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
		String message = ((EditText) findViewById(R.id.notification)).getText()
				.toString();

		short urgency = 0;
		switch (((Spinner) findViewById(R.id.urgency))
				.getSelectedItemPosition()) {
		case 0:
			urgency = 0x0001;
			break;
		case 1:
			urgency = 0x0003;
			break;
		case 2:
			urgency = 0x0007;
			break;
		default:
			urgency = 0;
			break;
		}

		AppState.getEventServer().send(
				PacketCreator.eventNotification(message, urgency));
		if (AppState.networkBypass) {
			PacketHandlers.eventNotificationCreate.handlePacket(null, AppState
					.getEventServer(), AppState.getEventServer().getContext());
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
}
