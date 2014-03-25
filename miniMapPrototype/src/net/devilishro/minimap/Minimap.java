package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers.Type;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Main app activity. Contains login logic.
 * 
 * @author trickyloki3
 * @author Daniel
 */
public class Minimap extends Activity {
	private EditText login_email; // login email
	private EditText login_pass; // login password

	private final int serverPort = 33600;
	private Network loginServer;

	/**
	 * Send packet 0xa3; registration
	 * 
	 * @author trickyloki3
	 */
	public void onClickRegisterButton(View view) {
		if (!State.networkBypass)
			/*
			 * sendToServer(0xa3, login_email.getText().toString(), login_pass
			 * .getText().toString());
			 */
			loginServer.send(PacketCreator.register(login_email.getText()
					.toString(), login_pass.getText().toString()));
	}

	/**
	 * Send packet 0xa1; authentication
	 * 
	 * @author trickyloki3
	 */
	public void onClickConnectButton(View view) {
		if (!State.networkBypass)
			/*
			 * sendToServer(0xa1, login_email.getText().toString(), login_pass
			 * .getText().toString());
			 */
			loginServer.send(PacketCreator.login(login_email.getText()
					.toString(), login_pass.getText().toString()));
		else
			startEventActivity();
	}

	/**
	 * Generate the XML layout and initialize the email, password, and
	 * authentication view.
	 * 
	 * @author trickyloki3
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_minimap);

		login_email = (EditText) findViewById(R.id.useremail);
		login_pass = (EditText) findViewById(R.id.userpass);
	}

	/**
	 * Connect to login server.
	 * 
	 * @author trickyloki3
	 */
	@Override
	public void onResume() {
		super.onResume();
		// TODO to be phased out
		// new CreateCommThreadTask().execute();
		loginServer = new Network(Type.LOGIN, State.serverAddress, serverPort,
				this);
		loginServer.start();
	}

	/**
	 * Close the login server connection.
	 * 
	 * @author trickyloki3
	 */
	@Override
	public void onPause() {
		super.onPause();
		/*
		 * if (login_socket != null) new CloseSocketTask().execute();
		 */
		if (loginServer != null) {
			loginServer.close();
			loginServer = null;
		}
	}

	public void startEventActivity() {
		State.setEventNumber(1);
		State.getEvents()[0] = new EventActivity.Event(0, "Test Event",
				"Provider", new LatLng(28.059891, -82.416183), 17.0f);
		State.setAdmin(true);
		Intent i = new Intent(this, EventActivity.class);
		startActivity(i);
	}

	/**
	 * UIupdate adapted from Beginning Android 4 Update txtMessage's EditText
	 * view
	 * 
	 * @author trickyloki3
	 */
	@SuppressLint("HandlerLeak")
	public Handler UIupdate = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				Toast.makeText(Minimap.this, "Registration Failed",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 1) {
				Toast.makeText(Minimap.this, "Registration Successful",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 2) {
				Toast.makeText(Minimap.this, "Login Incorrect",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 3) {
				Toast.makeText(Minimap.this, "Already Logged In",
						Toast.LENGTH_LONG).show();
			}
		}
	};
}
