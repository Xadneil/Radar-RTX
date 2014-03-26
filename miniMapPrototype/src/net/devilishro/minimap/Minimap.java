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
	private EditText login_pass_confirm; // login password confirm
	private boolean isRegister = false;

	/**
	 * Send packet 0xa3; registration
	 * 
	 * @author trickyloki3
	 */
	public void onClickRegisterButton(View view) {
		if (!isRegister) {
			isRegister = true;
			findViewById(R.id.password_confirm_text).setVisibility(View.VISIBLE);
			login_pass_confirm.setVisibility(View.VISIBLE);
			login_pass_confirm.requestFocus();
		} else {
			if (login_pass.getText().toString()
					.equals(login_pass_confirm.getText().toString())) {
				
				isRegister = false;
				findViewById(R.id.password_confirm_text).setVisibility(View.INVISIBLE);
				login_pass_confirm.setVisibility(View.INVISIBLE);
				
				if (!State.networkBypass) {
					if (State.getLoginServer() == null) {
						Toast.makeText(this,
								"Please wait until the network is ready.",
								Toast.LENGTH_LONG).show();
					} else {
						State.getLoginServer().send(
								PacketCreator.register(login_email.getText()
										.toString(), login_pass.getText()
										.toString()));
					}
				}
			} else {
				Toast.makeText(this, "The passwords must be the same!", Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Send packet 0xa1; authentication
	 * 
	 * @author trickyloki3
	 */
	public void onClickConnectButton(View view) {
		if (!State.networkBypass) {
			if (State.getLoginServer() == null) {
				Toast.makeText(this, "Please wait until the network is ready.",
						Toast.LENGTH_LONG).show();
			} else {
				State.getLoginServer().send(
						PacketCreator.login(login_email.getText().toString(),
								login_pass.getText().toString()));
			}
		} else
			startEventActivity();
	}

	private void verifyInformation() {
		//>5
		//<=25
		//user:alpha, digit
		//pass:needs upper, digit
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
		login_pass_confirm = (EditText) findViewById(R.id.password_confirm);
	}

	/**
	 * Connect to login server.
	 * 
	 * @author trickyloki3
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (State.getLoginServer() != null) {
			State.getLoginServer().registerContext(this,
					Network.Activities.LOGIN);
			if (!State.getLoginServer().isRunning()) {
				State.getLoginServer().start();
			}
		}
	}

	/**
	 * Close the login server connection.
	 * 
	 * @author trickyloki3
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (State.getLoginServer() != null) {
			State.getLoginServer().unregisterContext(Network.Activities.LOGIN);
			State.getLoginServer().close();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
				Toast.makeText(Minimap.this, "Invalid Username or Password",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 3) {
				Toast.makeText(
						Minimap.this,
						Minimap.this.login_email.getText().toString()
								+ "Is Already Logged In", Toast.LENGTH_LONG)
						.show();
			}
		}
	};
}
